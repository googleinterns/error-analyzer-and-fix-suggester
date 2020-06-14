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


import java.io.IOException;
import java.lang.*;
import java.util.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

public class MockDatabase implements DaoInterface {
    private final String[] database = new String[] {"error: null pointer Exception", "info: start appengine","scheduler shutting down",
     "WARNING: An illegal reflective access operation has occurred"};

    //search db using keywords and return searchHits having highlight field added 
    public ArrayList<SearchHit> fullTextSearch(String fileName, String searchString, String field) throws IOException {
        ArrayList<SearchHit> searchResults = new ArrayList();
        String[] keyWords = searchString.split(" "); 
        for(int i=0; i < database.length; i++) {
            String dbEntry = database[i];
            String[] dbKeyWordsArray = dbEntry.split(" ");
            HashSet<String> dbKeyWordsSet = new HashSet<>(Arrays.asList(dbKeyWordsArray)); 
            for(int j=0; j< keyWords.length; j++) {
                String keyWord = keyWords[j];
                if(dbKeyWordsSet.contains(keyWord))
                {
                    searchResults.add(new SearchHit(i));
                    break;
                }
            }
        }
        return searchResults;
    }

     // return ArrayList of hit ids corresponding to given searchhit list
    public ArrayList<String> hitId(SearchHit[] searchHits) throws IOException {
        ArrayList<String> result = new ArrayList();
        for(SearchHit hit: searchHits){
            result.add(hit.getId());
        }
        return result;
    }

    // return ArrayList of content of specified field  corresponding to given searchhit list
    public ArrayList<String> hitFieldContent(SearchHit[] searchHits, String field) throws IOException {
        ArrayList<String> result = new ArrayList();
        for(SearchHit hit: searchHits){
            int id=Integer.parseInt(hit.getId());
            result.add(database[id]);
        }
        return result;
    }

    //search db using user provided regex and return searchHits having highlight field added
    public SearchHit[] regexQuery(String filename, String regex) {
        return new SearchHit[0];
    }

    //return a section of given index starting from start and of length equal to given size
    public SearchHit[] getAll(int start, int size, String fileName) throws IOException {
        if(start>=database.length)
        {
            return new SearchHit[0];
        }
        if(start<0)
        {
            start=0;
        }
        int len = 0;
        int databaseLength=database.length;
        if(start+size-1 < databaseLength)
            len=size;
        else
            len=databaseLength-start;
        SearchHit[] searchHits = new SearchHit[len];
        for(int idx=start;idx<len;idx++)
        {
            searchHits[idx]=new SearchHit(idx);
        }
        return searchHits;
    }

    //returns hashmap of hit ids and highlighted content 
    public HashMap<String,String> getHighLightedText(ArrayList<SearchHit> searchHits, String field) throws IOException {
        HashMap<String,String> result = new HashMap();
        for(SearchHit hit: searchHits){
            String stringId=hit.getId();
            int id=Integer.parseInt(stringId);
            result.put(stringId,database[id]);
        }
        return result;
    }

    //search db using regex and keywords and store back in db searchHits sorted by logLineNumber
    public void errorQuery(String filename) {

    }

    //checks whether index with name fileName already exists in the database;
    public boolean FileExists(String fileName) {
        return true;
    }


    //Stores the jsonString at index with name filename and returns the logText of the document stored
    public String storeLogLine(String filename, String jsonString) {
        return new String();
    }

    //Stores the log into the database if an index with name fileName does not exist in the database and returns a string that contains the status of the log string whether the log string was stored in the database or not.
    public String checkAndStoreLog(String fileName, String log) {
        return new String();
    }

}