(ns mba-fiap.service.pagamento
  (:require
    [mba-fiap.base.validation :as validation]
    [mba-fiap.model.pagamento :as pagamento])
  (:import
    (mba_fiap.repository.repository
      Repository)))


(defn criar-pagamento
  [^Repository repository pagamento]
  {:pre [(instance? Repository repository)
         (validation/schema-check pagamento/Pagamento pagamento)]}
  (let [pagamento (.criar repository pagamento)]
    pagamento))


(defn atualizar-status-pagamento
  [^Repository repository id-pedido status]
  {:pre [(instance? Repository repository)
         (uuid? id-pedido)
         (validation/schema-check pagamento/Pagamento status)]}
  (let [data {:id-pedido id-pedido :status status}
        pagamento (.atualizar repository data)]
    pagamento))


(defn buscar-por-id-pedido
  [^Repository repository id-pedido]
  {:pre [(instance? Repository repository)]}
  (let [result (.buscar repository id-pedido)]
    (if (empty? result)
      {:error "Pagamento não encontrado"}
      result)))
