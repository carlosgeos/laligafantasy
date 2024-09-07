(ns laliga-fantasy.player
  (:require
   [hugsql.core :as hugsql]
   [laliga-fantasy.api :as api]
   [laliga-fantasy.auth :as auth]
   [laliga-fantasy.db :as db]
   [laliga-fantasy.env :as env]
   [laliga-fantasy.util :as u]
   [next.jdbc.sql :as sql]
   [taoensso.telemere :as t]))

(hugsql/def-db-fns "laliga_fantasy/sql/main.sql")
(declare drop-players-table)
(declare create-players-table)

(defn- api-endpoint
  []
  (format "https://api-fantasy.llt-services.com/api/v3/players/league/%s"
          (:league-id env/config)))

(defn- format-response
  [{:keys [positionId playerStatus nickname points marketValue
           id lastSeasonPoints averagePoints]}]
  {:player_id          (u/coerce-to-int id)
   :name               nickname
   :position           (case positionId
                         "1" "GKE"
                         "2" "DEF"
                         "3" "MID"
                         "4" "ATA"
                         "5" "COA")
   :status             playerStatus
   :points             points
   :market_value       (u/coerce-to-int marketValue)
   :points_last_season (u/coerce-to-int lastSeasonPoints)
   :avg_points         (u/coerce-to-int averagePoints)})

(defn- players*
  []
  (->> (api/get* (api-endpoint) {:oauth-token (auth/token)})
       (map format-response)))

(def players (memoize players*))

(defn player-ids
  []
  (map :player_id (players)))

(defn load-players-to-db
  []
  (t/log! :info "Rebuilding players table...")
  (drop-players-table db/ds)
  (create-players-table db/ds)
  (sql/insert-multi! db/ds :players (players)))
