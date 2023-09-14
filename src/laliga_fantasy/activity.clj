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
  (let [regex-pattern #"^([A-zÀ-ú0-9]+)\sha\s(?:comprado|vendido)\sa\s([A-zÀ-ú\s]+)\sa\s(\w+)\spor\s([\d\.]+)\s€"
        groups        (re-find regex-pattern msg)]
    {:origin      (nth groups 1)
     :destination (nth groups 3)
     :player_name (nth groups 2)
     :tx_type     (activity-type msg)
     :amount      (parse-amount (nth groups 4))}))

(defn- activity
  []
  (let [url       (str "https://api-fantasy.llt-services.com/api/v3/leagues/"
                       (:league-id env/config)
                       "/news/1")
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

(defn load-activity-to-db
  []
  (log/info "Rebuilding activity table...")
  (drop-activity-table db/ds)
  (create-activity-table db/ds)
  (sql/insert-multi! db/ds :activity (activity))
  (log/info "Done."))
