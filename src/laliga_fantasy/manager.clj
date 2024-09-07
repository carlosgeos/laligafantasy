(ns laliga-fantasy.manager
  (:require
   [laliga-fantasy.api :as api]
   [laliga-fantasy.auth :as auth]
   [laliga-fantasy.env :as env]))

(defn- api-endpoint
  []
  (format "https://api-fantasy.llt-services.com/api/v3/leagues/%s/ranking/"
          (:league-id env/config)))

(defn- managers*
  []
  (api/get* (api-endpoint) {:oauth-token (auth/token)}))

(def managers (memoize managers*))

(defn manager-ids
  "Iterating over the manager ids is useful in other contexts and API
  queries"
  []
  (map (comp :id :team) (managers)))
