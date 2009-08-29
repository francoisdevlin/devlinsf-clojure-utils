(ns lib.devlinsf.test-contrib.map-utils
    (:use clojure.contrib.test-is
          lib.devlinsf.str-utils
	  lib.devlinsf.map-utils))

(def test-left [{:name "Sean" :age 27}
		{:name "Ross" :age 27}
		{:name "Brian" :age 22}])

(def test-right [{:owner "Sean" :item "Beer" :cost 2.75} 
		 {:owner "Sean" :item "Pizza" :cost 10.99}
		 {:owner "Ross" :item "Computer" :cost 999.00}
		 {:owner "Matt" :item "Bike" :cost 2500.00}])

;(def test-hash {:owner "Sean" :item "Beer"})
(def test-hash {"a" 1 "b" 2 "c" 3})

(deftest test-map-vals
  (is (= (map-vals #(* 2 %) test-hash) 
	 {"a" 2 "b" 4 "c" 6})))

(deftest test-map-keys
  (is (= (map-keys keyword test-hash) 
	 {:a 1 :b 2 :c 3}))
  (is (= (map-keys (constantly :collide) + test-hash) 
	 {:collide 6})))

(deftest test-filter-map
  (is (= (filter-map (comp #{"a"} first) test-hash)
	 {"a" 1})))

(deftest test-remove-map
  (is (= (remove-map (comp #{"a"} first) test-hash)
	 {"b" 2 "c" 3})))

(deftest test-trans
  (is (= ((trans :count count) test-hash)
	 {"a" 1 
	  "b" 2
	  "c" 3
	  :count 3})))

(deftest test-freq
  (is (= (freq) 1)))

(deftest test-pivot
  (is (= (pivot test-right :owner freq +)
	 {"Sean" [2]
	  "Ross" [1]
	  "Matt" [1]})))

(deftest test-proj
  (is (= ((proj :owner :item count) test-hash)
	 ["Sean" "Beer" 2])))

