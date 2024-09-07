(ns laliga-fantasy.leagues
  (:require
   [laliga-fantasy.api :as api]
   [laliga-fantasy.auth :as auth]))

(defn- leagues*
  []
  (let [url "https://api-fantasy.llt-services.com/api/v3/leagues"]
    (api/get* url {:oauth-token (auth/token)})))

;;; This endpoint is useful to fetch one's `league-id`
(def leagues (memoize leagues*))
