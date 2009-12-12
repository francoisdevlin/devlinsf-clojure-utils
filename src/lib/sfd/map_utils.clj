(ns lib.sfd.map-utils
  (:use lib.sfd.core))

(defn trans
  "trans(form) is used to assoc values to an existing map.  Specifically, when the new values depend on values already existing in the map.

  This is similar to CL's let, the map does not have any information assoc'd until the end."
  [& params]
  (fn[a-map]
    (reduce 
     (fn[accum entry]
       (let [k (first entry)
	     v (second entry)]
	 (assoc accum k (v a-map))))
     a-map
     (partition 2 params))))

(defn trans*
  "trans(form) is used to assoc values to an existing map.  Specifically, when the new values depend on values already existing in the map.

  This is similar to CL's let*, the map gets the keys assoc'd as it goes."
  [& params]
  (fn[a-map]
    (reduce 
     (fn[accum entry]
       (let [k (first entry)
	     v (second entry)]
	 (assoc accum k (v accum))))
     a-map
     (partition 2 params))))

; These are a collection of helper functions in 
; defining the visitor functions.
(defn- kp [f] (comp f key)) 
(defn- vp [f] (comp f val))
(defn- ke [f] (juxt (comp f key) val))
(defn- ve [f] (juxt key (comp f key)))

(defn- hash-builder [coll] (into {} coll))

(def #^{:doc "Key predicate" 
	 :arglists '([pred-fn pred coll])} 
     hash-keys-pred (visitor kp hash-builder))

(def #^{:doc "Val predicate" 
	:arglists '([pred-fn pred coll])} 
     hash-vals-pred (visitor vp hash-builder))

(def #^{:doc "Key mapping fn" 
	:arglists '([map-fn f coll])} 
     hash-keys-map
     (visitor ke hash-builder))

(def #^{:doc "Val mapping fn" 
	:arglists '([map-fn f coll])} 
     hash-vals-map
     (visitor ve hash-builder))

(defn hash-keys-map-merge 
  "Like visit keys, but takes a merge function to resolve keys collisions."
  [merge-fn & args]
  (apply (visitor 
	  ke
	  (comp (partial apply merge-with merge-fn)
		(partail map (p apply hash-map))))
	 args))

(defn- sort-builder
  [args]
  (let [c (.comparator (second args))]
    (fn [coll]
      (apply sorted-map-by c
	     (apply concat coll)))))

(defn sort-keys-pred
  [f & args]
  (apply
   (visitor kp (sort-builder args))
   f args))

(defn sort-vals-pred
  [f & args]
  (apply
   (visitor vp (sort-builder args))
   f args))

(defn sort-keys-map
  [f & args]
  (apply
   (visitor ke (sort-builder args))
   f args))

(defn sort-vals-map
  [f & args]
  (apply
   (visitor ve (sort-builder args))
   f args))

(defn sort-keys-map-merge 
  "Like visit keys, but takes a merge function to resolve keys collisions."
  [merge-fn & args]
  (apply (visitor 
	  ke
	  (comp (sort-builder (rest args))
		(partial apply merge-with merge-fn)
		(partial map (partial apply hash-map))))
	 args))

(defn- sort-test
  [coll]
  (sorted? (second coll)))

(defn keys-pred
  [f & args]
  (apply (if (sort-test args) 
	   sort-keys-pred
	   hash-keys-pred)
	 f args))

(defn vals-pred
  [f & args]
  (apply (if (sort-test args) 
	   sort-vals-pred
	   hash-vals-pred)
	 f args))

(defn keys-map
  [f & args]
  (apply (if (sort-test args) 
	   sort-keys-map
	   hash-keys-map)
	 f args))

(defn vals-map
  [f & args]
  (apply (if (sort-test args) 
	   sort-vals-map
	   hash-vals-map)
	 f args))

(defn keys-map-merge
  [merge-fn f & args]
  (apply (if (sort-test (rest args))
	   sort-keys-map-merge
	   hash-keys-map-merge)
	 merge-fn f args))
	
(defn cat-proj
  "DEPRICATED

This function takes a collection of proj closures and returns a clojure that eagerly concatentates them."
  [& proj-coll]
  (fn [input-map] (vec (reduce concat (map #(% input-map) proj-coll)))))

(defn map-vals
  "DEPRICATED - use the vals-map visitor

This function behaves like map, except that f is applied to the values of the map, not just the entry.
The result is a hash-map, not a seq."
  [f coll] 
  (apply merge (map (fn[[k v]] { k (f v)}) coll)))

(defn map-keys
  "DEPRICATED - use the keys-map visitor, or the keys-map-merge visitor

This function behaves like map, except that f is applied to the keys of the map, not just the entry.
The result is a hash-map, not a seq.  Also takes an optional merge-fn in the event that a collision is generated."
  ([f coll] 
     (apply merge (map (fn[[k v]] { (f k) v}) coll)))
  ([f merge-fn coll]
     (apply merge-with merge-fn (map (fn[[k v]] { (f k) v}) coll))))

(defn filter-map
  "DEPRICATED

This is a specialized form of filter.  It is designed to transform the resulting seq into a hash-map."
  [pred a-map]
  (apply merge 
	 (map  
	  #(apply hash-map %) 
	  (filter pred a-map))))

(defn remove-map
  "DEPRICATED

This is a specialized form of remove.  It is designed to transform the resulting seq into a hash-map."
  [pred a-map]
  (apply merge 
	 (map  
	  #(apply hash-map %) 
	  (remove pred a-map))))

(defn filter-map-keys
  "DEPRICATED - use keys-pred visitor"
  [f a-map]
  (filter-map (comp f first) a-map))

(defn filter-map-vals
  "DEPRICATED - use vals-pred visitor"
  [f a-map]
  (filter-map (comp f second) a-map))

(defn remove-map-keys
  "DEPRICATED - use keys-pred visitor"
  [f a-map]
  (remove-map (comp f first) a-map))

(defn remove-map-vals
  "DEPRICATED - use vals-pred visitor"
  [f a-map]
  (remove-map (comp f second) a-map))


(defn- merge-like
  "DEPRICATED

This is an internal function so that merge with behaves like merge.  It is the default for marshall hashmap."
  [accum current]
  current)

(defn marshall-hashmap
  "DEPRICATED - The proper idiom is to use zipmap

This is a function designed to marshall a hash-map from a collection.  Very handy to combine with a 
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

(defn list-to-map
  "DEPRICATED - use (into {} coll)"
  [& coll]
  (marshall-hashmap (partition 2 coll)))

(defmacro deftrans
  "DEPRICATED - use a normal def.

This macro is analogous to defn.  It creates a trans closure based on body and binds the result to name."
  [name & body]
  `(def ~name (trans ~@body))) 


(defmacro deftrans*
  "DEPRICATED - use a normal def.

This macro is analogous to defn.  It creates a trans* closure based on body and binds the result to name."
  [name & body]
  `(def ~name (trans* ~@body))) 