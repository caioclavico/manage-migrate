(ns manage-migrate.util
  (:require
   [clojure.string :as st]))

(defn extensao-valida? [nome-arquivo]
  (-> (st/split nome-arquivo #"\.")
      last
      (= "sql")))

(defn parse-nome-arquivo [nome-arquivo]
  (when (extensao-valida? nome-arquivo)
    (next (re-matches #"^(\d+)-([^\.]+)((?:\.[^\.]+)+)$" nome-arquivo))))

(defn parse-conteudo-arquivo [conteudo]
  (->> (-> conteudo
           (st/replace #"\n" "")
           (st/replace #"\s+" " ")
           (st/split #"\;"))
       (map #(st/trim (str % ";")))))
