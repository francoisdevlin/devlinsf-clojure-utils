#Predicate Utilities

Sean Devlin

June 12 2008

Namespace: lib.devlinsf.predicate-utils

This is a namespace to assist with predicate creation and composition in clojure.  The AND & OR macros are good for testing, but not filtering.
This library attempts to fill those gaps.

#(same-entries[ & params]...)
Note - The name of this method sucks.

This function creates a predicate from a hash-map.

	user=> 	(def test-map 
				{:name "Sean" 
				:job "Software Nerd" 
				:gender :male 
				:favorite-team "Pittsburgh Steelers"})
				
Let look at an example:

	user=> ((same-entries {:name "Sean"}) test-map)
	true
				
The same entries method created a filter that determined if its input had a :name key of "Sean".  Since `test-map` does have such a key, it returned true.  Let's look at a
second example.

	user=> ((same-entries {:favorite-team "Pittsburgh Steelers" :gender :male}) test-map)
	true
	
This time we were looking for two entries.  Since the `test-map` has the exact same keys, it matched.  Look at a third example:

	user=> ((same-entries {:job "Software Nerd" :gender :female}) test-map)
	false
	
While our test-map is a Software Nerd, it isn't female.  The test fails.
	
This function can also take a list of keys
	
	user=> ((same-entries :name "Sean") test-map)
	true
	
#(predicate-chain [& predicates]...)

This function composes a list of predicates with a logical AND.  Look at the following example:

	user=> ((predicate-chain 
				(same-entries {:job "Software Nerd"}) 
				#(= (count %) 4)) 
			test-map)
	true

This is primarily useful for composing predicates for filtering operations.  It is based on the `every?` method, so it fails fast.

#(predicate-fan [& predicates]...)	

This function composes a list of predicates with a logical OR.  Look at the following example:

	user=> ((predicate-fan (same-entries {:job "Software Nerd"}) (same-entries {:gender :female})) test-map)
	true

This is primarily useful for composing predicates for filtering operations.  It is based on the `some` method, so it succeeds fast.