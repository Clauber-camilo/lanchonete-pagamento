(ns mba-fiap.datasource.mongo
  (:require
    [com.brunobonacci.mulog :as log]
    [integrant.core :as ig]
    [monger.core :as mg]))


(defmethod ig/init-key ::db
  [_ {:keys [spec]}]
  (println ".:: Initializing db ::.")

  (let [uri (get-in spec [:uri])
        db-name (get-in spec [:db-name])
        {:keys [conn]} (mg/connect-via-uri uri)
        db (mg/get-db conn db-name)]

    (println ".:: DB initialized ::.")
    (assoc
      spec
      :datasource conn
      :db db)))


(defmethod ig/resolve-key ::db
  [_ {:keys [db]}]
  db)
