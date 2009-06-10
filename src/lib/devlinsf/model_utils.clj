(ns lib.devlinsf.model-utils
  (:use lib.devlinsf.str-utils
	lib.devlinsf.sql-utils
	lib.devlinsf.map-utils
	clojure.contrib.sql
	clojure.contrib.with-ns))

(defn if-doc
  [doc-string?]
  (if doc-string?
    (str "\n\n  " doc-string?)))

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
     (let [mapping-name (symbol (str name "-mapping"))]
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

(defn find-params
  [& input-list]
  (if (= (class (first input-list)) java.lang.String)
		     (rest input-list)
		     input-list))

(defn find-doc-string
  [& input-list]
  (if (= (class (first input-list)) java.lang.String)
    (first input-list)))
  

(defmacro def-projection
  [name & input-keys]
  (let [header-name (symbol (str name "-columns"))
	column-keys (find-params input-keys)
	input-symbol (gensym "input_")
	doc-string? (str (if-doc (find-doc-string input-keys))
			  "This projection applies to the following keys:\n  * " 
			  (str-join "\n  * " (map #(str-rest(str %)) column-keys))) ]
    `(do
       (def ~header-name [~@column-keys])
       (def-mapping ~name ~doc-string? [~input-symbol] ((vals-subset-mf ~@column-keys) ~input-symbol)))))

(defn multi-projection
  [& projections]
  (fn [input-map] (vec (reduce concat (map #(% input-map) projections)))))

(defmacro def-transform
  [name & input-keys]
  (let [transform-symbol (symbol (str name "-transform"))
	param-map (apply hash-map (apply find-params input-keys))
	doc-string? (find-doc-string input-keys)
	input-map-symbol (gensym "input-map_")
	fn-map-symbol (gensym "fn-map_")]
    `(defn ~transform-symbol [~input-map-symbol]
       ~doc-string?
       (apply assoc ~input-map-symbol
	     (reduce concat 
		     (map 
		      (fn [~fn-map-symbol]
			(list 
			 (first ~fn-map-symbol) 
			 ((second ~fn-map-symbol) ~input-map-symbol)))
		      ~param-map))))))

(defmacro create-finder-fn
  [db-con table-name]
  (let [base-name (keywordize table-name)
	finder-symbol 'find-records
        cond-symbol (gensym "cond-map_")]
    `(defn ~finder-symbol
       ([] (~finder-symbol nil))
       ([~cond-symbol]
	  (get-tuples ~db-con ~table-name ~cond-symbol)))))

(defmacro create-update-fn
  [db-con table-name]
   (let [base-name (keywordize table-name)
	 update-symbol 'update-records
	 cond-symbol (gensym "cond-map_")
	 attr-symbol (gensym "attr-map_")]
     `(defn ~update-symbol
       [~cond-symbol ~attr-symbol]
       (clojure.contrib.sql/with-connection 
	~db-con
	(clojure.contrib.sql/transaction
	 (clojure.contrib.sql/update-values
	  ~(keyword table-name)
	  [(where-clause ~cond-symbol)]
	  ~attr-symbol))))))

(defmacro create-delete-fn
  [db-con table-name]
  (let [base-name (keywordize table-name)	
	delete-symbol 'delete-records
        kill-symbol 'kill-all-records
        cond-symbol (gensym "cond-map_")
        kill-params-symbol (gensym "kill-params_")]
    `(do
       (defn ~delete-symbol
	 [~cond-symbol]
	 (clojure.contrib.sql/with-connection
	  ~db-con
	  (clojure.contrib.sql/transaction
	   (clojure.contrib.sql/delete-rows
	    ~(keyword table-name)
	    [(where-clause ~cond-symbol)]))))
       (defn ~kill-symbol
	 ~(str "This function will delete the " table-name " table.  Pass the keyword :yes to activate the function.")
	 ([] (~kill-symbol nil))
	 ([~kill-params-symbol]
	    (if (= ~kill-params-symbol :yes)
	      (clojure.contrib.sql/with-connection
	       ~db-con
	       (clojure.contrib.sql/transaction
		(clojure.contrib.sql/do-commands
		 ~(str "DROP TABLE " table-name))))
	      "Table not deleted.  Pass the keyword :yes to execute the command."))))))

(defmacro def-model
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

