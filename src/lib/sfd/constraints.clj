(ns    
    #^{:author "Sean Devlin"
       :doc 
       "    This file defines a constraint engine for working with
    systems of equations.  There are macros for defining contraints
    and functions to enforce the constraints.  The constraint engine is
    written as a series of single variable equations.

      The main fns/macros are
    * defconstraint
    * definference
    * infer
    * infer-all
    * infer-chain

    It depends on a Newton solver.  As such, the results will be limited
    to the weaknesses of Newton's method."}
  lib.sfd.constraints
  (:use lib.sfd.math.numerics
	lib.sfd.pred-utils))

(defn find-all-free-keys
  "This is a utility fn to determine which keys are free in a map.
  It is used to determine which variable to solve for."
  [source-keys a-map]
  (let [frozen-keys (map first (filter (every-pred? 
					second 
					(comp (set source-keys) first))
				       a-map))
	remaining-keys (remove (set frozen-keys) source-keys)]
    remaining-keys))

(defn find-free-key
  "This is a utility fn to determine which keys are free in a map.
  It is used to determine which variable to solve for."
  [source-keys a-map]
  (let [remaining-keys (find-all-free-keys source-keys a-map)]
    (if (= (count remaining-keys) 1)
      (first remaining-keys))))

(defn solve-constraint
  "This solves a constraint fn with respect to free-var (a keyword).
  Typically constraints end with a * suffix."
  [constraint free-var a-map]
  (let [target-fn (fn [x] (constraint (assoc a-map free-var x)))]
    (solve target-fn :diff-method :richardson)))


(defmacro defconstraint
  "Creates a constraint.  This macro defines three functions.

  * sym*, which is the actual constraint.  It should be equal to zero.
  * sym-val, a closure to determine the actual value of the free fn.
  * sym, which returns a map.  The result of sym-val is assoc'd with 
  the free vairable."
  ([sym binding left right] 
     `(defconstraint ~sym "" ~binding ~left ~right))
  ([sym doc-string binding left right]
     (let [star-sym (symbol (str sym "*"))
	   val-sym (symbol (str sym "-val"))	
	   keywords (vec (map keyword binding))
	   binding-map (zipmap binding keywords)
	   free-key (gensym "free-key_")
	   eq-doc (str "This solves the following equation:\n  " left " = " right)
	   sexp-doc (str "This evaluates the following expression:\n\n  (- " left " " right ")"
			 "\n\n  The result of the expression should be zero.")
	   keys-doc (str "This requires the following keys in constraints:\n  "
			 (apply str (interpose " " keywords)))
	   common-doc (str doc-string "\n\n  "eq-doc "\n\n  " keys-doc)
	   star-doc (str doc-string "\n\n  " sexp-doc)
	   sym-doc (str common-doc "\n\n  Returns a map with the free variable included.")
	   val-doc (str common-doc "\n\n  Returns the value of the free variable in a double.")]
       `(do
	  (defn ~star-sym ~star-doc [~binding-map]
	    (- ~left ~right))
	  (defn ~val-sym ~val-doc [~'constraints-map]
	    (let [~free-key (find-free-key ~keywords ~'constraints-map)]
	      (if ~free-key (solve-constraint ~star-sym ~free-key ~'constraints-map))))
	  (defn ~sym ~sym-doc [~'constraints-map]
	    (let [~free-key (find-free-key ~keywords ~'constraints-map)]
	      (if ~free-key (assoc ~'constraints-map ~free-key (~val-sym ~'constraints-map)))))
	  ))))

(defmacro definference
  "Creates a constraint and adds it to the supplied inference graph.
  Use this instead of defconstraint if you want to use this inference
  engine.

  inference-graph-ref is expected to be a ref to a map."
  ([sym binding inference-graph-ref left right] 
     `(definference ~sym "" ~binding ~inference-graph-ref ~left ~right))
  ([sym doc-string binding inference-graph-ref left right]
     (let [keywords (vec (map keyword binding))]
       `(do      
	  (defconstraint ~sym ~doc-string ~binding ~left ~right)
	  (dosync (alter ~inference-graph-ref assoc ~keywords ~sym))))))

(def *max-depth* 10)

(defn- potential-fns
  "A private helper fn for inference chain."
  [values graph]
  (into {} (filter (comp (partial some (set values)) first) graph)))

(defn- usable-fns 
  "A private helper fn for inference chain."
  [current-map pot-fns]
  (into {} (filter (comp #(find-free-key % current-map) first) pot-fns)))
    
(defn infer-chain
  "This function does the heavy lifting for the infer method.
It inspects the input-map, and apllies any constraints that
have exactly one free variable.  Once a constraint that can
find desired value is found, the resulting closure chain is
reutrned.

  This performs a depth-first search, and will return nil when
*max-depth* steps are found."
  [inference-graph desired-value input-map]
  (let [clean-map (into {} (filter second input-map))]
    (if (clean-map desired-value)
      [identity] ;Value already is in map, abort
      ((fn inference-loop [current-map current-chain current-iter]
	(if (not (zero? current-iter))
	  (let [end-fns (potential-fns [desired-value] inference-graph)
		end-now (usable-fns current-map end-fns)]
	    (cond
	      (empty? end-fns) nil ;Can't get to the end :(
	      (not (empty? end-now)) (conj current-chain (first (vals end-now))) ; I can end!
	      true (let ;Can I make a move?
		       [next-fns (potential-fns (keys current-map) inference-graph)
			next-now (usable-fns current-map next-fns)]
		     (cond
		       (empty? next-now) nil ;Can't take next step :(
		       true (first (filter identity
					   (map #(let [next-move (second %) ;I can move!
						       current-key (find-free-key (first %) current-map)]
						   (inference-loop 
						    (assoc current-map current-key 1)
						    (conj current-chain next-move)
						    (dec current-iter)))
						next-now)))))))))
       clean-map () *max-depth*))))
      

(defn infer
  "This attempts to infer a value based on the equations in inference-graph.
It works by using infer-chain to determine a list of closures.

  It is NOT recommend using this function in a mapping operation.  Instead,
call infer-chain and cache the resulting closures.

  Inference graph is a map, not a ref.  Please deref accodingly."
  [inference-graph desired-value input-map]
  (let [clean-map (into {} (filter second input-map))
	ic (infer-chain inference-graph desired-value input-map)]
    (if ic
      ((apply comp ic) clean-map))))

(defn infer-all
  "Infers lots o' stuff!"
  [inference-graph desired-values input-map]
  ((apply comp (map #(partial infer inference-graph %) desired-values)) input-map))

(defn infer-val
  "This works like infer, but returns a double value instead of a map."
  [inference-graph desired-value input-map]
  (desired-value (infer inference-graph desired-value input-map)))