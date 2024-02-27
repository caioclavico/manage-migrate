(ns manage-migrate.util-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [manage-migrate.util :as util]))

(deftest extensao-valida?-test
  (testing "[OK] - extensão valida."
    (is (true? (util/extensao-valida? "0001-migrate.sql"))))
  (testing "[FAIL] - extensão inválida."
    (is (false? (util/extensao-valida? "0001-migrate.exe")))))
