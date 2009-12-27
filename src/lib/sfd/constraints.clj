(ns lib.sfd.constraints
  (:use lib.sfd.core))

(defn pre-constraint
  "Takes a predicates and generates a pre closure that applies
a pre constraint.  It defaults to applying it to the last value,
but the constraint takes an optional integer to specify the index
to apply the constraint to.  The index may be negative to specify
indexing from the end of the list."
  ([pred] (pre-constraint pred -1))
  ([pred idx]
     (fn[& args]
       {:pre [(pred (hof-target args idx))]}
       (let [h-args (hof-args args)
	     f (first h-args)
	     a (rest h-args)]
	 (apply f a)))))

(defn post-constraint
  "Generates a closure just like pre-constaint, except it is a
post condidtion.  The default is to apply the predicate to the
fn result."
  [pred]
  (fn[& args]
    {:post [(if (integer? (first args))
	      (pred (hof-target args))
	      (pred %))]}
    (let [h-args (hof-args args)
	  f (first h-args)
	  a (rest h-args)]
      (apply f a))))