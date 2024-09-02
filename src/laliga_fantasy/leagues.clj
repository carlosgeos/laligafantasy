(ns laliga-fantasy.leagues
  (:require
   [clj-http.client :as client]
   [laliga-fantasy.auth :as auth]))

(defn- leagues*
  []
  (let [url "https://api-fantasy.llt-services.com/api/v3/leagues"]
    (->> (client/get url {:as          :json
                          :oauth-token (auth/token)})
         :body)))

;;; This endpoint is useful to fetch one's `league-id`
(def leagues (memoize leagues*))
