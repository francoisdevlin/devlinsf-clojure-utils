(ns lib.devlinsf.map-utils
  (:use lib.devlinsf.str-utils))

(defn submap
  "Returns a sub-map based on input-keys"
  [input-map & input-keys]
  (loop [result {}
         remaining-keys input-keys]
    (if (empty? remaining-keys)
      result
      (let [current-key (first remaining-keys)]
        (recur
          (conj result (hash-map current-key (input-map current-key)))
          (rest remaining-keys))))))

(defn get-keys
  "Returns a vector of values based on a list of input keys.  Order is guarneteed.  Duplicate keys are returned twice."
  [input-map & input-keys]
  (loop [result []
         remaining-keys input-keys]
    (if (empty? remaining-keys)
      result
      (let [current-value (input-map (first remaining-keys))]
        (recur
          (conj result current-value)
          (rest remaining-keys))))))

(defn filter-nil-vals
  [input-map]
  (apply merge {} 
	 (map #(apply hash-map %) 
	      (filter second input-map))))

(defn render-map
  [input-map kv-delimiter entry-delimiter]
  (str-join entry-delimiter 
	    (map #(str (first %) kv-delimiter (second %)) input-map)))