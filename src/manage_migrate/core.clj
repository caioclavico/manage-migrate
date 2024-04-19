(ns manage-migrate.core
  (:require
   [clojure.tools.logging :as log]
   [manage-migrate.util :as util]
   [clojure.java.io :as io]))

(def default-parent-dir "resources/")
(def default-dir "migrates")

(def atom-migrates (atom {}))

(defn busca-diretorio [parent-dir dir]
  (let [migration-dir (io/file parent-dir dir)]
    (if (.exists migration-dir)
      migration-dir
      (let [implicit-migrate-dir (io/file dir)]
        (when (.exists implicit-migrate-dir)
          implicit-migrate-dir)))))

(defn busca-ou-cria-diretorio [parent-dir dir]
  (if-let [migration-dir (busca-diretorio parent-dir dir)]
    migration-dir
    (let [new-migration-dir (io/file parent-dir dir)]
      (io/make-parents new-migration-dir ".")
      new-migration-dir)))

(defn busca-arquivos [migration-dir]
  (for [arquivo (filter #(.isFile %) (file-seq migration-dir))
        :let [nome-arquivo (.getName arquivo)]
        :when (util/parse-nome-arquivo nome-arquivo)]
    arquivo))

(defn cria-migracao
  ([]
   (cria-migracao default-dir))
  ([dir]
   (cria-migracao default-parent-dir dir))
  ([parent-dir dir]
   (let [migrate-dir (busca-ou-cria-diretorio parent-dir dir)
         arquivos (sort (busca-arquivos migrate-dir))
         n-arquivos (count arquivos)]
     (loop [i 0]
       (if (< i n-arquivos)
         (let [arquivo (nth arquivos i)
               conteudo (into [] (util/parse-conteudo-arquivo (slurp arquivo)))
               [id _nome _ext] (util/parse-nome-arquivo (.getName arquivo))]
           (if (util/arquivo-nao-repetido? arquivo arquivos)
             (do
               (swap! atom-migrates assoc-in [:migrates id] conteudo)
               (recur (inc i)))
             (throw
              (ex-info "Arquivo repetido!" {:arquivo (.getName arquivo)}))))
         (log/info "Criação dos migrates finalizada!"))))))

(defn -main
  "I don't do a whole lot ... yet."
  [& _args]
  (log/info "Hello, World!"))
