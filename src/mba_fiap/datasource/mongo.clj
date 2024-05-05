(ns mba-fiap.datasource.mongo
  (:require
    [integrant.core :as ig]
    [monger.core :as mg]))


(defmethod ig/init-key ::db
  [_ {:keys [spec]}]
  (println "Initializing database connection: " spec)

  (let [uri (get-in spec [:uri])
        db-name (get-in spec [:db-name])
        {:keys [conn db]} (mg/connect-via-uri (str uri db-name))]

    (assoc
      spec
      :datasource conn
      :db db)))


(defmethod ig/resolve-key ::db
  [_ {:keys [datasource]}]
  datasource)
