(ns lib.devlinsf.predicate-utils.clj)

(defn predicate-chain
  "Mimics AND
   Takes a list of predicates, and composes a new predicate.  Each predicate must 
   be true for the composite predicate to be true"
  [& predicates]
  (fn [item](every? #(% item) predicates)))

(defn predicate-fan
  "Mimics OR
   Takes a list of predicates, and composes a new predicate.  Any predicate may 
   be true for the composite predicate to be true"
  [& predicates]
  (fn [item](some #(% item) predicates)))

(defn same-entries
  "This is used to create tuple filters.  The input is a map, and the output is a filter that
   requires every key in the input map to be present"
  [& params]
  (let [required-map (if (map? (first params))
		   (first params)
		   (apply hash-map (reduce concat (partition 2 params))))]
    (fn[input-map]
      (every? #(let [p-k (first %)
		     p-v (second %)]
		     (= (input-map p-k) p-v))
	      required-map))))