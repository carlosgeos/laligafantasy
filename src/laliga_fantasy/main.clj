(ns laliga-fantasy.main
  (:gen-class)
  (:require
   [hugsql.core :as hugsql]
   [laliga-fantasy.activity :as activity]
   [laliga-fantasy.daily-market :as daily-market]
   [laliga-fantasy.db :as db]
   [laliga-fantasy.env :as env]
   [laliga-fantasy.owned-players :as o.players]
   [laliga-fantasy.player :as player]
   [laliga-fantasy.price-history :as price-history]
   [laliga-fantasy.price-trend :as price-trend]
   [taoensso.telemere :as t])
  (:import
   (java.lang.management ManagementFactory)))

(hugsql/def-db-fns "laliga_fantasy/sql/main.sql")
(declare create-my-team-view)

(defn healthcheck
  "Another callable entry point to the application."
  [& _args]
  (println (format "%20s" "Host arch:")
           (.getArch (ManagementFactory/getOperatingSystemMXBean)))
  (println (format "%20s" "Java version:")
           (System/getProperty "java.version"))
  (println (format "%20s" "JVM name:")
           (System/getProperty "java.vm.name"))
  (println (format "%20s" "JVM version:")
           (System/getProperty "java.vm.version"))
  (println (format "%20s" "JVM vendor:")
           (System/getProperty "java.vm.vendor"))
  (println (format "%20s" "Clojure version:")
           (clojure-version)))

(defn picker
  [& _args]
  (println "Lineup:")
  (println "Not implemented"))

(defn- create-views
  []
  (create-my-team-view db/ds {:manager-id (:manager-id env/config)}))

(defn -main
  "Main entrypoint to the application"
  [& _args]
  (healthcheck)
  (t/log! :info "START")
  (player/load-players-to-db)
  (daily-market/load-daily-market-to-db)
  (o.players/load-owned-players-to-db)
  (price-history/load-price-history-to-db)
  (price-trend/load-price-trends-to-db)
  (activity/load-activity-to-db)

  (create-views)
  (t/log! :info "DONE")
  ;; The following is important to avoid the 1 minute waiting time
  ;; when the program execution is complete:
  ;;
  ;; https://groups.google.com/g/clojure/c/lCmpdwFp9FQ/m/zXEdJ_1NBgAJ
  ;;
  ;; This phenomenon is not well documented, but the best explanation
  ;; can be found in:
  ;;
  ;; https://clojure.atlassian.net/jira/software/c/projects/CLJ/issues/CLJ-124
  (shutdown-agents))
