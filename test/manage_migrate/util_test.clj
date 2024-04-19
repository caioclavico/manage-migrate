(ns manage-migrate.util-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [clojure.java.io :as io]
   [manage-migrate.core :as core]
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

(deftest arquivo-repetido?-test
  (testing "[OK] - Nenhum arquivo repetido encontrado."
    (core/busca-ou-cria-diretorio "test/" "diretorio_test")
    (spit "test/diretorio_test/0001-migrate.sql" "arquivo teste 0001")
    (spit "test/diretorio_test/0002-migrate.sql" "arquivo teste 0002")
    (let [arquivos (core/busca-arquivos (core/busca-diretorio "test/" "diretorio_test"))]
      (is (true? (util/arquivo-nao-repetido? "0001-migrate.sql" arquivos)))
      (is (true? (util/arquivo-nao-repetido? "0002-migrate.sql" arquivos)))
      (is (true? (util/arquivo-nao-repetido? (io/as-file "test/diretorio_test/0001-migrate.sql") arquivos)))
      (is (true? (util/arquivo-nao-repetido? (io/as-file "test/diretorio_test/0002-migrate.sql") arquivos)))))

  (testing "[FAIL] - Arquivo repetido encontrado."
    (spit "test/diretorio_test/0001-migrate-repetido.sql" "arquivo repetido teste 0001")
    (spit "test/diretorio_test/0002-migrate-repetido.sql" "arquivo repetido teste 0002")
    (let [arquivos (core/busca-arquivos (core/busca-diretorio "test/" "diretorio_test"))]
      (is (false? (util/arquivo-nao-repetido? "0001-migrate-repetido.sql" arquivos)))
      (is (false? (util/arquivo-nao-repetido? "0002-migrate-repetido.sql" arquivos)))
      (is (false? (util/arquivo-nao-repetido? (io/as-file "test/diretorio_test/0001-migrate-repetido.sql") arquivos)))
      (is (false? (util/arquivo-nao-repetido? (io/as-file "test/diretorio_test/0002-migrate-repetido.sql") arquivos)))))

  (testing "[OK] - Verifica se os arquivos de teste foram excluidos."
    (let [migrate-dir (core/busca-diretorio "test/" "diretorio_test")]
      (.delete (io/file migrate-dir "0001-migrate.sql"))
      (.delete (io/file migrate-dir "0002-migrate.sql"))
      (.delete (io/file migrate-dir "0001-migrate-repetido.sql"))
      (.delete (io/file migrate-dir "0002-migrate-repetido.sql"))
      (.delete (io/file migrate-dir))
      (is (nil? (core/busca-diretorio "test/" "diretorio_test"))))))
