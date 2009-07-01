(ns lib.devlinsf.map-utils
  (:use lib.devlinsf.str-utils))

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

(defn pivot
  "This function is designed to reudce a list of tuples to a single map."
  ([data kf vf](pivot data kf vf +))
  ([data kf vf reduct-fn]
    (apply merge-with reduct-fn 
      (map #(apply hash-map ((proj kf vf) %))
        data))))

(defn freq
  "This function returns 1 regardless of inputs."
  [& ignored-params]
  1)

(defn test-conj 
  [& args]
  (if (= (count args) 1) 
    (vector (first args))
    (concat (first args) (rest args))))

(defn test-conj 
  [& args]
  (println (count args)))

(defn vectorize
  [arg]
  (if (seq? arg)
    (vec arg)
    (vector arg)))

(defn setize
  [arg]
  (if (seq? arg)
    (apply hash-set arg)
    (hash-set arg)))

(defn merge-con
  [coll-fn]
  (fn [& args]
    (let [f-arg (first args)
	  s-arg (second args)]
      (apply conj
	     (coll-fn f-arg)
	     (coll-fn s-arg)))))

(defn test-conj
  [& args]
  (let [f-arg (first args)
	s-arg (second args)]
    (concat (if (seq? f-arg)
	      (f-arg)
	      (vecotr f-arg))
	    (if (seq? f-arg)
	      (f-arg)
	      (vecotr f-arg))
	    