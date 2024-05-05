(ns mba-fiap.service.pagamento
  (:import
    (mba_fiap.repository.repository
      Repository)))


(defn buscar-por-id-pedido
  [^Repository repository id-pedido]
  {:pre [(instance? Repository repository)]}
  (let [result (.buscar repository id-pedido)]
    (if (empty? result)
      {:error "Pagamento não encontrado"}
      result)))
