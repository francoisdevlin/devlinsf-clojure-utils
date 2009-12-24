(ns lib.sfd.constraints
  (:use lib.sfd.core))

(defn old-pre-constraint
  [pre post]
  (fn[& args]
    {:pre [(if (integer? (first args))
	     (pre (nth (rest args) (first args) (constantly false)))
	     (pre (last args)))]}
    (if (integer? (first args))
      (let [r-args (rest args)]
	(apply (first r-args) (rest r-args)))
      (apply (first args) (rest args)))))

(defn pre-constraint
  [pred]
  (fn[& args]
    {:pre [(pred (hof-target args))]}
    (let [h-args (hof-args args)
	  f (first h-args)
	  a (rest h-args)]
      (apply f a))))

(defn post-constraint
  [pred]
  (fn[& args]
    {:post [(if (integer? (first args))
	      (pred (hof-target args))
	      (pred %))]}
    (let [h-args (hof-args args)
	  f (first h-args)
	  a (rest h-args)]
      (apply f a))))