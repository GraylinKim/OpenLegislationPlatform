Open Legislation Platform
=========================

This platform processes XML documents into fully indexed and preserialized SOLR documents which are then made available through a configurable RESTful API. Additional tools, components, and libraries may be packaged as developed. This platform is based largely on the experience of developing the New York State Senate's OpenLegislation web service but is not a Senate project nor is it affiliated with the Senate.

The project is Licensed under the MIT License. See the LICENSE file for details.

Project Goals
--------------

To provide a data/structure agnostic platform for delivering Legislative data via an full featured web API that allows for the formation of complex queries across the many facets of the legislative process. All data should be fully indexed across a fine grained set of fields with support for full-text searching and range queries.

Project Components
-------------------

Currently comprised of 3 components:
* a configurable web API servlet
* a xml input processor
* and a SOLR database that acts as the mediator between the two.

In this way the platform can be thought of a wrapper around the SOLR database.
