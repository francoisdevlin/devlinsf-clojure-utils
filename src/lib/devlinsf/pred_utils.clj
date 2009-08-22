(ns lib.devlinsf.pred-utils)

(defn pred->bool
  "Evaulates a s-exp, and casts the result to a boolean"
  [s-exp]
  (if s-exp true false))

(defn every-pred?
  "Mimics AND
   Takes a list of predicates, and composes a new predicate.  Each predicate must 
   be true for the composite predicate to be true"
  [& predicates]
  (fn [item](every? #(% item) predicates)))

(defn any-pred?
  "Mimics OR
   Takes a list of predicates, and composes a new predicate.  Any predicate may 
   be true for the composite predicate to be true"
  [& predicates]
  (fn [item](if (some #(% item) predicates)
	      true
	      false)))

(defn some-pred?
  "Mimics some
   Takes a list of predicates, and composes a new predicate.  Any predicate may 
   be true for the composite predicate to be true"
  [& predicates]
  (fn [item](some #(% item) predicates)))

(defn map-pred
  "Takes a list of predicates as input.  Returns a fn that maps the true/false values of the predicate list
 for an item."
  [& predicates]
  (fn [item](map 
	     #(if (% item)
		true
		false) 
	     predicates)))

(defn match-map
  "This is used to create tuple filters.  The input is a map, and the output is a filter that
   requires every key in the input map to be present"
  [& params]
  (let [required-map (if (map? (first params))
		   (first params)
		   (apply hash-map (reduce concat (partition 2 params))))]
    (fn[input-map]
      (every? #(let [p-k (first %)
		     p-v (second %)
		     input-val (input-map p-k)]
		 (cond
		  (vector? p-v) (some (fn[element] (= input-val element)) p-v)
		  true  (= input-val p-v)))
	      required-map))))

(defn match-map-strict
  "This is used to create tuple filters.  The input is a map, and the output is a filter that
   requires every key in the input map to be present"
  [& params]
  (let [required-map (if (map? (first params))
		   (first params)
		   (apply hash-map (reduce concat (partition 2 params))))]
    (fn[input-map]
      (every? #(let [p-k (first %)
		     p-v (second %)
		     input-val (input-map p-k)]
		 (= input-val p-v))
	      required-map))))