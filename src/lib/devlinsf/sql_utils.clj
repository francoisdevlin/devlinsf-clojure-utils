(ns lib.devlinsf.sql-utils
  (:use lib.devlinsf.str-utils
	lib.devlinsf.map-utils
	clojure.contrib.sql))

(defmulti sqlize-table class)
(defmulti sqlize-column class)
(defmulti clause-detect class)

(defn where-clause[where-map]
  (str-join " AND "    
    (map #(str
            (sqlize-table (first %))
            (clause-detect (second %))
            (sqlize-column (second %)))
      where-map)))

(defn sql-select-str[table-name,where-map]
  (str "SELECT * FROM "
    (sqlize-table table-name)
    " WHERE "
    (where-clause where-map)))


(defmethod sqlize-table String [table-name]
  table-name)
(defmethod sqlize-table clojure.lang.Keyword [table-name]
  (apply str (rest (str table-name))))

(defn get-tuples
  [db table where-map]
  (with-connection pn-db
    (with-query-results rs
      [(sql-select-str table where-map)]
      (loop [output ()
             results rs]
        (if (first results)
          (recur (conj output (first results)) (rest results))
          output)))))

(derive clojure.lang.LazilyPersistentVector ::in-clause)
(derive clojure.lang.PersistentVector ::in-clause)

(defmethod sqlize-column String [val]
  (str "\"" val "\""))
(defmethod sqlize-column :default [val]
    val)
(defmethod sqlize-column ::in-clause [val]
  (str "("(str-join ", " (map sqlize-column val)) ")"))

(defmethod clause-detect :default [val]
    "=")
(defmethod clause-detect ::in-clause [val]
  " IN ")