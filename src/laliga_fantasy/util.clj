(ns laliga-fantasy.util
  (:require [java-time.api :as jt]))

(def default-datetime-formatter (jt/formatter :iso-offset-date-time))

(defn coerce-to-int
  [n]
  (cond
    (number? n)
    n

    (and (string? n)
         (not-empty n))
    (Integer/parseInt n)))
