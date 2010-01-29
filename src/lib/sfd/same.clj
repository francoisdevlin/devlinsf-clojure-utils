(ns lib.sfd.same)

(defprotocol same-p
  (my-into [to from] "Mimics into with specific overloading for same")
  (my-empty [coll] "Mimics empty with specific overloading for same"))

(defprotocol seq-p
  (to-seqable [coll] "Mimics seq if required"))

(extend java.lang.Object 
	same-p 
	{:my-into into
	 :my-empty empty}
	seq-p
	 {:to-seqable identity})

(extend java.lang.String
	same-p 
	{:my-into (fn[to from] (apply str to from))
	 :my-empty (constantly "")})

(extend clojure.lang.LazySeq
	same-p
	{:my-into (fn[to from] from)
	 :my-empty (fn [coll] (take 1 '()))})

(extend clojure.lang.PersistentList
	same-p 
	{:my-into (comp reverse into)
	 :my-empty empty})

(extend clojure.lang.PersistentList$EmptyList
	same-p 
	{:my-into (comp reverse into)
	 :my-empty empty})

(extend clojure.lang.AMapEntry
	same-p 
	{:my-into (fn [to from] (into [] from))
	 :my-empty (constantly [])})

(extend clojure.lang.Keyword
	same-p 
	{:my-into (fn[to from] (keyword (apply str (name to) from)))
	 :my-empty (constantly (keyword ""))}
	seq-p
	{:to-seqable name})

(extend clojure.lang.Symbol
	same-p 
	{:my-into (fn[to from] (symbol (apply str (name to) from)))
	 :my-empty (constantly (symbol ""))}
	seq-p
	{:to-seqable name})

(defn- seqify-last
  [args]
  (let [[h t] (split-at (dec (count args)))]
    (concat h (to-seqable t))))

(defn same
  "same is a fn that is designed to \"undo\" seq.  It expects
a seq-fn that returns a normal seq, and the appropraite args.
It converts the resulting seq into the same type as the last argument.

This operation is fundamentally eager, unless a lazy seq is detected.  In 
this case no conversion is attempted, and laziness is preserved."
  ([f arg1] (my-into (my-empty arg1) (f (to-seqable arg1))))
  ([f arg1 arg2] (my-into (my-empty arg2) (f arg1 (to-seqable arg2))))
  ([f arg1 arg2 arg3] (my-into (my-empty arg3) (f arg1 arg2 (to-seqable arg3))))
  ([f arg1 arg2 arg3 & more] (my-into (my-empty (last more)) (apply f arg1 arg2 arg3 (seqify-last more)))))

(defn multi-same
  "multi-same is a fn that is designed to \"undo\" seq.  It expects
a seq-fn that returns a seq of seqs, and the appropraite args.  It converts
the resulting element seqs into the same type as the last argument.  If it is a 
sorted seq, the comparator is preserved."
  ([f arg1] (map (partial my-into (my-empty arg1)) (f (to-seqable arg1))))
  ([f arg1 arg2] (map (partial my-into (my-empty arg2)) (f arg1 (to-seqable arg2))))
  ([f arg1 arg2 arg3] (map (partial my-into (my-empty arg3)) (f arg1 arg2 (to-seqable arg3))))
  ([f arg1 arg2 arg3 & more] (map (partial my-into (my-empty arg3)) (f arg1 arg2 arg3 (seqify-last more)))))

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