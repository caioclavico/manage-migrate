(ns manage-migrate.util
  (:require
   [clojure.string :as st]))

(defn extensao-valida? [nome-arquivo]
  (-> (st/split nome-arquivo #"\.")
      last
      (= "sql")))
