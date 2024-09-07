(ns laliga-fantasy.auth
  (:require
   [laliga-fantasy.api :as api]
   [laliga-fantasy.env :as env]))

(defn code
  ([]
   (code (:username env/config) (:password env/config)))
  ([username password]
   (let [auth-url "https://api-fantasy.llt-services.com/login/v3/email/auth"
         payload  {:policy   "B2C_1A_ResourceOwnerv2"
                   :username username
                   :password password}]
     (:code (api/post* auth-url {:form-params payload})))))

(defn- token*
  ([]
   (token* (:username env/config) (:password env/config)))
  ([username password]
   (let [token-url "https://api-fantasy.llt-services.com/login/v3/email/token"
         payload   {:code   (code username password)
                    :policy "B2C_1A_ResourceOwnerv2"}]
     (:access_token (api/post* token-url {:form-params payload})))))

;;; This fn can be memoized because the lifespan of any of this app's
;;; processes won't be > 24 hours, which is the token expiration time
(def token (memoize token*))
