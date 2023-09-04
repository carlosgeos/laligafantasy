(ns laliga-fantasy.player
  (:require
   [clj-http.client :as client]
   [hugsql.core :as hugsql]
   [laliga-fantasy.auth :as auth]
   [laliga-fantasy.db :as db]
   [laliga-fantasy.env :as env]
   [laliga-fantasy.util :as u]
   [next.jdbc.sql :as sql]
   [taoensso.timbre :as log]))

(hugsql/def-db-fns "laliga_fantasy/sql/main.sql")

(defn- players*
  []
  (let [url       (str "https://api-fantasy.llt-services.com/api/v3/players/league/"
                       (:league-id env/config))
        format-fn (fn [{:keys [positionId playerStatus nickname
                               points marketValue id lastSeasonPoints
                               averagePoints]}]
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
                     :avg_points         (u/coerce-to-int averagePoints)})]
    (->> (client/get url {:as          :json
                          :oauth-token (auth/token)})
         :body
         (pmap format-fn))))

(def players (memoize players*))

(defn load-players-to-db
  []
  (log/info "Rebuilding players table...")
  (drop-players-table db/ds)
  (create-players-table db/ds)
  (sql/insert-multi! db/ds :players (players))
  (log/info "Done."))
