(ns mba-fiap.service.pagamento
  (:require
    [mba-fiap.base.validation :as validation]
    [mba-fiap.model.pagamento :as pagamento])
  (:import
    (mba_fiap.repository.repository
      Repository)))


(defn criar-pagamento
  [^Repository repository pagamento]
  (let [pagamento (.criar repository pagamento)]
    pagamento))


(defn atualizar-status-pagamento
  [^Repository repository id-pagamento status]
  {:pre [(instance? Repository repository)
         (uuid? id-pagamento)
         (validation/schema-check pagamento/Status status)]}
  (let [pagamento (.buscar repository id-pagamento)
        pagamento (assoc pagamento :status status)
        pagamento (.atualizar repository pagamento)]
    pagamento))


(defn buscar-por-id-pedido
  [^Repository repository id-pedido]
  {:pre [(instance? Repository repository)]}
  (let [pagamento (.listar repository id-pedido)]
    (if (empty? pagamento)
      {:error "Pagamento não encontrado"}
      pagamento)))
