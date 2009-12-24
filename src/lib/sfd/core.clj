(ns lib.sfd.core)

(def & comp)
(def p partial)

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

(defn fn-tuple
  "This is comp's twin.  comp takes a collection of functions, and applied them in series.
  fn-tuple takes a collection of functions, and applies them in parallel."
  [& coll]
  (fn[& args](vec (map #(apply % args) coll))))

(def proj fn-tuple)

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

(defn hof-args
  "This is a helper function that determines the appropriate
arguments for the same multimethod."
  [args]
  (if (integer? (first args))
    (rest args)
    args))

(defn hof-target
  "A helper function to determine the collection type for the
same multimethod."
  [args]
  (if (integer? (first args))
    (nth (rest args) (first args))
    (last args)))

(defn visitor
  "Used to implement visitor patterns.  (first (args)) is modified by the visitor function"
  [visit-fn return-fn]
  (fn [f & args]
    (return-fn
     (apply f (visit-fn (first args)) (rest args)))))

(defn visitor*
  [visit-fn return-fn]
  (fn [f & args]
    (let [r-args (reverse args)
	  mod-args (conj (rest r-args) (visit-fn (first r-args)))]
    (return-fn
     (apply f (reverse mod-args))))))

(def visit-keyword (visitor name keyword))
(def visit-symbol (visitor name symbol))


(def #^{:doc "Visitor function designed to apply a 
transient function f to a persistent collection.
Returns a peristent collection." 
	:arglists '([f! & args])} 
     quick! (visitor transient persistent!))


(defn- same-dispatch [& args]
  (class (hof-target args)))

(defmulti
  #^{:doc
     "same is a mutlimethod that is designed to \"undo\" seq.  It expects
a seq-fn that returns a normal seq, and the appropraite args.  By default 
it converts the resulting seq into the same type as the last argument.  An
optional leading integer, index, can be provided to specify the index of the
argument that should be used to convert the seq.  If it is a sorted seq, 
the comparator is preserved.

This operation is fundamentally eager, unless a lazy seq is detected.  In 
this case no conversion is attempted, and laziness is preserved."
     :arglists '([index seq-fn & args])}
  same same-dispatch)

(defmethod same String
  [& args]
  (let [s-args (hof-args args)
	f (first s-args)
	a (rest s-args)]
    (apply str (apply f a))))

(defmethod same clojure.lang.LazySeq
  [& args]
  (let [s-args (hof-args args)
	f (first s-args)
	a (rest s-args)]
  (apply f a)))

(defmethod same :default
  [& args]
  (let [s-args (hof-args args)
	f (first s-args)
	a (rest s-args)]
    (into (empty (hof-target args)) (apply f a))))

(defmulti
  #^{:doc
     "multi-same is a mutlimethod that is designed to \"undo\" seq.  It expects
a seq-fn that returns a seq of seqs, and the appropraite args.  It converts
the resulting element seqs into the same type as the last argument.  If it is a 
sorted seq, the comparator is preserved."
     :arglists '([index seq-fn & args])}
  multi-same same-dispatch)

(defmethod multi-same String
  [& args]
  (let [s-args (hof-args args)
	f (first s-args)
	a (rest s-args)]
  (map (partial apply str) (apply f a))))

(defmethod multi-same clojure.lang.LazySeq
  [& args]
  (let [s-args (hof-args args)
	f (first s-args)
	a (rest s-args)]
    (apply f a)))

(defmethod multi-same :default
  [& args]
  (let [s-args (hof-args args)
	f (first s-args)
	a (rest s-args)]
    (map (partial into (empty (hof-target args))) (apply f a))))

(defn key-entry
  "This is a helper function for mapping operations in a hashmap.  It
takes a fn, f, and creates a new fn that applies f to the key in each
entry.  A two element vector representing the entry is returned."
  [f] (fn [[k v]] [(f k) v]))

(defn val-entry
  "This is a helper function for mapping operations in a hashmap.  It
takes a fn, f, and creates a new fn that applies f to the value in each
entry.  A two element vector representing the entry is returned."
  [f] (fn [[k v]] [k (f v)]))