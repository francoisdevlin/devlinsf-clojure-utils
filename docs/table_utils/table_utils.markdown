#table-utils

Sean Devlin

Aug 29, 2009

Namespace: lib.devlinsf.table-utils

This is a collection of map utility functions that I use when manipulating map data.  Since much of clojure is designed around manipulating a list of tuples, 
I find these very handy.  This documentation reviews how to do the following things.

* joining tables
* pivoting tables

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

## Join Caveats
There are currently a few kinks in the join engine.

###Name Collisions
The join engine does not handle key collisions well.   Consider the following two tables

	table:purchase_orders
	id (int)
	state (int) ;In this case state means status
	vendor_id (int)
	
	table:vendor
	id (int)
	street_adress (string)
	state (string) ;In this case state mean US state
	name (string)
	
When these two tables are joined, the join engine will blindly merge the maps in each row, and the state column will be nonsensical.  It is recommended
to use map-keys to add a prefix do each key, in order to preserve row information.
	
### Performance

These functions haven't been tuned yet.  It is recommended to use a partitioning/map/reduce scheme to join over large data sets.

#Pivoting a list of hashes
This was inspired by the pivot table feature of Excel.  It is very common to have to group, map, and reduce a list of tuples.  The pivot function is designed to handle all of the 
skeleton code, so that the developer only has to worry about three things:

First, let's consider pivoting.  This is best show by example.  Suppose you have a table with the following columns

	table:sales
	order_id (int)
	product_id (int)
	product_cost (double)
	quantity (double)

Now, we use c.c.sql-utils to query the entire table.

	;runs "SELECT * FROM SALES;"
	(def sales-table (a-call-to-a-db ...))

How would we get the total sales for each product?  Perhaps like this:

	(apply merge-with + 
  		(map 
    		#(hash-map 
      			(:product-id %) 
      			(* (:product-cost %) (:quantity %)))
    	sales-table))

This involves 3 distinct operation

1.  (:product-id %)
  This is the grouping function.  It determines which product the order is associated with.

2.  (* (:product-cost %) (:quantity %))
  This is the mapping function.  It determines the value for each row.

3.  +
  This is the reduction function.  It combines every item in a group.

We are now ready to define the pivot function

	(defn pivot [coll grouping-fn mapping-fn reduce-fn] 
  		(apply merge-with reduce-fn 
    		(map 
      			#(hash-map 
        			(grouping-fn %) 
        			(mapping-fn %))
      		sales-table)))

This replaces our s-exp above with

	(pivot sales-table :product-id (* (:product-cost %) (:quantity %)) +)

Now, it is very easy to change the groupings, mapping, or reductions.  For example, what if we wanted to count the quantity of units sold?  The pivot operation becomes

	(pivot sales-table :product-id :quantity +)

Very clean, IMHO.

Now, suppose we wasted to count the number of times each product was ordered.  That is, how many rows is each product_id in?  We simply write the pivot operation as follows

	(pivot sales-table :product-id (constantly 1) +)

This frequency pivoting operation shows up often enough that I created the following alias

	(def freq (constantly 1))
	(pivot sales-table :product-id freq +)

In fact, c.c.seq-util/frequencies could be written in terms of pivot

	(defn frequencies
  	[coll]
  		(pivot coll identity freq +))

