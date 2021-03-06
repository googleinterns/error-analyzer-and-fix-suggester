/**Copyright 2019 Google LLC
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    https://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.google.error_analyzer.backend;

import com.google.common.collect.ImmutableList;
import com.google.error_analyzer.data.Document;
import java.io.IOException;
import java.lang.*;
import java.util.*;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.search.SearchHit;

public interface DaoInterface {

    //search db using keywords and return SearchHit object containing highlight field
    public ImmutableList < SearchHit > fullTextSearch (String fileName, String searchString,
    String field, int start, int size)throws IOException;

    //return a section of given index starting from start and length equal to 
    // given size
    public ImmutableList < SearchHit > getAll (String fileName, int start, 
    int size) throws IOException;

    // returns no of documents in an index
    public long getDocumentCount (String index) throws IOException;

    //search an index for errors using regex and keywords and store back in db
    //Returns name of the new index 
    public String findAndStoreErrors (String filename) throws IOException;

    //checks whether index with name fileName already exists in the database;
    public boolean fileExists (String fileName) throws IOException;

    //Stores the jsonString at index with name filename and returns the stored 
    // string
    public String storeLogLine (String filename, String jsonString, String id) 
    throws IOException;
    
    //Stores the documents into the database by performing multiple 
    //indexing operations
    public void bulkStoreLog(String fileName, 
        ImmutableList < Document > documentList) throws IOException;

    //returns the jsonString stored in the document
    public String getJsonStringById (String fileName, String id)
     throws IOException;

    //delete indices
    public String deleteIndices(String indexPrefix) throws IOException ;

    //fetch documents from index according to searchRequest
    public ImmutableList < SearchHit > getHitsFromIndex(SearchRequest searchRequest)
    throws IOException;

}