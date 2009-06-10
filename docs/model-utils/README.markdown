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
* def-filter-factory

#mappings

* def-mapping
* def-mapping-factory

#transforms

Transforms take a hash-map in and return a map.  Think of them as decorators for an entire object, not just one or two methods.

#projections

Projections take a hash-map and turn in into a vector.

#standard-functions

* keys-subset-mf
* vals-subset-mf