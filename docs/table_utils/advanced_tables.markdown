This documents some advanced uses of table-utils.

In order to understand joining, I'd like to write about how the join engine works.  It's actually very simple.

	(defn join-worker
  	"This is an internal method to be used in each join function."
  	[join-style left-coll right-coll left-join-fn right-join-fn]
     	(let 	[indexed-left (group-by left-join-fn left-coll)
	   			 indexed-right (group-by right-join-fn right-coll)
	   			 desired-joins (join-style (keys indexed-left) (keys indexed-right))]
       			(reduce concat 
					(map 
						(fn [joined-value]
			     			(for [left-side  (get indexed-left joined-value [{}])
			 			  		  right-side (get indexed-right joined-value [{}])]
			       				  (merge left-side right-side)))
			   			desired-joins))))

You'll notice that there are three basic stages

1.  Indexing the collections
2.  Determining the appropriate set of indexes to use
3.  Repeated performing a cross-join

I'd like to spend some time discussing the indexing collections today.  It is one of the most important aspects of my join engine.  Let's consider a real world example
from my job.

Over the past few weeks I have been trying to coordinate manufacturing information for my company.  As such I have been trying to make sense of
inventory levels as they relate to production.  Let's consider the following inventory table with 4 columns.

	:part-num	:rev	:qty	:name
	123-01		A		15	Ancient Widget
	123-01		B		15	Ancient Widget
	123-02		A		0	Old Widget
	123-03		A		2	Current Widget
	123-03		B		0	Current Widget
	... Other Parts ...
	
Now, the design control systems are very particular.  It requires inventory to match part-number & rev(ision) in order to be used.  So, in order to index this
table we would need to group it as follows

	;Assume inventory-table stores a list of maps
	;Returns an indexed inventory-table
	user=>(group-by (juxt :part-num :rev) inventory-table)
	
Now, in our example let's assume that we need to build the 123-03 B Widget.  If we do a lookup on this we'll find... no inventory.  Great.  However, our lead engineer
tells us that the 123-03 A is the exact same part, except it doesn't have a product label on it.  It'll do the exact same job as the B version.  Let's make a gross
assumption that all revs are interchangeable.  Our grouping function becomes

	user=>(group-by :part-num inventory-table)
	
And, if we investigate the 123-03 part, we find that we have 2 in inventory.  Awesome!

Of course, no story would be complete without a third act.  Our boss comes in, telling us that he needs 25 of 123-03 Widgets immediately.  We tell him we've only got 2 in house, 
and it will take months to purchase new material.  The boss asks if there is anything we can do.  He mentions that even older versions of the part will be acceptable.

Now, I need to reveal an extra detail about our part numbering system.  As you can see, each part number begins with a 123- prefix.  This is what is known as a "smart part number",
in which the prefix means something.  In my real life case, any part with the same prefix is part of the same design history.  The 123-01 part is an ancient version of the 123-03
part.  So, our grouping function will look like this

	;Assume str-utils2 is required as s
	user=>(group-by (comp #(s/take % 3) :part-num) inventory-table)
	
Now, all of 123* parts are grouped together.  Fantastic.

However, my boss is still not happy.  He hands me the following list of parts

	:pn		:qty-req
	123-03	25
	124-01	20
	200-10	5
	205-10	10
	999-01	1
	
I have to do determine the availability for all of these parts.  Fortunately, my boss has the same flexibility, and ANY version of the part will do.  So, we can index the
list of "boss parts" as follows

	user=>(group-by (comp #(s/take % 3) :pn) boss-parts)
	
Now that we've determined our indexing fns, we can run the report the boss needs

	;Left outer join w/ the boss parts on the left will
	;provide a complete list of stuff the boss needs	
	user=>(left-outer-join 
			boss-parts 
			inventory-table 
			(comp #(s/take % 3)
					:part-num)
			(comp #(s/take % 3)
					:pn))

I've been calling these operations "approximate joins".  I get my boss the material he wants, and the day is finally saved!

While I am enjoying solving a problem for my boss, some of you may be wondering something.  If the 123* prefix indicates something, why isn't it in a different field?  Isn't 
this a case of bad table design/normalization?  What kind of database system does this, anyway?  Consider the following points

	1 LEGACY DATABASES DO THIS ALL OF THE TIME
	2 WORKING WITH LEGACY SYSTEMS PAYS THE BILLS
	3 GOTO 2

I will skip my rant about poorly designed schemas & those who design them, except to say that we are all on mistake away from creating a terrible design that goes undetected
for years. The reality is developers must work with them constantly.  It is difficult if not impossible to perform the approximate join
with SQL, and that's not always an option in heterogeneous systems anyway.  

Since the mechanics of the joins is abstracted away, all that is exposed is the application specific indexing.  This applies equally to SQL, REST, Excel, ERP, CSV, and JSON
generated data sets.  All that is left of is defining a mapping operation, which every lisper is very, very familiar with.

If perfected, I think an out-of-the-box join engine could be a real selling point for Clojure.  No, it does not even come close to the awesomeness of the STM.  However, it is
easier to understand, and I think it's closer to a real pain point many Java programmers have.  It could serve as a simple, concrete example of when OO is the wrong solution, and open minds to listen
to the rest of the Clojure story.

Just another $.02
Sean

PS - This join proposal, and lots of other code I now write, is not possible w/o pervasive persistent data structures.  I do not know how I would begin
to write this code in non-FP languages.  Thanks for Clojure!