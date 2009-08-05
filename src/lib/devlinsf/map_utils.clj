(ns lib.devlinsf.map-utils
  (:use lib.devlinsf.str-utils
	clojure.contrib.seq-utils
	clojure.set))

(defn list-to-map
  [& params]
  (apply hash-map 
	 (reduce concat (partition 2 params))))

(defn- find-params
  [& input-list]
  (if (= (class (first input-list)) java.lang.String)
		     (rest input-list)
		     input-list))

(defn- find-doc-string
  [& input-list]
  (if (= (class (first input-list)) java.lang.String)
    (first input-list)))

(defn filter-nil-vals
  [input-map]
  (apply merge {} 
	 (map #(apply hash-map %) 
	      (filter second input-map))))

(defn render-map
  [input-map kv-delimiter entry-delimiter]
  (str-join entry-delimiter 
	    (map #(str (first %) kv-delimiter (second %)) input-map)))

(defn- transform-helper
  "This is a helper fn for the the transform macro.  It allows maps to be passes in instaed of functions.
  If a map is passed, it is applied to the key.  The thought is that the maps is a decoder.
  If a fn is passed, it is applied to the map.
  If anything else is passed, it is assoc'd with the key"
  [data-map]
  (fn [fn-entry]
    (let [fn-key (first fn-entry)
	  fn-val (second fn-entry)]
      (list
       fn-key
       (cond
	(map? fn-val) (fn-val (data-map fn-key))
	(fn? fn-val) (fn-val data-map)
	true fn-val)))))

(defn trans
  [& params]
  (let [transform-map (if (map? (first params))
			(first params)
			(apply list-to-map params))]
    (fn [input-map]
      (apply assoc input-map
	     (reduce concat 
		     (map (transform-helper input-map) transform-map))))))

(defmacro deftrans
  [name & input-keys]
  (let [transform-symbol name
	param-list (apply find-params input-keys)
	doc-string? (str (apply find-doc-string input-keys))
	input-map-symbol (gensym "input-map_")]
    `(do
       (defn ~transform-symbol ~doc-string? [~input-map-symbol]
	 ((trans ~@param-list) ~input-map-symbol)))))

(defn proj
  "Takes a list of input functions to be applied to a map.  The result is a closure."
  [& params]
  (fn [input-map] (vec (map #(% input-map) params))))

(defmacro defproj
  [name & input-keys]
  (let [proj-symbol name
	param-list (apply find-params input-keys)
	doc-string? (str (apply find-doc-string input-keys))
	input-map-symbol (gensym "input-map_")]
    `(do
       (defn ~proj-symbol ~doc-string? [~input-map-symbol]
	 ((proj ~@param-list) ~input-map-symbol)))))


(defn cat-proj
  "This function takes a collection of proj closures and returns a clojure that eagerly concatentates them."
  [& proj-coll]
  (fn [input-map] (vec (reduce concat (map #(% input-map) proj-coll)))))

(defn map-vals
  [f coll] 
  (apply merge (map (fn[[k v]] { k (f v)}) coll)))

(defn map-keys
  [f coll] 
  (apply merge (map (fn[[k v]] { (f k) v}) coll)))

(defn filter-map
  "This is a specialized form of filter.  It is designed to transform the resulting seq into a hash-map."
  [pred a-map]
  (apply merge 
	 (map  
	  #(apply hash-map %) 
	  (filter pred a-map))))

(defn remove-map
  "This is a specialized form of remove.  It is designed to transform the resulting seq into a hash-map."
  [pred a-map]
  (apply merge 
	 (map  
	  #(apply hash-map %) 
	  (remove pred a-map))))

(defn freq
  "This function returns 1 regardless of inputs.  Very useful for determining a count with 
  pivot tables"
  [& ignored-params]
  1)
  
(defn pivot
  "This function is designed to reudce a list of tuples to a single map. It is supposed to take a list of
  alternating mapping and reduction functions.  The resulting values are stored in a vector.

  If no functions are provided, it uses freq as the mapping fn and + as the reduction fn."
  ([coll grouping-fn](pivot coll grouping-fn freq +))
  ([coll grouping-fn & fns]
     (if (even? (count fns))
       (let [reduce-help (fn [a-fn accum-val new-val] (a-fn accum-val new-val))
	     mapping-fns (map first (partition 2 fns))
	     reduction-fns (map second (partition 2 fns))
	     mapped-tuples (map #(hash-map (grouping-fn %) ((apply proj mapping-fns) %)) coll)]
	 (apply merge-with 
		(fn[accum-vec new-vec] (vec (map reduce-help reduction-fns accum-vec new-vec)))
		mapped-tuples)))))

(defn single-pivot
  ([coll grouping-fn](single-pivot coll grouping-fn freq +))
  ([coll grouping-fn & fns](map-vals first (apply pivot coll grouping-fn fns))))

(defn- merge-like
  "This is an internal function so that merge with behaves like merge.  It is the default for marshall hashmap."
  [accum current]
  current)

(defn marshall-hashmap
  "This is a function designed to marsh a hash-map from a collection.  Very handy to combine with a 
  parser.  The defaults are

  key-fn: first
  val-fn: second
  merge-fn: merge-like

  They are progressively replaced as the arity increases."
  ([coll] (marshall-hashmap coll first second merge-like))
  ([coll key-fn] (marshall-hashmap coll key-fn second merge-like))
  ([coll key-fn val-fn] (marshall-hashmap coll key-fn val-fn merge-like))
  ([coll key-fn val-fn merge-fn]
     (apply merge-with merge-fn
	    (map (fn [entry] {(key-fn entry) (val-fn entry)}) coll))))

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
  ([join-style coll-a coll-b join-fn-a] (join-worker join-style coll-a coll-b join-fn-a join-fn-a))
  ([join-style coll-a coll-b join-fn-a join-fn-b]
     (let [keys-a (keys (first coll-a)) ;The column names of coll-a
	   keys-b (keys (first coll-b)) ;The column names of coll-b
	   indexed-a (group-by join-fn-a coll-a)
	   indexed-b (group-by join-fn-b coll-b)
	   desired-joins (join-style (keys indexed-a) (keys indexed-b))]
       (reduce concat (map (fn [joined-value]
			     (for [left-side (get indexed-a joined-value [{}])
				   right-side (get indexed-b joined-value [{}])]
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

(defjoin inner-join inner-style "This is for performing inner joins.  The join value must exist in both lists.")
(defjoin left-outer-join left-outer-style "This is for performing left outer joins.  The join value must exist in the left hand list.")
(defjoin right-outer-join right-outer-style "This is for performing right outer joins.  The join value must exist in the right hand list.")
(defjoin full-outer-join full-outer-style "This is for performing full outer joins.  The join value may exist in either list.")

(defn natural-join
  "Performs the natural join.  If there are no keys that intersect, the join is not performed."
  [left-coll right-coll]
  (let [left-keys (keys (first left-coll))
	right-keys (keys (first right-coll))
	intersect (intersection (set left-keys) (set right-keys))]
    (if (empty? intersect)
      []
      (inner-join left-coll right-coll (apply proj intersect)))))
	
(defn cross-join
  "DAMN CLOJURE IS AWESOME"
  [left-coll right-coll]
  (inner-join left-coll right-coll (constantly 1)))
