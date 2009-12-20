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
  (class (last args)))

(defmulti
  #^{:doc
     "same is a mutlimethod that is designed to \"undo\" seq.  It expects
a seq-fn that returns a normal seq, and the appropraite args.  It converts
the resulting seq into the same type as the last argument.  If it is a 
sorted seq, the comparator is preserved.

This operation is fundamentally eager."
     :arglists '([seq-fn & args])}
  same same-dispatch)

(defmethod same String
  [hof f & args]
  (apply str (apply hof f args)))

(defmethod same clojure.lang.LazySeq
  [hof f & args]
  (apply hof f args))

(defmethod same :default
  [hof & args]
  (into (empty (last args)) (apply hof args)))

(defmulti
  #^{:doc
     "multi-same is a mutlimethod that is designed to \"undo\" seq.  It expects
a seq-fn that returns a seq of seqs, and the appropraite args.  It converts
the resulting element seqs into the same type as the last argument.  If it is a 
sorted seq, the comparator is preserved."
     :arglists '([seq-fn & args])}
  multi-same same-dispatch)

(defmethod multi-same String
  [hof & args]
  (map (partial apply str) (apply hof args)))

(defmethod same clojure.lang.LazySeq
  [hof & args]
  (apply hof args))

(defmethod multi-same :default
  [hof f & args]
  (map (partial into (empty (last args))) (apply hof f args)))

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

(defn alternate
  "Splits a collection by matching a predicate.  The predicate match is greedy.  It's like a regex partition."
  [pred coll]
  (lazy-seq
    (loop [output []
	   r-coll coll]
      (let [item (first r-coll)
	    split-coll (split-with #(= (nil? (pred item)) (nil? (pred %))) r-coll)
	    next-output (conj output (first split-coll))
	    non-matching-coll (second split-coll)]
	(if (empty? non-matching-coll)
	  next-output
	  (recur next-output non-matching-coll))))))

(defn split
  "Splits like a regex split."
  [pred coll]
  (remove (comp pred first) (alternate pred coll)))

(defn match
  "Finds all of the matches."
  [pred coll]
  (filter (comp pred first) (alternate pred coll)))
 
(defn take-until
  "Returns a lazy sequence of successive items from coll while
  (pred item) returns false. pred must be free of side-effects."
  [pred coll]
  (take-while (complement pred) coll))

(defn drop-until
  "Returns a lazy sequence of the items in coll starting from the first
  item for which (pred item) returns true."
  [pred coll]
  (drop-while (complement pred) coll))

(defn rotate
  "Take a collection and left rotates it n steps.  If n is negative, the
collection is rotated right. Executes in O(n) time."
  [n coll]
  (let [c (count coll)]
    (take c (drop (- c (mod n c)) (cycle coll)))))

(defn rotate-while
  "Rotates a collection left while (pred item) is true.  Will return a unrotated
sequence if (pred item) is never true. Executes in O(n) time."
  [pred coll]
  (let [head (drop-while pred coll)]
    (take (count coll) (concat head coll))))

(defn rotate-until
  "Rotates a collection left while (pred item) is nil.  Will return a unrotated
sequence if (pred item) is always true. Executes in O(n) time."
  [pred coll]
  (rotate-while (complement pred) coll))