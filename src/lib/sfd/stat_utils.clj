(ns lib.sfd.stat-utils)

(defn mean
  [coll]
  (/ (reduce + coll) (count coll)))

(defn variance
  [coll]
  (let [mu (mean coll)]
    (/ (reduce + 
	       (map (comp #(* % %) #(- % mu)) coll))
       (count coll))))

(defn stdev
  [coll]
  (java.lang.Math/sqrt (variance coll)))