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
  (are
   (let [expected-string "Clojure Is Awesome"]
     (= (titleize _1) expected-string))
   "clojure is awesome"
   "clojure   is  awesome"
   "CLOJURE IS AWESOME"
   "clojure-is-awesome"
   "clojure- _ is---awesome"
   "clojure_is_awesome"))

(deftest test-camelize
  (are
   (let [expected-string "clojureIsAwesome"]
     (= (camelize _1) expected-string))
   "clojure is awesome"
   "clojure   is  awesome"
   "CLOJURE IS AWESOME"
   "clojure-is-awesome"
   "clojure- _ is---awesome"
   "clojure_is_awesome"))

(deftest test-underscore
  (are
   (let [expected-string "clojure_is_awesome"]
     (= (underscore _1) expected-string))
   "clojure is awesome"
   "clojure   is  awesome"
   "CLOJURE IS AWESOME"
   "clojure-is-awesome"
   "clojure- _ is---awesome"
   "clojure_is_awesome"))

(deftest test-dasherize
  (are
   (let [expected-string "clojure-is-awesome"]
     (= (dasherize _1) expected-string))
   "clojure is awesome"
   "clojure   is  awesome"
   "CLOJURE IS AWESOME"
   "clojure-is-awesome"
   "clojure- _ is---awesome"
   "clojure_is_awesome"))

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
    (is (= (re-split source-string {:pattern #"\n" :length 1}) '("1\t2\t3")))
    (is (= (re-split source-string {:pattern #"\n" :offset 1}) '("4\t5\t6")))
    (is (= (re-split source-string {:pattern #"\n" :marshal-fn #(str % "\ta")}) '("1\t2\t3\ta" "4\t5\t6\ta")))
    (is (= (re-split source-string {:pattern #"\n" :length 1 :marshal-fn #(str % "\ta")}) '("1\t2\t3\ta")))
    ))

(deftest test-re-split-single-element-list
  (let [source-string "1\t2\t3\n4\t5\t6"]
    (is (= (re-split source-string (list #"\n")) '("1\t2\t3" "4\t5\t6")))))

(deftest test-re-split-pure-list
  (let [source-string "1\t2\t3\n4\t5\t6"]
    (is (= (re-split source-string (list #"\n" #"\t")) '(("1" "2" "3") ("4" "5" "6"))))))

(deftest test-re-split-mixed-list
  (let [source-string "1\t2\t3\n4\t5\t6"]
    (is (= (re-split source-string (list {:pattern #"\n" :length 1} #"\t")) '(("1" "2" "3"))))
    (is (= (re-split source-string (list {:pattern #"\n" :length 1} {:pattern #"\t" :offset 1 :length 2})) '(("2" "3"))))
    (is (= (re-split source-string (list 
				    {:pattern #"\n" :length 1} 
				    {:pattern #"\t" :length 2 :marshal-fn #(java.lang.Double/parseDouble %)}))
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

(deftest test-swap-letters
  (are (= (swap-letters _1) _2)
       "ab" '("ba")
       "abc" '("bac" "acb")))

(deftest test-try-letters
  (is (= (try-letter "a" "dog")) '("adog" "aog" "daog" "dag" "doag" "doa" "doga")))

;;;The code for the test-singularize function was based on functions contributed by Brian Doyle
(deftest test-singularize 
  (are (= _1 (singularize _2))
    "foo" "foos"
    "baby" "babies"
    "beach" "beaches"
    "box" "boxes"
    "bush" "bushes"
    "bus" "buses"
    "stop" "stops"))

;;;The code for the test-pluralize function was based on functions contributed by Brian Doyle 
(deftest test-pluralize 
  (are (= _1 (pluralize _2))
       "foos" "foo"
       "beaches" "beach"
       "babies" "baby"
       "boxes" "box"
       "bushes" "bush"
       "buses" "bus"
       "stops" "stop"))

(deftest test-str-rest 
  (are (= _1 (str-rest _2))
       "beer" (str :beer)
       "eer" "Beer"
       "" "B"
       "" ""
       "" '()))

(deftest test-str-take
  (let [source-string "Be er"]
    (are 
     (= (str-take _1 source-string) _2)
     2 "Be"
     10 "Be er"
     #"\r" "Be er"
     #"\s+" "Be")
    (is (= (str-take #"\s+" source-string {:include true}) "Be "))
    (is (= (str-take #"\s+" source-string {:include false}) "Be"))
    (is (= (str-take 2 ["B" "e" "e" "r"]) "Be"))
    (is (= (str-take 2 ["B" "ee" "r"]) "Bee"))
    (is (= (str-take 2 []) ""))))

(deftest test-str-drop
  (let [source-string "Be er"]
    (are (= (str-drop _1 source-string) _2)
	 2 " er"
	 10 ""
	 #"\r" ""
	 #"\s+" "er")
    (is (= (str-drop #"\s+" source-string {:include true}) " er"))
    (is (= (str-drop #"\s+" source-string {:include false}) "er"))
    (is (= (str-drop 2 ["B" "e" "e" "r"]) "er"))
    (is (= (str-drop 2 ["B" "ee" "r"]) "r"))
    (is (= (str-drop 2 []) ""))))