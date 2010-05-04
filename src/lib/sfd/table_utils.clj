(ns lib.sfd.table-utils
  (:use lib.sfd.map-utils
	lib.sfd.str-utils
	lib.sfd.core
	clojure.contrib.seq-utils
	clojure.set))

;;;Table Utilities
;;;A table is defined to be a sequence of maps.  These functions perform
;;;Common operations on tables.
(defn freq
  "This function returns 1 regardless of inputs.  Very useful for determining a count with 
  pivot tables"
  [& ignored-params]
  1)
  
(defn multi-pivot
  "This function is designed to reudce a list of tuples to a single map. It is supposed to take a list of
  alternating mapping and reduction functions.  The resulting values are stored in a vector."
  [coll grouping-fn & fns]
     (if (even? (count fns))
       (let [reduce-help (fn [a-fn accum-val new-val] (a-fn accum-val new-val))
	     mapping-fns (map first (partition 2 fns))
	     reduction-fns (map second (partition 2 fns))
	     mapped-tuples (map #(hash-map (grouping-fn %) ((apply fn-tuple mapping-fns) %)) coll)]
	 (apply merge-with 
		(fn[accum-vec new-vec] (vec (map reduce-help reduction-fns accum-vec new-vec)))
		mapped-tuples))))

(defn pivot
  ([coll grouping-fn](pivot coll grouping-fn freq +))
  ([coll grouping-fn mapping-fn](pivot coll grouping-fn mapping-fn +))
  ([coll grouping-fn mapping-fn reduce-fn](map-vals first (multi-pivot coll grouping-fn mapping-fn reduce-fn))))

(defn inner-style
  [left-keys right-keys]
  (intersection (set left-keys) (set right-keys)))

(defn left-outer-style
  [left-keys right-keys]
  left-keys)

(defn right-outer-style
  [left-keys right-keys]
  right-keys)

(defn full-outer-style
  [left-keys right-keys]
  (union (set left-keys) (set right-keys)))

(defn join-worker
  "This is an internal method to be used in each join function."
  ([join-style left-coll right-coll join-fn] (join-worker join-style left-coll right-coll join-fn join-fn))
  ([join-style left-coll right-coll left-join-fn right-join-fn]
     (let [keys-a (keys (first left-coll)) ;The column names of coll-a
	   keys-b (keys (first right-coll)) ;The column names of coll-b
	   indexed-left (group-by left-join-fn left-coll) ;indexes the coll-a using join-fn-a
	   indexed-right (group-by right-join-fn right-coll) ;indexes the coll-b using join-fn-b
	   desired-joins (join-style (keys indexed-left) (keys indexed-right))]
       (reduce concat (map (fn [joined-value]
			     (for [left-side (get indexed-left joined-value [{}])
				   right-side (get indexed-right joined-value [{}])]
			       (merge left-side right-side)))
			   desired-joins)))))

(defmacro defjoin
  [join-name join-style doc-string]
  `(do
     (defn ~join-name
       ~(str doc-string
	     "\n  This function takes a left collection, a right collection, and at least one join function.  If only one
join function is provided, it is used on both the left & right hand sides.")
       ([~'left-coll ~'right-coll ~'join-fn]
	  (join-worker ~join-style ~'left-coll ~'right-coll ~'join-fn ~'join-fn))
       ([~'left-coll ~'right-coll ~'left-join-fn ~'right-join-fn]
	  (join-worker ~join-style ~'left-coll ~'right-coll ~'left-join-fn ~'right-join-fn)))))

(defjoin inner-join inner-style 
  "This is for performing inner joins.  The join value must exist in both lists.")
(defjoin left-outer-join left-outer-style 
  "This is for performing left outer joins.  The join value must exist in the left hand list.")
(defjoin right-outer-join right-outer-style 
  "This is for performing right outer joins.  The join value must exist in the right hand list.")
(defjoin full-outer-join full-outer-style
  "This is for performing full outer joins.  The join value may exist in either list.")

(defn natural-join
  "Performs the natural join.  If there are no keys that intersect, the join is not performed."
  [left-coll right-coll]
  (let [intersect (apply intersection
			 (map (comp set keys first)
			      [left-coll right-coll]))]
    (if (empty? intersect)
      []
      (inner-join left-coll right-coll (apply fn-tuple intersect)))))
	
(defn cross-join
  "DAMN CLOJURE IS AWESOME"
  [left-coll right-coll]
  (inner-join left-coll right-coll (constantly 1)))
