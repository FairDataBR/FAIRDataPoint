# FAIR Data Point Service
#
# Copyright 2015 Netherlands eScience Center in collaboration with
# Dutch Techcenter for Life Sciences.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#
# FAIR Data Point (FDP) Service exposes the following endpoints (URL paths):
#   /, /doc or /doc/     = Redirect to the RESTful Data API documentation (Swagger UI)
#   /fdp                 = returns FDP metadata
#   /catalog/{catalogID} = returns catalog metadata (default: catalog-01)
#   /dataset/{datasetID} = returns dataset metadata (default: breedb)
#
# This services makes extensive use of metadata defined by:
#   Data Catalog Vocabulary (DCAT, http://www.w3.org/TR/vocab-dcat/)
#   and Dublin Core Metadata Terms (DCMI, http://dublincore.org/documents/dcmi-terms/)
#

__author__  = 'Arnold Kuzniar'
__version__ = '0.2'
__status__  = 'Prototype'
__license__ = 'Apache Lincense, Version 2.0'

import os
import bottle
from bottle import (get, route, run, static_file, redirect)

project_dir = os.path.dirname(os.path.abspath(__file__))
metadata_dir = os.path.join(project_dir, 'metadata/')
doc_dir = os.path.join(project_dir, 'doc/')

@get(['/', '/doc', '/doc/'])
def default_page():
   redirect('/doc/index.html')

@get('/doc/<fname:path>')
def doc_files(fname):
   return static_file(fname, root=doc_dir)

@get('/fdp')
def FAIRDataPointMetadata():
   return static_file('fairdatapoint.ttl', root=metadata_dir)

@get('/catalog/<catalogID>')
def CatalogMetadata(catalogID):
   filename = '{catalogID}.ttl'.format(catalogID=catalogID)
   return static_file(filename, root=metadata_dir)

@get('/dataset/<datasetID>')
def DatasetMetadata(datasetID):
   filename = '{datasetID}.ttl'.format(datasetID=datasetID)
   return static_file(filename, root=metadata_dir)

if __name__ == '__main__':
   run(host='fdp.biotools.nl', port=8080, server='wsgiref', debug=True)
