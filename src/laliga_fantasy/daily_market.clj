(ns laliga-fantasy.daily-market
  (:require
   [clj-http.client :as client]
   [hugsql.core :as hugsql]
   [java-time.api :as jt]
   [laliga-fantasy.auth :as auth]
   [laliga-fantasy.db :as db]
   [laliga-fantasy.env :as env]
   [laliga-fantasy.util :as u]
   [next.jdbc.sql :as sql]
   [taoensso.timbre :as log]))

(hugsql/def-db-fns "laliga_fantasy/sql/main.sql")

(defn- daily-market*
  []
  (let [url       (str "https://api-fantasy.llt-services.com/api/v3/league/"
                       (:league-id env/config)
                       "/market")
        fmt       (jt/formatter :iso-offset-date-time)
        format-fn (fn [{:keys [playerMaster directOffer discr expirationDate
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
                     :expiration   (->> (jt/instant fmt expirationDate)
                                        (jt/instant->sql-timestamp))
                     :created_at   (jt/sql-timestamp)})]
    (->> (client/get url {:as          :json
                          :oauth-token (auth/token)})
         :body
         (pmap format-fn))))

(defn load-daily-market-to-db
  []
  (log/info "Rebuilding daily_market table...")
  (drop-daily-market-table db/ds)
  (create-daily-market-table db/ds)
  (sql/insert-multi! db/ds :daily_market (daily-market*))
  (log/info "Done."))
