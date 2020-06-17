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
import java.io.IOException;
import java.lang.*;
import java.util.*;
import org.elasticsearch.search.SearchHit;

public interface DaoInterface {

    //search db using keywords and return SearchHit object containing highlight field
    public ImmutableList < SearchHit > fullTextSearch (String fileName, 
        String searchString, String field)throws IOException;

    //return a section of given index starting from start and length equal to 
    // given size
    public SearchHit[] getAll (String fileName, int start, int size) 
    throws IOException;

    //search db using regex and keywords and store back in db searchHits sorted by 
    // logLineNumber
    public boolean errorQuery (String filename) throws IOException;

    //checks whether index with name fileName already exists in the database;
    public boolean fileExists (String fileName) throws IOException;

    //Stores the jsonString at index with name filename and returns the logText 
    // of the document stored
    public String storeLogLine (String filename, String jsonString, String Id) 
    throws IOException;

    //Stores the log into the database if an index with name fileName does not exist in the 
    // database  and returns a string that contains the status of the log string whether the 
    // log string was stored in the database or not.
    public String checkAndStoreLog (String fileName, String log) throws IOException;

}