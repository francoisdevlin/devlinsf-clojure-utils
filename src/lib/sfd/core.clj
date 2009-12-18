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
  (fn[& args](vec(map #(apply % args) coll))))

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

(defn- same-dispatch [coll]
  (cond
   (map? coll) :default
   (sorted? coll) :sorted-set
   (set? coll) :hash-set
   (vector? coll) :vector
   (string? coll) :string
   true :default))

(defmulti same (fn [ & args] (same-dispatch (last args))))

(defmethod same :default
  [hof f & args]
  (apply hof f args))

(defmethod same :vector
  [hof f & args]
  (vec (apply hof f args)))

(defmethod same :hash-set
  [hof f & args]
  (set (apply hof f args)))

(defmethod same :string
  [hof f & args]
  (apply str (apply hof f args)))

(defmethod same :sorted-set
  [hof f & args]
  (let [c (.comparator (first args))]
    (apply sorted-set-by c (apply hof f args))))

(defmulti multi-same (fn [ & args] (same-dispatch (last args))))

(defmethod multi-same :default
  [hof f & args]
  (apply hof f args))

(defmethod multi-same :string
  [hof f & args]
  (map (partial apply str) (apply hof f args)))

(defmethod multi-same :vector
  [hof f & args]
  (map vec (apply hof f args)))

(defmethod multi-same :hash-set
  [hof f & args]
  (map set (apply hof f args)))

(defmethod multi-same :sorted-set
  [hof f & args]
  (let [c (.comparator (first args))]
    (map (partial apply sorted-set-by c) (apply hof f args))))

(defn alternate
  "Splits a collection by matching a predicate.  The predicate match is greedy.  It's like a regex partition."
  [pred coll]
  (lazy-seq
    (loop [output []
	   r-coll coll]
      (let [item (first r-coll)
	    split-coll (split-with #(= (nil? (pred item)) (nil? (pred %))) r-coll)
	    new-coll (conj output (first split-coll))
	    non-matching-coll (second split-coll)]
	(if (empty? non-matching-coll)
	  new-coll
	  (recur new-coll non-matching-coll))))))

(defn split
  "Splits like a regex split."
  [pred coll]
  (remove (comp pred first) (alternate pred coll)))

(defn take-last
  "Mirrors drop-last.  Works like tail, the classic *nix command."
  [n coll]
  (drop (- (count coll) n) coll))

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