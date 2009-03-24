(ns devlinsf.test-contrib.str-utils
    (:use clojure.contrib.test-is
          devlinsf.str-utils))

(deftest test-str-reverse
  (is (= (str-reverse "Clojure") "erujolC")))

(deftest test-downcase
  (is (= (downcase "Clojure") "clojure")))

(deftest test-upcase
  (is (= (upcase "Clojure") "CLOJURE")))

(deftest test-trim
  (is (= (trim "  Clojure  ") "Clojure")))

(deftest test-strip
  (is (= (strip "  Clojure  ") "Clojure")))

(deftest test-ltrim
  (is (= (ltrim "  Clojure  ") "Clojure  ")))

(deftest test-rtrim
  (is (= (rtrim "  Clojure  ") "  Clojure")))

(deftest test-chop
  (is (= (chop "Clojure") "Clojur")))

(deftest test-chomp
  (is (= (chomp "Clojure \n") "Clojure "))
  (is (= (chomp "Clojure \r") "Clojure "))
  (is (= (chomp "Clojure \n\r") "Clojure ")))

(deftest test-capitalize
  (is (= (capitalize "clojure") "Clojure")))

(deftest test-titleize
  (let [expected-string "Clojure Is Awesome"]
    (is (= (titleize "clojure is awesome") expected-string))
    (is (= (titleize "clojure   is  awesome") expected-string))
    (is (= (titleize "CLOJURE IS AWESOME") expected-string))
    (is (= (titleize "clojure-is-awesome") expected-string))
    (is (= (titleize "clojure- _ is---awesome") expected-string))
    (is (= (titleize "clojure_is_awesome") expected-string))))

(deftest test-camelize
  (let [expected-string "clojureIsAwesome"]
    (is (= (camelize "clojure is awesome") expected-string))
    (is (= (camelize "clojure   is  awesome") expected-string))
    (is (= (camelize "CLOJURE IS AWESOME") expected-string))
    (is (= (camelize "clojure-is-awesome") expected-string))
    (is (= (camelize "clojure- _ is---awesome") expected-string))
    (is (= (camelize "clojure_is_awesome") expected-string))))

(deftest test-underscore
  (let [expected-string "clojure_is_awesome"]
    (is (= (underscore "clojure is awesome") expected-string))
    (is (= (underscore "clojure   is  awesome") expected-string))
    (is (= (underscore "CLOJURE IS AWESOME") expected-string))
    (is (= (underscore "clojure-is-awesome") expected-string))
    (is (= (underscore "clojure- _ is---awesome") expected-string))
    (is (= (underscore "clojure_is_awesome") expected-string))))

(deftest test-dasherize
  (let [expected-string "clojure-is-awesome"]
    (is (= (dasherize "clojure is awesome") expected-string))
    (is (= (dasherize "clojure   is  awesome") expected-string))
    (is (= (dasherize "CLOJURE IS AWESOME") expected-string))
    (is (= (dasherize "clojure-is-awesome") expected-string))
    (is (= (dasherize "clojure- _ is---awesome") expected-string))
    (is (= (dasherize "clojure_is_awesome") expected-string))))

(deftest test-str-before
  (is (= (str-before "Clojure Is Awesome" #"Is") "Clojure ")))

(deftest test-str-before-inc
  (is (= (str-before-inc "Clojure Is Awesome" #"Is") "Clojure Is")))

(deftest test-str-after
  (is (= (str-after "Clojure Is Awesome" #"Is") " Awesome")))

(deftest test-str-after-inc
  (is (= (str-after-inc "Clojure Is Awesome" #"Is") "Is Awesome")))

(deftest test-str-join
  (is (= (str-join " " '("A" "B")) "A B")))

(deftest test-re-split-single-regex
  (let [source-string "1\t2\t3\n4\t5\t6"]
    (is (= (re-split source-string #"\n") '("1\t2\t3" "4\t5\t6")))))

(deftest test-re-split-single-map
  (let [source-string "1\t2\t3\n4\t5\t6"]
    (is (= (re-split source-string {:pattern #"\n"}) '("1\t2\t3" "4\t5\t6")))
    (is (= (re-split source-string {:pattern #"\n" :limit 1}) '("1\t2\t3")))
    (is (= (re-split source-string {:pattern #"\n" :marshal-fn #(str % "\ta")}) '("1\t2\t3\ta" "4\t5\t6\ta")))
    (is (= (re-split source-string {:pattern #"\n" :limit 1 :marshal-fn #(str % "\ta")}) '("1\t2\t3\ta")))
    ))

(deftest test-re-split-single-element-list
  (let [source-string "1\t2\t3\n4\t5\t6"]
    (is (= (re-split source-string (list #"\n")) '("1\t2\t3" "4\t5\t6")))))

(deftest test-re-split-pure-list
  (let [source-string "1\t2\t3\n4\t5\t6"]
    (is (= (re-split source-string (list #"\n" #"\t")) '(("1" "2" "3") ("4" "5" "6"))))))

(deftest test-re-split-mixed-list
  (let [source-string "1\t2\t3\n4\t5\t6"]
    (is (= (re-split source-string (list {:pattern #"\n" :limit 1} #"\t")) '(("1" "2" "3"))))
    (is (= (re-split source-string (list {:pattern #"\n" :limit 1} {:pattern #"\t" :limit 2})) '(("1" "2"))))
    (is (= (re-split source-string (list 
				    {:pattern #"\n" :limit 1} 
				    {:pattern #"\t" :limit 2 :marshal-fn #(java.lang.Double/parseDouble %)}))
	   '((1.0 2.0))))
    (is (= (re-split source-string (list 
				    {:pattern #"\n"} 
				    {:pattern #"\t" :marshal-fn #(java.lang.Double/parseDouble %)}))
	   '((1.0 2.0 3.0) (4.0 5.0 6.0))))
    (is (= (map #(reduce + %) (re-split source-string (list 
						       {:pattern #"\n"} 
						       {:pattern #"\t" :marshal-fn #(java.lang.Double/parseDouble %)})))
	   '(6.0 15.0)))
    (is (= (reduce +(map #(reduce + %) (re-split source-string (list 
								{:pattern #"\n"} 
								{:pattern #"\t" :marshal-fn #(java.lang.Double/parseDouble %)}))))
	   '21.0))
    ))

(deftest test-re-partition
  (is (= (re-partition "Clojure Is Awesome" #"\s+") '("Clojure" " " "Is" " " "Awesome"))))

(deftest test-re-gsub
  (let [source-string "1\t2\t3\n4\t5\t6"]
    (is (= (re-gsub source-string #"\s+" " ") "1 2 3 4 5 6"))
    (is (= (re-gsub source-string '((#"\s+" " "))) "1 2 3 4 5 6"))
    (is (= (re-gsub source-string '((#"\s+" " ") (#"\d" "D"))) "D D D D D D"))))
    
(deftest test-re-sub
  (let [source-string "1 2 3 4 5 6"]
    (is (= (re-sub source-string #"\d" "D") "D 2 3 4 5 6"))
    (is (= (re-sub source-string '((#"\d" "D") (#"\d" "E"))) "D E 3 4 5 6"))))