(ns laliga-fantasy.api
  (:require
   [clojure.data.json :as json]
   [org.httpkit.client :as client]))

(defn get*
  "Synchronous GET request, returning the reponse body as JSON"
  [url & [opts]]
  (-> @(client/get url opts)
      :body
      (json/read-str :key-fn keyword)))

(defn post*
  "Synchronous POST request, returning the response body as JSON"
  [url & [opts]]
  (-> @(client/post url opts)
      :body
      (json/read-str :key-fn keyword)))
