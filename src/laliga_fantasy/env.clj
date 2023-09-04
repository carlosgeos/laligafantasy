(ns laliga-fantasy.env
  (:require
   [cprop.core :refer [load-config]]))

;;; Even though `cprop` will primarily look for a
;;; `resources/config.edn` in the classpath, it will still merge the
;;; matching ENV vars it can find at runtime.
(def config (load-config))
