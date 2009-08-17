#map-utils

Sean Devlin

July 1, 2009

Namespace: lib.devlinsf.map-utils

This is a collection of map utility functions that I use when manipulating map data.  Since much of clojure is designed around manipulating a list of tuples, 
I find these very handy.  This documentation reviews how to do the following things.

* transforming a map
* projecting a map
* marshalling a map
* joining map lists
* pivoting map lists

#Tranforming a map

This section explores the `trans` closure, which is used to modify a tuple.

##trans

I defined a function trans 

	(defn trans [& params]...) 

Let me show an example: 

	user=> (def test-map {:a 0 :b "B" :c "C"}) 
	#'user/test-map 
	
	user=> ((trans :count count) test-map) 
	{:count 3, :a 0, :b "B", :c "C"} 
	
Notice the call to trans first, and then the result it applied to test- 
map.  This is because trans generates a closure.  In this case, it 
applies the count function to the map, and associates it with the 
key :count. 

Here's how I would write the incrementer: 

	user=> ((trans :a (comp inc :a)) test-map) 
	{:a 1, :b "B", :c "C"} 

##deftrans

trans is a little cumbersome, generating a closure.  I also wrote a 
deftrans macro.  It creates a trans and stores it in the provided 
name: 

	user=> (deftrans counter :count count) 
	#'user/counter 
	
	user=> (counter test-map) 
	{:count 3, :a 0, :b "B", :c "C"} 
	
	user=> (deftrans inc-a :a (comp inc :a)) 
	#'user/inc-a 

	user=> (inc-a test-map) 
	{:a 1, :b "B", :c "C"} 

##Using a closure
	
Let's revisit the fact that trans generates a closure.  We can use the 
resulting transform anywhere we'd use a function. 

### In a map
	user=> (map (trans :count count) (repeat 5 test-map)) 
	({:count 3, :a 0, :b "B", :c "C"} 
	{:count 3, :a 0, :b "B", :c "C"} 
	{:count 3, :a 0, :b "B", :c "C"} 
	{:count 3, :a 0, :b "B", :c "C"} 
	{:count 3, :a 0, :b "B", :c "C"}) 

Or, we could use the def'd version 

	user=> (map counter (repeat 5 test-map)) 
	(...) 

### In a comp
	user=> ((comp inc-a counter counter) test-map) 
	{:count 4, :a 1, :b "B", :c "C"} 

### In the STM

This is my favorite use of trans so far

	user=> (def test-ref (ref test-map)) 
	#'user/test-ref 

	user=> (dosync (alter test-ref inc-a)) 
	{:a 1, :b "B", :c "C"} 

	user=> @test-ref 
	{:a 1, :b "B", :c "C"} 
	
##Extra stuff 

I also added a feature to enable the use of decoder/lookup maps.  When 
a map is passed instead of a function, it is assumed that the map is 
to decode the specific key it is assigned to. 

	(def decoder-map {0 "Awesome" 1 "Not Awesome"}) 

	;This will decode the key :a 
	user=> (deftrans decoder :a decoder-map) 
	#'user/decoder 
	
	user=> (decoder test-map) 
	{:a "Awesome", :b "B", :c "C"} 
	
	user=> (decoder @test-ref) 
	{:a "Not Awesome", :b "B", :c "C"} 
	
#Projecting a map
`proj` is the parallel version of `comp`.  

`proj` is used to apply a list of functions to a map, and return the result as a vector.  Here's an example with the same `test-map` as above.

	user=> ((proj :a :b :c count) test-map)
	[0 "B" "C" 3]
	
We'll see proj in action better when we get to the section on joins

#marshall-hashmap
This is designed to aid in transforming a list into a hash-map.  Typically a list of pairs need a minor transformation to become a hash-map.
###Signature

	(marshall-hashmap coll)
 	(marshall-hashmap coll key-fn)
 	(marshall-hashmap coll key-fn val-fn)
 	(marshall-hashmap coll key-fn val-fn merge-fn)

This is a function designed to marsh a hash-map from a collection.  Very handy to combine with a parser.  The defaults are

	key-fn: first
	val-fn: second
	merge-fn: merge-like

They are progressively replaced as the arity increases.

###Usage
TO DO: Add an example

#Altering a map
All the higher order functions in clojure accept and return a seq.  It is common to transform the resulting seq into a hash map.
These are a few functions that do this for you automatically.

##map-vals
This is like the `map` operator, but it applies `f` to every value of the hash map instead of the entry.  It returns a hash map.

##map-keys
This is like the `map` operator, but it applies `f` to every key of the hash map instead of the entry.  It returns a hash map.

##filter-map
This behaves just like `filter`.  `pred` is applied to each entry of the hash-map, and the resulting collection is transformed into a hash map.

##remove-map
This behaves just like `remove`.  `pred` is applied to each entry of the hash-map, and the resulting collection is transformed into a hash map.

#Joining a list of hashes
There are many times when you need to perform a type of join on a list of data, but it cannot 
be performed on the back end.  The back end may not support the type of join, or you may need to join
data from two different back ends (e.g. one SQL and another REST). 

In order to solve this problem, this library also includes a set of functions to perform joins.  
Currently the following type of joins are supported

* inner-join (equi, nautural, cross)
* outer-join (left, right, full)

Let's define some terms for our examples.

	user=> (def test-left 
			[{:name "Sean" :age 27} 
			 {:name "Ross" :age 27} 
			 {:name "Brian" :age 22}])
			
	user=> (def test-right 
			[{:owner "Sean" :item "Beer"} 
			 {:owner "Sean" :item "Pizza"}
			 {:owner "Ross" :item "Computer"}
			 {:owner "Matt" :item "Bike"}])
			
	user=> (def test-proj (proj :name :age :owner :item))

##inner-join (equi)
This is for performing inner joins.  The join value must exist in both lists. This function takes a left collection, 
a right collection, and at least one join function.  If only one join function is provided, it is used on both the left & right hand sides.	
###Signature

	(inner-join left-coll right-coll join-fn)
	(inner-join left-coll right-coll left-join-fn right-join-fn)
	
###Usage

	user=> (println (to-tab-str (map test-proj (inner-join test-left test-right :name :owner))))
	Ross	27	Ross	Computer
	Sean	27	Sean	Beer
	Sean	27	Sean	Pizza
	nil

Yup, this behaves properly.
	
##natural-join (natural)
This performs a natural join on the two collections.
###Signature
	(natural-join left-coll right-coll)
###Usage
	user=> (println (to-tab-str (map test-proj (natural-join test-left test-right))))
	nil

Hmmm... nothing.  This is what we would expect.  Let's see if we can make this work with a mapping operation on the test-right data.

	user=> (println (to-tab-str (map test-proj (natural-join test-left (map (trans :name #(% :owner)) test-right)))))
	Ross	27	Ross	Computer
	Sean	27	Sean	Beer
	Sean	27	Sean	Pizza
	nil
	
Perfect.

##cross-join (cross)
###Signature
	(cross-join left-coll right-coll)
###Usage
	user=> (println (to-tab-str (map test-proj (cross-join test-left test-right))))
	Sean	27	Sean	Beer
	Sean	27	Sean	Pizza
	Sean	27	Ross	Computer
	Sean	27	Matt	Bike
	Ross	27	Sean	Beer
	Ross	27	Sean	Pizza
	Ross	27	Ross	Computer
	Ross	27	Matt	Bike
	Brian	22	Sean	Beer
	Brian	22	Sean	Pizza
	Brian	22	Ross	Computer
	Brian	22	Matt	Bike
	nil

Yup, it's verbose output.

## left-outer-join
This is for performing left outer joins.  The join value must exist in the left hand list. This function takes a left collection,
a right collection, and at least one join function.  If only one join function is provided, it is used on both the left & right hand sides.
###Signature
	(left-inner-join left-coll right-coll join-fn)
	(left-innner-join left-coll right-coll left-join-fn right-join-fn)
	
###Usage
	user=> (println (to-tab-str (map test-proj (left-outer-join test-left test-right :name :owner))))
	Brian	22	null	null
	Ross	27	Ross	Computer
	Sean	27	Sean	Beer
	Sean	27	Sean	Pizza
	nil
	
## right-outer-join
This is for performing right outer joins.  The join value must exist in the right hand list. This function takes a left collection,
a right collection, and at least one join function.  If only one join function is provided, it is used on both the left & right hand sides.
###Signature
	(right-inner-join left-coll right-coll join-fn)
	(right-innner-join left-coll right-coll left-join-fn right-join-fn)
	
###Usage

	user=> (println (to-tab-str (map test-proj (right-outer-join test-left test-right :name :owner))))
	null	null	Matt	Bike
	Ross	27		Ross	Computer
	Sean	27		Sean	Beer
	Sean	27		Sean	Pizza
	nil

## full-outer-join
This is for performing full outer joins.  The join value may exist in either list. This function takes a left collection,
a right collection, and at least one join function.  If only one join function is provided, it is used on both the left & right hand sides.
###Signature
	(full-inner-join left-coll right-coll join-fn)
	(full-innner-join left-coll right-coll left-join-fn right-join-fn)
	
###Usage

	user=> (println (to-tab-str (map test-proj (full-outer-join test-left test-right :name :owner))))
	Brian	22		null	null	
	null	null	Matt	Bike
	Ross	27		Ross	Computer
	Sean	27		Sean	Beer
	Sean	27		Sean	Pizza
	nil
	
Booya.

#Pivoting a list of hashes
This was inspired by the pivot table feature of Excel.  It is very common to have to group, map, and reduce a list of tuples.  The pivot function is designed to handle all of the 
skeleton code, so that the developer only has to worry about three things:

1. How the data is grouped.
2. How each hash is mapped.
3. How each mapping is reduced.

##freq
`freq` is a special mapping function.  It constantly returns 1.  This is to enable counting in the pivot method.

##pivot
`pivot` has the following signature:

	(pivot coll grouping-fn & fns)

It is designed to take an alternating list of mapping and reducing functions.  Let's use the test-right data above as an example.

	user=> (pivot test-right :owner (comp count :item) +)
	{"Matt" [4], "Ross" [8], "Sean" [9]}

The data is grouped by :owner, each row is mapped by the `(comp count :item)` function (it computes the length of item), 
and the resulting lists are reduced by the `+` function.


