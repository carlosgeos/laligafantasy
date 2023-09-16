(ns laliga-fantasy.manager
  (:require
   [clj-http.client :as client]
   [laliga-fantasy.auth :as auth]
   [laliga-fantasy.env :as env]))

(defn- managers*
  []
  (let [ranking-url (str "https://api-fantasy.llt-services.com/api/v3/leagues/"
                         (:league-id env/config)
                         "/ranking/")]
    (->> (client/get ranking-url {:as          :json
                                  :oauth-token (auth/token)})
         :body)))

(def managers (memoize managers*))


(defn manager-ids
  "Iterating over the manager ids is useful in other contexts and API
  queries"
  []
  (map (comp :id :team) (managers)))
