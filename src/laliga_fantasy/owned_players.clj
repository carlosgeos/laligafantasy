(ns laliga-fantasy.owned-players
  (:require
   [clj-http.client :as client]
   [hugsql.core :as hugsql]
   [java-time.api :as jt]
   [laliga-fantasy.auth :as auth]
   [laliga-fantasy.db :as db]
   [laliga-fantasy.env :as env]
   [laliga-fantasy.manager :as manager]
   [laliga-fantasy.util :as u]
   [next.jdbc.sql :as sql]
   [taoensso.timbre :as log]))

(hugsql/def-db-fns "laliga_fantasy/sql/main.sql")

(defn- owned-players*
  []
  (let [url       (fn [manager-id]
                    (str "https://api-fantasy.llt-services.com/api/v3/leagues/"
                         (:league-id env/config)
                         "/teams/" manager-id))
        format-fn (fn [manager-id {:keys [buyoutClause manager
                                          buyoutClauseLockedEndTime playerMaster]}]
                    ;; Here we grab both the top level
                    ;; `manager-id` (account level?), and not the
                    ;; league level manager id
                    {:player_id              (u/coerce-to-int (:id playerMaster))
                     :manager                (:managerName manager)
                     :league_manager_id      (:id manager)
                     :manager_id             manager-id
                     :buyout                 (u/coerce-to-int buyoutClause)
                     :buyout_lock_expiration (->> (jt/instant buyoutClauseLockedEndTime)
                                                  (jt/instant->sql-timestamp))
                     :created_at             (jt/sql-timestamp)})]
    (->> (-> (manager/manager-ids)
             (conj (:manager-id env/config))
             (set))
         (map #(->> (client/get (url %) {:as          :json
                                         :oauth-token (auth/token)})
                    :body
                    :players
                    (pmap (partial format-fn %))))
         (flatten))))

(def owned-players (memoize owned-players*))

(defn load-owned-players-to-db
  []
  (log/info "Rebuilding owned_players table...")
  (drop-owned-players-table db/ds)
  (create-owned-players-table db/ds)
  (sql/insert-multi! db/ds :owned_players (owned-players))
  (log/info "Done."))
