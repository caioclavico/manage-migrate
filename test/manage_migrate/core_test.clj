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
    (core/busca-ou-cria-diretorio "test/" "diretorio_test")
    (spit "test/diretorio_test/0001-migrate.sql" "arquivo teste 0001")
    (spit "test/diretorio_test/0002-migrate.sql" "arquivo teste 0002")
    (spit "test/diretorio_test/0003-migrate.sql" "arquivo teste 0003")
    (is (= "arquivo teste 0001" (slurp "test/diretorio_test/0001-migrate.sql")))
    (is (= "arquivo teste 0002" (slurp "test/diretorio_test/0002-migrate.sql")))
    (is (= "arquivo teste 0003" (slurp "test/diretorio_test/0003-migrate.sql")))

    (testing "[OK] - Cria e verifica migrações"
      (doall (core/cria-migracao "test/" "diretorio_test"))
      (is (not-empty (deref core/atom-migrates)))
      (is (= {:migrates {"0001" ["arquivo teste 0001;"]
                         "0002" ["arquivo teste 0002;"]
                         "0003" ["arquivo teste 0003;"]}}
             (deref core/atom-migrates)))
      (reset! core/atom-migrates {}))

    (testing "[ERRO] - Falha ao criar migrações com arquivo repetido."
      (spit "test/diretorio_test/0002-migrate-repetido.sql" "arquivo repetido teste 0002")
      (is (= "arquivo repetido teste 0002" (slurp "test/diretorio_test/0002-migrate-repetido.sql")))
      (is (thrown? Exception #"Arquivo repetido!" (core/cria-migracao "test/" "diretorio_test")))
      (is (not-empty (deref core/atom-migrates)))
      (is (= {:migrates {"0001" ["arquivo teste 0001;"]}}
             (deref core/atom-migrates))))

    (testing "[OK] - Cria migrações após remover arquivo repetido."
      ;; removendo arquivo repetido
      (let [migrate-dir (core/busca-diretorio "test/" "diretorio_test")]
        (.delete (io/file migrate-dir "0002-migrate-repetido.sql")))
      ;; rodando a criacao de migração novamente
      (doall (core/cria-migracao "test/" "diretorio_test"))
      (is (not-empty (deref core/atom-migrates)))
      (is (= {:migrates {"0001" ["arquivo teste 0001;"]
                         "0002" ["arquivo teste 0002;"]
                         "0003" ["arquivo teste 0003;"]}}
             (deref core/atom-migrates))))

    (testing "[OK] - verifica se os migrates inicia de onde parou."
      ;; verifica se os migrates que ja foram rodados esta la!
      (is (= {:migrates {"0001" ["arquivo teste 0001;"]
                         "0002" ["arquivo teste 0002;"]
                         "0003" ["arquivo teste 0003;"]}}
             (deref core/atom-migrates)))

      ;; modifica um arquivo de migrate que ja foi rodado.
      (spit "test/diretorio_test/0001-migrate.sql" "arquivo modificado!")
      (is (= "arquivo modificado!" (slurp "test/diretorio_test/0001-migrate.sql")))

      ;; cria um novo arquivo para rodar a criação dos migrates novamente.
      (spit "test/diretorio_test/0004-migrate.sql" "arquivo teste 0004")
      (is (= "arquivo teste 0004" (slurp "test/diretorio_test/0004-migrate.sql")))

      ;; roda o cria-migracao!
      (doall (core/cria-migracao "test/" "diretorio_test"))

      ;; o arquivo modificado nao foi substituido conforme esperado.
      (is (= {:migrates {"0001" ["arquivo teste 0001;"]
                         "0002" ["arquivo teste 0002;"]
                         "0003" ["arquivo teste 0003;"]
                         "0004" ["arquivo teste 0004;"]}}
             (deref core/atom-migrates))))

    (testing "[OK] - Verifica se os arquivos de teste foram excluidos."
      (let [migrate-dir (core/busca-diretorio "test/" "diretorio_test")]
        (.delete (io/file migrate-dir "0001-migrate.sql"))
        (.delete (io/file migrate-dir "0002-migrate.sql"))
        (.delete (io/file migrate-dir "0003-migrate.sql"))
        (.delete (io/file migrate-dir "0004-migrate.sql"))
        (.delete (io/file migrate-dir))
        (reset! core/atom-migrates {})
        (is (nil? (core/busca-diretorio "test/" "diretorio_test")))
        (is (= {} (deref core/atom-migrates)))))))

(deftest verifica-novos-migrates-test
  (testing "[OK] - Verifica se existe novos migrates para rodar."
    (core/busca-ou-cria-diretorio "test/" "diretorio_test")
    (let [migrate-dir (core/busca-diretorio "test/" "diretorio_test")]
      (spit "test/diretorio_test/0001-migrate.sql" "arquivo teste 0001")
      (is (thrown?
           Exception
           #"Novos migrates, rode o 'crie-migrate'!"
           (core/verifica-novos-migrates migrate-dir)))

      ;; roda o cria-migracao!
      (doall (core/cria-migracao "test/" "diretorio_test"))

      (is (= {:migrates {"0001" ["arquivo teste 0001;"]}}
             (deref core/atom-migrates)))

      (is (nil? (core/verifica-novos-migrates migrate-dir)))))

  (testing "[OK] - Verifica se os arquivos de teste foram excluidos."
      (let [migrate-dir (core/busca-diretorio "test/" "diretorio_test")]
        (.delete (io/file migrate-dir "0001-migrate.sql"))
        (.delete (io/file migrate-dir))
        (is (nil? (core/busca-diretorio "test/" "diretorio_test"))))))
