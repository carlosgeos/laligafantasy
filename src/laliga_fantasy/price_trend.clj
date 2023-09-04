(ns laliga-fantasy.price-trend
  (:require
   [hugsql.core :as hugsql]
   [java-time.api :as jt]
   [laliga-fantasy.db :as db]
   [laliga-fantasy.price-history :as price.h]
   [next.jdbc.sql :as sql]
   [taoensso.timbre :as log]))

(hugsql/def-db-fns "laliga_fantasy/sql/main.sql")

(defn- pct-change
  "Return the percentage increase/decrease"
  [before after]
  (double (* (/ (- after before)
                before)
             100)))

(defn- change
  ([prices]
   (change prices Integer/MAX_VALUE))
  ([prices interval]
   (let [sorted-prices (->> (sort-by :ts prices)
                            (reverse)
                            (map :market_value))
         after         (first sorted-prices)
         before        (nth sorted-prices interval (last sorted-prices))]
     (pct-change before after))))

(defn price-trends
  []
  (->> (price.h/all-price-history)
       (group-by :player_id)
       (pmap (fn [[player_id prices]]
               {:player_id  player_id
                :change_3d  (change prices 3)
                :change_7d  (change prices 7)
                :change_14d (change prices 14)
                :change_30d (change prices 30)
                :change_all (change prices)
                :created_at (jt/sql-timestamp)}))))

(defn load-price-trends-to-db
  []
  (log/info "Rebuilding price_trends...")
  (drop-price-trends-table db/ds)
  (create-price-trends-table db/ds)
  (sql/insert-multi! db/ds :price_trends (price-trends))
  (log/info "Done."))
