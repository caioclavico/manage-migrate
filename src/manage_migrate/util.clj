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

(defn arquivo-nao-repetido? [arquivo todos-arquivos]
  (let [arquivo (if (string? arquivo) arquivo (.getName arquivo))
        id (first (parse-nome-arquivo arquivo))
        todos-ids (map #(first (parse-nome-arquivo (.getName %))) todos-arquivos)]
    (= 1 (get (frequencies todos-ids) id))))
