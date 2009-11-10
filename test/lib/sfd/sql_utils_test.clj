(ns lib.sfd.test-contrib.sql-utils
    (:use clojure.contrib.test-is
          lib.sfd.sql-utils))

(deftest test-keyword-id
  (is (= (where-clause {:id 1}) "id=1")))

(deftest test-string-id
  (is (= (where-clause {"id" 1}) "id=1")))

(deftest test-keyword-in
  (is (= (where-clause {:id [1 2]}) "id IN (1, 2)")))

(deftest test-sql-and
  (is (= (where-clause {:id 1 :name "Sean"}) "id=1 AND name=\"Sean\"")))

(deftest test-array-in
  (is (= (where-clause {:name ["Sean" "Bill"]}) "name IN (\"Sean\", \"Bill\")")))
