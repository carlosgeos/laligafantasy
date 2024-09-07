(ns laliga-fantasy.owned-players
  (:require
   [hugsql.core :as hugsql]
   [java-time.api :as jt]
   [laliga-fantasy.api :as api]
   [laliga-fantasy.auth :as auth]
   [laliga-fantasy.db :as db]
   [laliga-fantasy.env :as env]
   [laliga-fantasy.manager :as manager]
   [laliga-fantasy.util :as u]
   [next.jdbc.sql :as sql]
   [taoensso.telemere :as t]))

(hugsql/def-db-fns "laliga_fantasy/sql/main.sql")
(declare drop-owned-players-table)
(declare create-owned-players-table)

(defn- api-endpoint
  [manager-id]
  (format "https://api-fantasy.llt-services.com/api/v3/leagues/%s/teams/%s"
          (:league-id env/config)
          manager-id))

(defn- format-response
  "This `manager-id` is the outer level one (account level?), not the
  league level manager-id"
  [manager-id {:keys [buyoutClause manager buyoutClauseLockedEndTime playerMaster]}]
  {:player_id              (u/coerce-to-int (:id playerMaster))
   :manager                (:managerName manager)
   :league_manager_id      (:id manager)
   :manager_id             manager-id
   :buyout                 (u/coerce-to-int buyoutClause)
   :buyout_lock_expiration (->> (jt/instant u/default-datetime-formatter buyoutClauseLockedEndTime)
                                (jt/instant->sql-timestamp))
   :created_at             (jt/sql-timestamp)})

(defn- owned-players*
  []
  (let [manager-ids (-> (manager/manager-ids)
                        (conj (:manager-id env/config))
                        (set))
        players     (map #(->> (api/get* (api-endpoint %) {:oauth-token (auth/token)})
                               :players
                               (map (partial format-response %)))
                         manager-ids)]
    (flatten players)))

(def owned-players (memoize owned-players*))

(defn load-owned-players-to-db
  []
  (t/log! :info "Rebuilding owned_players table...")
  (drop-owned-players-table db/ds)
  (create-owned-players-table db/ds)
  (sql/insert-multi! db/ds :owned_players (owned-players)))
