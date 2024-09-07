(ns laliga-fantasy.price-history
  (:require
   [clojure.core.async :as a]
   [clojure.data.json :as json]
   [hugsql.core :as hugsql]
   [java-time.api :as jt]
   [laliga-fantasy.db :as db]
   [laliga-fantasy.player :as player]
   [laliga-fantasy.util :as u]
   [next.jdbc.sql :as sql]
   [org.httpkit.client :as client]
   [taoensso.telemere :as t]
   [taoensso.timbre :as log]))

(hugsql/def-db-fns "laliga_fantasy/sql/main.sql")
(declare drop-price-history-table)
(declare create-price-history-table)

(defn- normalize-price-history-point
  "`player-id` doesn't come in the response, so it has to come from this
  fn's caller"
  [player-id {:keys [marketValue date]}]
  {:player_id    (u/coerce-to-int player-id)
   :market_value (u/coerce-to-int marketValue)
   :ts           (-> (jt/instant (jt/formatter :iso-offset-date-time) date)
                     (jt/instant->sql-timestamp))
   :created_at   (jt/sql-timestamp)})

(defn- ok-handler
  [result player-id {:keys [_opts _status _headers body]}]
  (a/go
    (let [parsed-response     (json/read-str body :key-fn keyword)
          normalized-response (map (partial normalize-price-history-point player-id) parsed-response)]
      (a/>! result normalized-response)
      (a/close! result))))

(defn- ex-handler
  [player-id {:keys [_opts error]}]
  (log/error "Error fetching price_history for player-id" player-id)
  (log/error (.getMessage error))
  (throw error))

(defn- api-endpoint
  [player-id]
  (format "https://api-fantasy.llt-services.com/api/v3/player/%s/market-value"
          player-id))

(defn- price-history-async*
  "`pipeline-async` handlers must return immediately, shouldn't have any
  blocking operations and must close the internal `result` chan after
  `put`ing.

  `org.httpkit.client/get` puts the request in flight and returns an
  Apache's `BasicFuture`."
  [player-id result]
  (a/go
    (try
      (t/log! :info ["Requesting price_history for player-id" player-id])
      (client/get (api-endpoint player-id)
                  (partial ok-handler result player-id)
                  (partial ex-handler player-id))
      (catch Throwable e
        (log/error e)
        (a/close! result)
        (throw e)))))

(defn- all-price-history*
  "`pipeline-async` is very different from `pipeline`. The `parallelism`
  arg is off by two. The reason is:
  https://ask.clojure.org/index.php/333/off-by-two-in-pipeline-async-parallelism?show=485#a485

  An async dispatcher parallelism of 5 is more than enough here. Go
  blocks all share 8 JVM threads, and that's why they must be properly
  parked and not blocked"
  []
  (let [from       (a/chan 50)
        to         (a/chan 50)
        _          (a/onto-chan! from (player/player-ids))]
    (a/pipeline-async
     5
     to
     price-history-async*
     from)
    (->> (a/<!! (a/into [] to))
         (flatten))))

(def all-price-history (memoize all-price-history*))

(defn load-price-history-to-db
  "`next.jdbc.sql/insert-multi!` needs the batch opt set to true, as the
  number of parameters cannot be > 65535 (it compiles to a single
  prepared statement). The batch option does batching automatically"
  []
  (t/log! :info "Rebuilding price_history...")
  (drop-price-history-table db/ds)
  (create-price-history-table db/ds)
  (sql/insert-multi! db/ds :price_history (all-price-history) {:batch true}))

(comment
;;; Pipeline async results viewer
  (a/go-loop [res (a/<! to)]
    (if (not (nil? res))
      (do (println res)
          (recur (a/<! to)))
      (do
        (println "outchan close. done.")
        nil)))

  (def c (a/chan 1))
  (price-history-async* 1718 c)

  )
