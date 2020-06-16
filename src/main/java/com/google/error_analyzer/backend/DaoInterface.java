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

import org.elasticsearch.search.SearchHit;
import java.io.IOException;
import java.lang.*;
import java.util.*;

public interface DaoInterface {

    //search db using keywords and return searchHits having highlight field added 
    public ArrayList < SearchHit > fullTextSearch (String fileName, String searchString, String field) throws IOException;

    // return ArrayList of hit ids corresponding to given searchhit list
    public ArrayList < String > hitId (SearchHit[] searchHits) throws IOException;

    // return ArrayList of content for specified field  corresponding to given searchhit list
    public ArrayList < String > hitFieldContent (SearchHit[] searchHits, String field) throws IOException;

    //search db using user provided regex and return searchHits having highlight field added
    public SearchHit[] regexQuery (String filename, String regex);

    //return a section of given index starting from start and length equal to given size
    public SearchHit[] getAll (int start, int size, String fileName) throws IOException;

    //returns hashmap of hit ids and highlighted content 
    public HashMap < String, String > getHighLightedText (ArrayList < SearchHit > searchHits, String field) throws IOException;

    //search db using regex and keywords and store back in db searchHits sorted by logLineNumber
    public boolean errorQuery (String filename) throws IOException;

    //checks whether index with name fileName already exists in the database;
    public boolean FileExists (String fileName) throws IOException;


    //Stores the jsonString at index with name filename and returns the logText of the document stored
    public String storeLogLine (String filename, String jsonString, String Id) throws IOException;

    //Stores the log into the database if an index with name fileName does not exist in the database and returns a string that contains the status of the log string whether the log string was stored in the database or not.
    public String checkAndStoreLog (String fileName, String log) throws IOException;

}