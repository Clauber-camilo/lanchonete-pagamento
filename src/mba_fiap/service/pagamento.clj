(ns mba-fiap.service.pagamento
  (:require
    [mba-fiap.base.validation :as validation]
    [mba-fiap.model.pagamento :as pagamento])
  (:import
    (mba_fiap.repository.repository
      Repository)
    (org.bson.types
      ObjectId)))


(defn criar-pagamento
  [^Repository repository pagamento]
  (let [pagamento (.criar repository pagamento)]
    (assoc pagamento :_id (str (:_id pagamento)))))


(defn atualizar-status-pagamento
  [^Repository repository id-pagamento status]
  {:pre [(instance? Repository repository)
         (string? id-pagamento)
         (validation/schema-check pagamento/Status status)]}
  (let [pagamento (.buscar repository (ObjectId. id-pagamento))
        pagamento (assoc pagamento :status status)
        pagamento (.atualizar repository pagamento)]
    (tap> {:from "atualizar-status-pagamento"
           :pagamento pagamento})
    pagamento))


(defn buscar-por-id-pedido
  [^Repository repository id-pedido]
  {:pre [(instance? Repository repository)]}
  (let [pagamento (.listar repository id-pedido)]
    (if (empty? pagamento)
      {:error "Pagamento não encontrado"}
      pagamento)))
