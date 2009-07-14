(ns lib.devlinsf.sort-utils)

(defn chain-comp
  "This takes a list of comparator functions, and chains them together.  
  It returns another comparator.  It behaves similar to comp, in that
  comparisons are done right to left."
  [& comps]
  (fn [a b]
    (loop [remaining-comps (reverse comps)]
      (let [iter-comp (first remaining-comps)
            iter-result (iter-comp a b)]
        (if (and (zero? iter-result) (next remaining-comps))
          (recur (rest remaining-comps))
          iter-result)))))
 
(defn <=>
  "This creates a comparator that wraps the mapping fn f."
  [f]
  (fn [a b]
    (compare (f a) (f b))))
 
(defn -<=>
  "This creates an inverse comparator that wraps the mapping fn f."
  [f]
  (fn [a b]
    (compare (f b) (f a))))
 
(defn inv-compare
  "This function takes a comparator f (with two inputs) as an input, and reverses it."
  [f]
  (fn [a b](f b a)))
 
(defn least
  "This finds the smallest item of a collection based on the comparator fn.  If no fn is passed,
   the items are compared by identity."
  ([coll] (least (<=> identity) coll))
  ([comp-fn coll] (first (sort comp-fn coll))))
 
(defn most
  "This finds the largest item of a collection based on the comparator fn.  If no fn is passed,
   the items are compared by identity."
  ([coll] (most (<=> identity) coll))
  ([comp-fn coll] (first (sort (inv-compare comp-fn) coll))))