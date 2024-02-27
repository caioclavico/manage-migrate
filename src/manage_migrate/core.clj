(ns manage-migrate.core
  (:require
   [clojure.tools.logging :as log]
   [manage-migrate.util :as util]
   [clojure.java.io :as io]
   [clojure.string :as str]))

(def default-parent-dir "resources/")
(def default-dir "migrates")

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
        :when (util/extensao-valida? nome-arquivo)]
    (slurp arquivo)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (log/info "Hello, World!"))
