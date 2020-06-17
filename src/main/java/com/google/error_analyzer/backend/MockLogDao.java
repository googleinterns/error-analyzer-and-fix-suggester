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
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

public class MockLogDao implements DaoInterface {
    private final String[] database = new String[] {"Error: nullPointerException", 
    "info: start appengine","scheduler shutting down",
    "WARNING: An illegal reflective access operation has occurred", 
    "Severe: Could not find index file", "warning: NullPointerException"};

    //search db using keywords and return searchHits having highlight field added 
    @Override 
    public ImmutableList < SearchHit > fullTextSearch
    (String fileName, String searchString, String field) throws IOException 
    {
        ArrayList < SearchHit > searchResults = new ArrayList();
        String[] keyWords = searchString.split(" ");
        for (int i = 0; i < database.length; i++) {
            String dbEntry = database[i];
            String[] dbKeyWordsArray = dbEntry.split(" ");
            HashSet < String > dbKeyWordsSet 
                = new HashSet < > (Arrays.asList(dbKeyWordsArray));
            for (int j = 0; j < keyWords.length; j++) {
                String keyWord = keyWords[j];
                if (dbKeyWordsSet.contains(keyWord)) {
                    SearchHit hit = new SearchHit
                        (i,String.valueOf(i),null,new HashMap());
                    Text [] text = new Text[]{new Text(database[i])};
                    HashMap <String,HighlightField> highlight = new HashMap();
                    highlight.put(field,new HighlightField(field,text));
                    hit.highlightFields(highlight);
                    searchResults.add(hit);
                    break;
                }
            }
        }
        return ImmutableList.<SearchHit>builder() .addAll(searchResults) .build();
    }

    //return a section of given index starting from start and of length equal
    //  to given size
    @Override 
    public SearchHit[] getAll(String fileName, int start, int size) 
    throws IOException 
    {
        if (start >= database.length) {
            return new SearchHit[0];
        }
        if (start < 0) {
            start = 0;
        }
        int len = 0;
        int databaseLength = database.length;
        if (start + size - 1 < databaseLength) {
            len = size;
        }
        else {
            len = databaseLength - start;
        }
        SearchHit[] searchHits = new SearchHit[len];
        for (int idx = start; idx < len; idx++) {
            searchHits[idx] = new SearchHit(idx);
        }
        return searchHits;
    }

    //search db using regex and keywords and store back in db searchHits 
    // sorted by logLineNumber
    @Override 
    public boolean errorQuery(String filename) {
        return true;
    }

    //checks whether index with name fileName already exists in the database;
    @Override 
    public boolean fileExists(String fileName) {
        return true;
    }

    //Stores the jsonString at index with name filename and returns the logText 
    // of the document stored
    @Override 
    public String storeLogLine(String filename, String jsonString, String Id) {
        return new String();
    }

    //Stores the log into the database if an index with name fileName does not 
    // exist in the database and returns a string that contains the status of the 
    // log string whether the log string was stored in the database or not.
    @Override 
    public String checkAndStoreLog(String fileName, String log) {
        return new String();
    }
}