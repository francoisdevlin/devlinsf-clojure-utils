(ns lib.devlinsf.model-utils
  (:use lib.devlinsf.str-utils
	lib.devlinsf.sql-utils
	lib.devlinsf.map-utils
	clojure.contrib.with-ns)
  (:require [clojure.contrib [sql :as sql]]))

(defn- if-doc
  [doc-string?]
  (if doc-string?
    (str "\n\n  " doc-string?)))

(defn- find-params
  [& input-list]
  (if (= (class (first input-list)) java.lang.String)
		     (rest input-list)
		     input-list))

(defn- find-doc-string
  [& input-list]
  (if (= (class (first input-list)) java.lang.String)
    (first input-list)))

(defmacro def-filter
  ([name params body] (def-filter name nil params body))
  ([name doc-string? params body]
     (let [filter-name (symbol (str name "-filter"))]
       `(defn ~filter-name
	  ~(str "This is a function that acts as a " 
		name 
		" filter.  It can be used as a predicate."
		(if-doc doc-string?))
	  ~params
	  ~body))))

(defmacro def-filter-factory
  ([name params body] (def-filter-factory name nil params body))
  ([name doc-string? params body]
     (let [filter-name (symbol (str name "-ff"))]
       `(defn ~filter-name
	  ~(str "This is a factory function that produces " 
		name 
		" filters.  It returns a function that can be used as a filtering predicate."
		(if-doc doc-string?))
	  ~params
	  ~body))))

(defmacro def-mapping
  ([name params body] (def-mapping name nil params body))
  ([name doc-string? params body]
     (let [mapping-name (symbol (str name "-map"))]
       `(defn ~mapping-name
	  ~(str "This is a function that performs a \"" 
		name 
		"\" mapping."
		(if-doc doc-string?))
	  ~params
	  ~body))))

(defmacro def-mapping-factory
  ([name params body] (def-mapping-factory name nil params body))
  ([name doc-string? params body]
     (let [mapping-name (symbol (str name "-mf"))]
       `(defn ~mapping-name
	  ~(str "This is a factory function that produces" 
		name 
		" mappings.  It returns a function that can be used as a mapping operator."
		(if-doc doc-string?))
	  ~params
	  ~body))))

(def-mapping-factory keys-subset
  "This function is used to create standard sub-maps based on input keys.  The resulting mapping should return a hash-map."
  [& input-keys]
  (fn[input-map] (select-keys input-map input-keys)))

(def-mapping-factory vals-subset
  "This function is used to retrieve standard values based on input keys.  The resulting mapping should return a vector."
  [& input-keys]
  (fn[input-map] (vec (map #(input-map %) input-keys))))

(defn *update*
  [db-con table-name cond-map attr-map]
  (sql/with-connection 
   db-con
   (sql/transaction
    (sql/update-values
     (keyword table-name)
     [(where-clause cond-symbol)]
     attr-symbol))))

(defn *delete*
  [db-con table-name cond-map]
  (sql/with-connection
   db-con
   (sql/transaction
    (sql/delete-rows
     (keyword table-name)
     [(where-clause ~cond-symbol)]))))

(defn *drop*
  [db-con table-name kill-params-symbol]
     (if (= kill-params-symbol :yes)
       (sql/with-connection
	db-con
	(sql/transaction
	 (sql/do-commands
	  (str "DROP TABLE " table-name))))
       "Table not deleted.  Pass the keyword :yes to execute the command."))

(defmacro create-finder-fn
  [db-con table-name]
  (let [finder-symbol 'find-records
        cond-symbol (gensym "cond-map_")]
    `(defn ~finder-symbol
       ([] (~finder-symbol nil))
       ([~cond-symbol]
	  (get-tuples ~db-con ~table-name ~cond-symbol)))))

(defmacro create-update-fn
  [db-con table-name]
   (let [update-symbol 'update-records
	 cond-symbol (gensym "cond-map_")
	 attr-symbol (gensym "attr-map_")]
     `(defn ~update-symbol
       [~cond-symbol ~attr-symbol]
       (*update* ~db-con ~table-name ~cond-symbol ~attr-symbol))))

(defmacro create-delete-fn
  [db-con table-name]
  (let [delete-symbol 'delete-records
        kill-symbol 'kill-all-records
        cond-symbol (gensym "cond-map_")
        kill-params-symbol (gensym "kill-params_")]
    `(do
       (defn ~delete-symbol
	 [~cond-symbol]
	 (*delete* ~db-con ~table-name ~cond-symbol))
       (defn ~kill-symbol
	 ~(str "This function will DROP the " table-name " table.  Pass the keyword :yes to activate the function.")
	 ([] (~kill-symbol nil))
	 ([~kill-params-symbol]
	    (*drop* ~db-con ~table-name ~kill-params))))))

(defmacro defmodel
  ([db-con table-name] (def-model db-con table-name nil))
  ([db-con table-name base-ns]
     (let [base-name (keywordize table-name)
	   model-ns (if base-ns
		      (symbol (str base-ns "." base-name))
		      (symbol (str *ns*)))
	   qualified-db (symbol (str *ns* "/" db-con))]
       `(do
	  (when-not (find-ns (quote ~model-ns)) (create-ns (quote ~model-ns)))
	  (with-ns (quote ~model-ns)
		   (do
		     (create-finder-fn ~qualified-db ~table-name)
		     (create-update-fn ~qualified-db ~table-name)
		     (create-delete-fn ~qualified-db ~table-name)))))))