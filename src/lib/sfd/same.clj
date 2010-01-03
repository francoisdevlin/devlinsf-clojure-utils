(ns lib.sfd.same)

(defn- hof-args
  "This is a helper function that determines the appropriate
arguments for the same multimethod."
  [args]
  (if (integer? (first args))
    (rest args)
    args))

(defn- hof-target
  "A helper function to determine the collection type for the
same multimethod."
  ([args] (hof-target args -1))
  ([args idx]
     (if (integer? (first args))
       (nth (rest args) (mod (first args) (count (rest args))))
       (nth args (mod idx (count args))))))

(defprotocol same-p
  (my-into [to from] "Mimics into with specific overloading for same")
  (my-empty [coll] "Mimics empty with specific overloading for same"))

(extend java.lang.Object 
	same-p 
	{:my-into into
	 :my-empty empty})

(extend java.lang.String
	same-p 
	{:my-into (fn[to from] (apply str to from))
	 :my-empty (constantly "")})

(extend clojure.lang.LazySeq
	same-p
	{:my-into (fn[to from] from)
	 :my-empty (fn [coll] (take 1 '()))})

(defn same
  "same is a fn that is designed to \"undo\" seq.  It expects
a seq-fn that returns a normal seq, and the appropraite args.  By default 
it converts the resulting seq into the same type as the last argument.  An
optional leading integer, index, can be provided to specify the index of the
argument that should be used to convert the seq.  If it is a sorted seq, 
the comparator is preserved.

This operation is fundamentally eager, unless a lazy seq is detected.  In 
this case no conversion is attempted, and laziness is preserved."
  [& args]
  (let [s-args (hof-args args)
	f (first s-args)
	a (rest s-args)]
    (my-into (my-empty (hof-target args)) (apply f a))))

(defn multi-same
  "multi-same is a fn that is designed to \"undo\" seq.  It expects
a seq-fn that returns a seq of seqs, and the appropraite args.  It converts
the resulting element seqs into the same type as the last argument.  If it is a 
sorted seq, the comparator is preserved."
  [& args]
  (let [s-args (hof-args args)
	f (first s-args)
	a (rest s-args)]
    (map (partial my-into (my-empty (hof-target args))) (apply f a))))

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