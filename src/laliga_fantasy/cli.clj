(ns laliga-fantasy.cli
  (:require
   [laliga-fantasy.activity :as activity])
  (:import
   (java.text NumberFormat)
   (java.util Locale)))

(defn print-players-cash-amounts
  [& _args]
  (let [cash-amounts (->> (activity/get-players-cash-amounts)
                          (sort-by second)
                          (reverse))
        fmt          (NumberFormat/getInstance (Locale. "ES"))]
    (println "--------------------------------------")
    (doseq [[player amount] cash-amounts]
      (println (format "%20s" player)
               ":\t" "€"
               (format "%11s" (.format fmt amount))))))
