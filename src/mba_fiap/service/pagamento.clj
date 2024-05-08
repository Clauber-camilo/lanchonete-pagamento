(ns mba-fiap.service.pagamento
  (:import
    (mba_fiap.repository.repository
      Repository)))


(defn criar-pagamento
  [^Repository repository pagamento]
  {:pre [(instance? Repository repository)]}
  (let [pagamento (.criar repository pagamento)]
    pagamento))


(defn atualizar-status-pagamento
  [^Repository repository id-pedido status]
  {:pre [(instance? Repository repository)
         (uuid? id-pedido)]}
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
