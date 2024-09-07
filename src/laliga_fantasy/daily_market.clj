(ns laliga-fantasy.daily-market
  (:require
   [hugsql.core :as hugsql]
   [java-time.api :as jt]
   [laliga-fantasy.api :as api]
   [laliga-fantasy.auth :as auth]
   [laliga-fantasy.db :as db]
   [laliga-fantasy.env :as env]
   [laliga-fantasy.util :as u]
   [next.jdbc.sql :as sql]
   [taoensso.telemere :as t]))

(hugsql/def-db-fns "laliga_fantasy/sql/main.sql")
(declare drop-daily-market-table)
(declare create-daily-market-table)

(defn- api-endpoint
  []
  (format "https://api-fantasy.llt-services.com/api/v3/league/%s/market"
          (:league-id env/config)))

(defn- format-response
  [{:keys [playerMaster directOffer discr expirationDate
           numberOfBids numberOfOffers salePrice sellerTeam]}]
  {:player_id    (u/coerce-to-int (:id playerMaster))
   :owner        (case discr
                   "marketPlayerLeague" "LaLiga"
                   "marketPlayerTeam"   (some-> sellerTeam
                                                :manager
                                                :managerName))
   :direct_offer directOffer
   :sale_price   (u/coerce-to-int salePrice)
   :offers       (or (u/coerce-to-int numberOfOffers) 0)
   :bids         (or (u/coerce-to-int numberOfBids) 0)
   :expiration   (->> (jt/instant u/default-datetime-formatter expirationDate)
                      (jt/instant->sql-timestamp))
   :created_at   (jt/sql-timestamp)})

(defn- daily-market*
  []
  (->> (api/get* (api-endpoint) {:oauth-token (auth/token)})
       (map format-response)))

(defn load-daily-market-to-db
  []
  (t/log! :info "Rebuilding daily_market table...")
  (drop-daily-market-table db/ds)
  (create-daily-market-table db/ds)
  (sql/insert-multi! db/ds :daily_market (daily-market*)))
