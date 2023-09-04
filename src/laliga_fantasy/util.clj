(ns laliga-fantasy.util)

(defn coerce-to-int
  [n]
  (cond
    (number? n)
    n

    (and (string? n)
         (not-empty n))
    (Integer/parseInt n)))
