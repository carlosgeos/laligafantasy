(ns laliga-fantasy.manager
  (:require
   [clj-http.client :as client]
   [laliga-fantasy.auth :as auth]
   [laliga-fantasy.env :as env]))

(defn- manager-ids*
  []
  (let [ranking-url (str "https://api-fantasy.llt-services.com/api/v3/leagues/"
                         (:league-id env/config)
                         "/ranking/4")
        format-fn   (fn [{:keys [team]}]
                      (:id team))]
    (->> (client/get ranking-url {:as          :json
                                  :oauth-token (auth/token)})
         :body
         (pmap format-fn))))

(def manager-ids (memoize manager-ids*))
