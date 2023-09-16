(ns laliga-fantasy.activity
  (:require
   [clj-http.client :as client]
   [clojure.string :as string]
   [hugsql.core :as hugsql]
   [java-time.api :as jt]
   [laliga-fantasy.auth :as auth]
   [laliga-fantasy.db :as db]
   [laliga-fantasy.env :as env]
   [laliga-fantasy.util :as u]
   [next.jdbc.result-set :as rs]
   [next.jdbc.sql :as sql]
   [taoensso.timbre :as log]))

(hugsql/def-db-fns "laliga_fantasy/sql/main.sql")

(defn- activity-type
  [msg]
  (cond
    (re-find #"ha\svendido\sa" msg)
    "sell"

    (re-find #"ha\scomprado\sa" msg)
    "buy"))

(defn- parse-amount
  [amount]
  (when (seq amount)
    (Integer/parseInt (string/replace amount #"\." ""))))

(defn- parse-activity
  [msg]
  (let [regex-pattern #"^([\p{L}0-9_\s]+)\sha\s(?:comprado|vendido)\sa\s([\p{L}\s]+)\sa\s([\p{L}0-9_\s]+)\spor\s([\d\.]+)\s€"
        groups        (re-find regex-pattern msg)]
    {:origin      (nth groups 1)
     :destination (nth groups 3)
     :player_name (nth groups 2)
     :tx_type     (activity-type msg)
     :amount      (parse-amount (nth groups 4))}))

(defn- activity-page
  [page]
  (let [url       (str "https://api-fantasy.llt-services.com/api/v3/leagues/"
                       (:league-id env/config)
                       "/news/"
                       page)
        fmt       (jt/formatter :iso-offset-date-time)
        format-fn (fn [{:keys [msg _title id publicationDate]}]
                    (merge
                     {:transaction_id   (u/coerce-to-int id)
                      :publication_date (->> (jt/instant fmt publicationDate)
                                             (jt/instant->sql-timestamp))
                      :created_at       (jt/sql-timestamp)}
                     (parse-activity msg)))]
    (->> (client/get url {:as          :json
                          :oauth-token (auth/token)})
         :body
         (filter #(= "Operación de mercado" (:title %)))
         (pmap format-fn))))

(defn- full-activity
  "Query activity pages until the result is an empty response"
  []
  (loop [cnt 1
         res (activity-page cnt)
         acc []]
    (if (or (empty? res) (> cnt 1000))
      acc
      (recur (inc cnt) (activity-page (inc cnt)) (concat acc res)))))

(defn- cash-balance
  "Given an array of market transactions, return the current cash in
  hand for the given player. The starting cash amount of every player
  is 100M"
  [txs]
  (let [starting-amount 100000000
        reduce-fn       (fn [acc {:keys [tx_type amount]}]
                          (cond
                            (= "sell" tx_type)
                            (+ acc amount)

                            (= "buy" tx_type)
                            (+ acc (- amount))))]
    (reduce reduce-fn starting-amount txs)))

(defn get-players-cash-amounts
  []
  (let [activities (->> (sql/query
                         db/ds
                         ["select * from activity"]
                         {:builder-fn rs/as-unqualified-maps}))
        txs-per-player (group-by :origin activities)]
    (->> (map (fn [[player txs]] [player (cash-balance txs)]) txs-per-player)
         (into {}))))

(defn load-activity-to-db
  []
  (log/info "Rebuilding activity table...")
  (drop-activity-table db/ds)
  (create-activity-table db/ds)
  (sql/insert-multi! db/ds :activity (full-activity))
  (log/info "Done."))
