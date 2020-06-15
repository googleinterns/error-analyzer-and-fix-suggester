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
import com.google.error_analyzer.backend.MockErrorQuery;
import com.google.error_analyzer.data.Document;
import com.google.error_analyzer.data.Index;

public class MockDatabase implements DaoInterface {
    private final String[] database = new String[] {"Error: nullPointerException", "info: start appengine","scheduler shutting down",
     "WARNING: An illegal reflective access operation has occurred", "Severe: Could not find index file", "warning: NullPointerException"};
    public ArrayList<String> errorDatabase;
    private ArrayList<Index> LogDatabase=new ArrayList<Index>();

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
            String stringId=hit.docId()+"";
            int id=hit.docId();
            result.put(stringId,database[id]);
        }
        return result;
    }

    //search db using regex and keywords and store back in db searchHits sorted by logLineNumber
    public boolean errorQuery(String filename) {
        MockErrorQuery  mockQuery = new MockErrorQuery();
        ArrayList<String> searchResults = new ArrayList();
        for (int i = 0; i < database.length; i++) {
            
            String document = database[i];
            if (mockQuery.matchesCondition(database[i])) {
                searchResults.add(document);
            }
        }
        errorDatabase = searchResults;
        return true;
    }

    //checks whether index with name fileName already exists in the database;
    public boolean FileExists(String fileName) {
        Iterator<Index> iter = LogDatabase.iterator();
        while (iter.hasNext()) { 
            if(fileName.equals((iter.next()).getIndexName())){
                return true;
            }
        }
        return false;
    }


    //Stores the jsonString at index with name filename and returns the logText of the document stored
    public String storeLogLine(String fileName, String jsonString, String Id) {
        String result = new String();
        Index index=new Index();
        index.setIndexName(fileName);
        Document document = new Document (Id, jsonString);
        index.addDocument(document);
        LogDatabase.add(index);
        ArrayList <Document> DocList = index.getDocumentList();
        Iterator<Document> it = DocList.iterator();
            while (it.hasNext()){
                Document doc = it.next();
                if(Id.equals(doc.getID())){
                    result = doc.getJsonString();
                }
            }
        return result;
    }


}