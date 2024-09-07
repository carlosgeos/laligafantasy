(ns laliga-fantasy.activity
  (:require
   [clojure.string :as string]
   [hugsql.core :as hugsql]
   [java-time.api :as jt]
   [laliga-fantasy.api :as api]
   [laliga-fantasy.auth :as auth]
   [laliga-fantasy.db :as db]
   [laliga-fantasy.env :as env]
   [laliga-fantasy.util :as u]
   [next.jdbc.result-set :as rs]
   [next.jdbc.sql :as sql]
   [taoensso.telemere :as t]))

(hugsql/def-db-fns "laliga_fantasy/sql/main.sql")
(declare create-activity-table)

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

(defn- api-endpoint
  [page]
  (format "https://api-fantasy.llt-services.com/api/v3/leagues/%s/news/%s"
          (:league-id env/config)
          page))

(defn- format-response
  [{:keys [msg _title id publicationDate]}]
  (merge
   {:transaction_id   (u/coerce-to-int id)
    :publication_date (->> (jt/instant u/default-datetime-formatter publicationDate)
                           (jt/instant->sql-timestamp))
    :created_at       (jt/sql-timestamp)}
   (parse-activity msg)))

(defn- activity-page
  [page]
  (->> (api/get* (api-endpoint page) {:oauth-token (auth/token)})
       (filter #(= "Operación de mercado" (:title %)))
       (map format-response)))

(defn- full-activity
  "Query activity pages until the result is an empty response"
  []
  (loop [cnt 1
         res (activity-page cnt)
         acc []]
    (if (or (empty? res) (> cnt 1000))
      acc
      (recur (inc cnt) (activity-page (inc cnt)) (concat acc res)))))

(defn load-activity-to-db
  []
  (t/log! :info "Incrementally updating activity table...")
  (create-activity-table db/ds)
  (let [existing-activity (->> (sql/query
                                db/ds
                                ["select * from activity"]
                                {:builder-fn rs/as-unqualified-maps})
                               (map :transaction_id)
                               (set))
        new-activity      (->> (full-activity)
                               (remove #(existing-activity (:transaction_id %))))]
    (if (seq new-activity)
      (do
        (t/log! :info [" --- " (count new-activity) "new activity records found"])
        (sql/insert-multi! db/ds :activity new-activity))
      (t/log! :info "No new activity records found"))))
