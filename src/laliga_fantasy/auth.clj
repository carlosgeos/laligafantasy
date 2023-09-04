(ns laliga-fantasy.auth
  (:require
   [clj-http.client :as client]
   [laliga-fantasy.env :as env]))

(defn code
  ([]
   (code (:username env/config) (:password env/config)))
  ([username password]
   (let [auth-url "https://api-fantasy.llt-services.com/login/v3/email/auth"
         payload  {:policy   "B2C_1A_ResourceOwnerv2"
                   :username username
                   :password password}]
     (-> (client/post auth-url {:form-params payload
                                :as          :json})
         :body
         :code))))

(defn- token*
  ([]
   (token* (:username env/config) (:password env/config)))
  ([username password]
   (let [token-url "https://api-fantasy.llt-services.com/login/v3/email/token"
         payload   {:code   (code username password)
                    :policy "B2C_1A_ResourceOwnerv2"}]
     (-> (client/post token-url {:form-params payload
                                 :as          :json})
         :body
         :access_token))))

;;; This fn can be memoized because the lifespan of any of this app's
;;; processes won't be > 24 hours, which is the token expiration time
(def token (memoize token*))
