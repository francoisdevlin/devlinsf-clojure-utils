(ns lib.sfd.map-utils
  (:use lib.sfd.str-utils
	lib.sfd.core
	clojure.contrib.seq-utils
	clojure.set))

(defn filter-nil-vals
  [input-map]
  (apply merge {} 
	 (map #(apply hash-map %) 
	      (filter second input-map))))

(defn render-map
  [input-map kv-delimiter entry-delimiter]
  (str-join entry-delimiter 
	    (map (partial str-join kv-delimiter) input-map)))

(defn trans
  "This is similar to CL's let, the map does not have any information assoc'd until the end."
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
  "This is similar to CL's let*, the map gets the keys assoc'd as it goes."
  [& params]
  (fn[a-map]
    (reduce 
     (fn[accum entry]
       (let [k (first entry)
	     v (second entry)]
	 (assoc accum k (v accum))))
     a-map
     (partition 2 params))))


(defmacro deftrans
  [name & body]
  `(def ~name (trans ~@body))) 


(defmacro deftrans*
  [name & body]
  `(def ~name (trans* ~@body))) 

(defn cat-proj
  "This function takes a collection of proj closures and returns a clojure that eagerly concatentates them."
  [& proj-coll]
  (fn [input-map] (vec (reduce concat (map #(% input-map) proj-coll)))))

(defn map-vals
  "This function behaves like map, except that f is applied to the values of the map, not just the entry.
   The result is a hash-map, not a seq."
  [f coll] 
  (apply merge (map (fn[[k v]] { k (f v)}) coll)))

(defn map-keys
  "This function behaves like map, except that f is applied to the keys of the map, not just the entry.
   The result is a hash-map, not a seq.  Also takes an optional merge-fn."
  ([f coll] 
     (apply merge (map (fn[[k v]] { (f k) v}) coll)))
  ([f merge-fn coll]
     (apply merge-with merge-fn (map (fn[[k v]] { (f k) v}) coll))))

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

(defn filter-map-keys
  [f a-map]
  (filter-map (comp f first) a-map))

(defn filter-map-vals
  [f a-map]
  (filter-map (comp f second) a-map))

(defn remove-map-keys
  [f a-map]
  (remove-map (comp f first) a-map))

(defn remove-map-vals
  [f a-map]
  (remove-map (comp f second) a-map))


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

(defn list-to-map
  [& coll]
  (marshall-hashmap (partition 2 coll)))