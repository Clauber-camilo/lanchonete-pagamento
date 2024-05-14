(ns mba-fiap.usecase.testing
  (:require
    [integrant.core :as ig]))


(defn testing
  [_ event]
  (tap> {:from "testing"
         :event event}))


(defmethod ig/init-key ::test [_ _]
  testing)
