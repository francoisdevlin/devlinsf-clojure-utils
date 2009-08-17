(ns lib.devlinsf.core)

(def & comp)
(def p partial)

(defn pa 
  [f & args] (apply p apply f args))

(defn find-params
  [& input-list]
  (if (= (class (first input-list)) java.lang.String)
    (rest input-list)
    input-list))

(defn find-doc-string
  [& input-list]
  (if (= (class (first input-list)) java.lang.String)
    (first input-list)))

(defn accum
  [f init]
  (let [local-ref (ref init)]
    (fn [val]
      (dosync (alter local-ref f val)))))

(defn ifi
  "Ifi takes a pred(icate) and a f(unction), and returns a test function.
  If (pred arg) is true, f is applied.  If it is false, identity is applied."
  [pred f]
  (fn[arg] (if (pred arg) 
	     (f arg)
	     arg)))

(defn apply-map
  "This works like apply, but for keyword arguments in a map."
  [f & args]
  (let [pos-args (butlast args)
        a-map (last args)
        applied-args (concat
                       (if pos-args pos-args [])
                       (cond
                         (nil? a-map) []
                         (map? a-map) (reduce concat a-map)
                         true (vector a-map)))]
    (apply f applied-args)))

(defn proj
  "This is comp's twin.  comp takes a collection of functions, and applied them in series.
  proj takes a collection of functions, and applies them in parallel."
  [& coll]
  (fn [input-map] (vec (map #(% input-map) coll))))

(defmacro defproj
  "This is a macro for naming projections"
  [name & input-keys]
  (let [proj-symbol name
	param-list (apply find-params input-keys)
	doc-string? (str (apply find-doc-string input-keys))
	input-map-symbol (gensym "input-map_")]
    `(do
       (defn ~proj-symbol ~doc-string? [~input-map-symbol]
	 ((proj ~@param-list) ~input-map-symbol)))))