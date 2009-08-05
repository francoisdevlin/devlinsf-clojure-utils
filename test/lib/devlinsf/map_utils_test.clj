(ns lib.devlinsf.test-contrib.map-utils
    (:use clojure.contrib.test-is
          lib.devlinsf.str-utils
	  lib.devlinsf.map-utils))

(def test-left [{:name "Sean" :age 27}
		{:name "Ross" :age 27}
		{:name "Brian" :age 22}])

(def test-right [{:owner "Sean" :item "Beer"} 
		 {:owner "Sean" :item "Pizza"}
		 {:owner "Ross" :item "Computer"}
		 {:owner "Matt" :item "Bike"}])

(def test-hash {:owner "Sean" :item "Beer"})

(deftest test-map-vals
  (is (= (map-vals upcase test-hash) 
	 {:owner "SEAN" 
	  :item "BEER"})))

(deftest test-map-keys
  (is (= (map-keys (comp str-rest str) test-hash) 
	 {"owner" "Sean"
	  "item" "beer"})))

(deftest test-filter-map
  (is (= (filter-map (comp #{:owner} first) test-hash)
	 {:owner "Sean"})))

(deftest test-remove-map
  (is (= (remove-map (comp #{:owner} first) test-hash)
	 {:item "Beer"})))

(deftest test-freq
  (is (= (freq) 1)))

(deftest test-pivot
  (is (= (pivot test-right :owner freq +)
	 {"Sean" [2]
	  "Ross" [1]
	  "Matt" [1]})))

(deftest test-trans
  (is (= ((trans :name #(% :owner) :count count) test-hash)
	 {:name "Sean"
	  :owner "Sean"
	  :item "Beer"
	  :count 2})))

(deftest test-proj
  (is (= ((proj :owner :item count) test-hash)
	 ["Sean" "Beer" 2])))

