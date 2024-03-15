(ns manage-migrate.core-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [manage-migrate.core :as core]
   [clojure.java.io :as io]))

(deftest busca-diretorio-test
  (testing "Busca diretório de migrações"
    (is (instance? java.io.File (core/busca-diretorio "test/" "manage_migrate")))
    (is (instance? java.io.File (core/busca-diretorio "banana/" "test")))
    (is (nil? (core/busca-diretorio "test/" "banana")))))

(deftest busca-ou-cria-diretorio-test
  (testing "Busca ou cria um diretório"
    (is (nil? (core/busca-diretorio "test/" "diretorio_test")))
    (is (instance? java.io.File (core/busca-ou-cria-diretorio "test/" "diretorio_test")))
    (is (instance? java.io.File (core/busca-diretorio "test/" "diretorio_test")))

    (testing "[OK] - Verifica se os arquivos de teste foram excluidos."
      (.delete (core/busca-diretorio "test/" "diretorio_test"))
      (is (nil? (core/busca-diretorio "test/" "diretorio_test"))))))

(deftest busca-arquivos-test
  (testing "Busca arquivos no diretório."
    (let [migrate-dir (core/busca-ou-cria-diretorio "test/" "diretorio_test")]
      (.createNewFile (io/file "test/diretorio_test" "0001-migrate.sql"))
      (is (not-empty (core/busca-arquivos migrate-dir)))
      (is (= 1 (count (core/busca-arquivos migrate-dir))))
      (.createNewFile (io/file "test/diretorio_test" "0002-migrate.sql"))
      (is (= 2 (count (core/busca-arquivos migrate-dir))))
      (is (true? (every? #(instance? java.io.File %) (core/busca-arquivos migrate-dir))))

      (testing "[OK] - Não retorna arquivos com extensão invalida."
        (.createNewFile (io/file "test/diretorio_test" "0003-migrate.exe"))
        (is (= 2 (count (core/busca-arquivos migrate-dir)))))

      (testing "[OK] - Verifica se os arquivos de teste foram excluidos."
        (.delete (io/file migrate-dir "0001-migrate.sql"))
        (.delete (io/file migrate-dir "0002-migrate.sql"))
        (.delete (io/file migrate-dir "0003-migrate.exe"))
        (.delete (io/file migrate-dir))
        (is (nil? (core/busca-diretorio "test/" "diretorio_test")))))))

(deftest cria-migracao-test
  (testing "[SETUP] - Cria arquivos para teste"
    (spit "test/diretorio_test/0001-migrate.sql" "arquivo teste 0001")
    (spit "test/diretorio_test/0002-migrate.sql" "arquivo teste 0002")
    (is (= "arquivo teste 0001" (slurp "test/diretorio_test/0001-migrate.sql")))
    (is (= "arquivo teste 0002" (slurp "test/diretorio_test/0002-migrate.sql")))

    (testing "[OK] - Cria e verifica migracoes"
      (doall (core/cria-migracao "test/" "diretorio_test"))
      (is (not-empty (deref core/atom-migrates)))
      (is (= {:migrates {"0001" ["arquivo teste 0001;"], "0002" ["arquivo teste 0002;"]}}
             (deref core/atom-migrates))))

    (testing "[OK] - Verifica se os arquivos de teste foram excluidos."
      (let [migrate-dir (core/busca-diretorio "test/" "diretorio_test")]
        (.delete (io/file migrate-dir "0001-migrate.sql"))
        (.delete (io/file migrate-dir "0002-migrate.sql"))
        (.delete (io/file migrate-dir))
        (is (nil? (core/busca-diretorio "test/" "diretorio_test")))))))
