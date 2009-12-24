(ns lib.sfd.seq-utils)

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
    (take c (drop (mod n c) (cycle coll)))))

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
