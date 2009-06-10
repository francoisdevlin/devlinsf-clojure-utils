#Model Utils

Sean Devlin

June 10, 2009

This is the main overview for the model-utils documentation.

#def-model

This macro creates the following methods.

* find-records
* update-records
* delete-records
* kill-all-records

#filters

* def-filter

A thin wrapper over defn

* def-filter-factory

When a filter needs to be curried...

#mappings

* def-mapping

A thin wrapper over defn

* def-mapping-factory

When a mapping needs to be curried...

#transforms

Transforms take a hash-map in and return a map.  Think of them as decorators for an entire object, not just one or two methods.

##`(def-transform [name doc-string? & params])`

Takes a name, an optional doc-string, and a flat list of key/value pairs.  Defines a function _name_-transform.

Sample Usage:
	
	user=> (def-transform counter :count count)
	#'user/counter-transform
	user=> (def-transform title :title #(titleize (% :title)) :name #(titleize(% :name)))
	#'user/title-transform
	user=> (def sample-map {:name "sean devlin" :title "hacking clojure"})
	#'user/sample-map
	user=> (title-transform sample-map)
	{:name "Sean Devlin", :title "Hacking Clojure"}
	user=> ((comp title-transform counter-transform) sample-map)
	{:count 2, :name "Sean Devlin", :title "Hacking Clojure"}
	user=> ((apply comp [title-transform counter-transform]) sample-map)
	{:count 2, :name "Sean Devlin", :title "Hacking Clojure"}

#projections

Projections take a hash-map and turn in into a vector.  Combined with transforms they make a 1-2 punch of awesomeness.

##`(def-projection [name doc-string? & params])`

Takes a name, an optional doc-string, and a list of keywords.  Defines a function _name_-projection that expects a map as an input.
Each key is retrieved in order, and collected into a vector.  The vector is the returned.

Also, a variable _name_-columns is created, and the list of params is stored there.

#standard-functions

* keys-subset-mf
* vals-subset-mf

* multi-projection
Used to concatenate mapping operations.