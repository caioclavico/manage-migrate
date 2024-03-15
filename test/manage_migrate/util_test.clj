(ns manage-migrate.util-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [manage-migrate.util :as util]))

(deftest extensao-valida?-test
  (testing "[OK] - extensão valida."
    (is (true? (util/extensao-valida? "0001-migrate.sql"))))
  (testing "[FAIL] - extensão inválida."
    (is (false? (util/extensao-valida? "0001-migrate.exe")))))

(deftest parse-nome-arquivo-test
  (testing "[OK] - arquivo com estensão valida retorna [id nome ext]."
    (is (= ["0001" "migrate" ".sql"] (util/parse-nome-arquivo "0001-migrate.sql")))
    (is (= ["0002" "migrate" ".sql"] (util/parse-nome-arquivo "0002-migrate.sql")))
    (is (= ["1" "nome" ".sql"] (util/parse-nome-arquivo "1-nome.sql"))))
  (testing "[FAIL] - retorna nil caso o nome do arquivo seja inválido"
    (is (nil? (util/parse-nome-arquivo "migrate")))
    (is (nil? (util/parse-nome-arquivo "migrate.sql")))
    (is (nil? (util/parse-nome-arquivo "0001-migrate")))
    (is (nil? (util/parse-nome-arquivo "0001-migrate.exe")))
    (is (nil? (util/parse-nome-arquivo "numero-migrate.exe")))))

(deftest parse-conteudo-arquivo-test
  (testing "[OK] - conteudo do arquivo formatado."
    (is (= ["CREATE TABLE IF NOT EXISTS manage.migrates ( id text, conteudo text, PRIMARY KEY ((id)));"]
           (util/parse-conteudo-arquivo
            "CREATE TABLE IF NOT EXISTS manage.migrates (\n    id text,\n    conteudo text,\n   PRIMARY KEY ((id)));\n")))
    (is (= ["CREATE TABLE IF NOT EXISTS manage.migrates ( id text, conteudo text, PRIMARY KEY ((id)));"
            "DROP TABLE manage.migrates;"]
           (util/parse-conteudo-arquivo
            "CREATE TABLE IF NOT EXISTS manage.migrates (\n    id text,\n    conteudo text,\n   PRIMARY KEY ((id)));\n
             DROP TABLE manage.migrates;\n")))))
