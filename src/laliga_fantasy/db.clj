(ns laliga-fantasy.db
  (:require
   [hugsql.adapter.next-jdbc :as next-adapter]
   [hugsql.core :as hugsql]
   [laliga-fantasy.env :as env]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]))

(hugsql/set-adapter! (next-adapter/hugsql-adapter-next-jdbc {:builder-fn rs/as-unqualified-maps}))

(def ds (jdbc/get-datasource (:database-url env/config)))
