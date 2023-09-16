(ns laliga-fantasy.snoop
  (:require
   [laliga-fantasy.db :as db]
   [next.jdbc.result-set :as rs]
   [next.jdbc.sql :as sql]))

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
