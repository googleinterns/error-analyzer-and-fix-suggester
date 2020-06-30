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
import com.google.common.collect.ImmutableList.Builder;
import com.google.error_analyzer.backend.LogDaoHelper;
import com.google.error_analyzer.data.Document;
import com.google.error_analyzer.data.Index;
import java.io.IOException;
import java.lang.*;
import java.util.*;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

public class MockLogDao implements DaoInterface {
    //database is an example of input index file
    private final String[] database = new String[] {"Error: nullPointerException", 
    "info: start appengine","scheduler shutting down",
    "WARNING: An illegal reflective access operation has occurred", 
    "Severe: Could not find index file", "warning: NullPointerException"};
    //logDatabase is the list of all indices stored in the database
    private ArrayList < Index > logDatabase = new ArrayList < Index >();
    //errorFile is for storing error logs after findAndStoreErrors is executed
    public ArrayList < String > errorFile;

    //search db using keywords and return searchHits having highlight field added 
    @Override 
    public ImmutableList < SearchHit > fullTextSearch(String fileName, 
    String searchString, String field, int start, int size) throws IOException {
        Builder < SearchHit > searchResultBuilder = ImmutableList.< SearchHit >builder();
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
                    HashMap < String,HighlightField > highlight = new HashMap();
                    highlight.put(field,new HighlightField(field,text));
                    hit.highlightFields(highlight);
                    searchResultBuilder.add(hit);
                    break;
                }
            }
        }
        return searchResultBuilder.build();
    }

    //return a section of given index starting from start and of length equal
    //  to given size
    @Override 
    public ImmutableList < SearchHit > getAll( String fileName,
    int start, int size) throws IOException {
        if (start >= database.length) {
            return ImmutableList.< SearchHit >builder().build();
        }
        Builder < SearchHit > resultBuilder = 
            ImmutableList.< SearchHit >builder();
        for (int idx = start; idx < start+size ; idx++) {
            resultBuilder.add(new SearchHit(idx));
        }
        return resultBuilder.build();
    }
  
    // returns no of documents in an index
    public long getDocumentCount (String index) throws IOException {
        return 0l;
    }

    //search an index for errors using regex and keywords and store back in db
    //Returns name of the new index 
    @Override 
    public String findAndStoreErrors(String fileName) {
        String errorFileName = LogDaoHelper.getErrorIndexName(fileName);
        errorFile.add("Error: nullPointerException");
        errorFile.add("Severe: Could not find index file");
        errorFile.add("warning: NullPointerException");
        return errorFileName;
    }

    //checks whether index with name fileName already exists in the database;
    @Override
    public boolean fileExists(String fileName) {
        Iterator < Index > indexListIterator = logDatabase.iterator();
        while (indexListIterator.hasNext()) {
            String indexName = (indexListIterator.next()).getIndexName();
            if (fileName.equals(indexName)) {
                return true;
            }
        }
        return false;
    }

    //stores the jsonString at index with name filename and returns the stored 
    //string
    @Override
    public String storeLogLine(String fileName, String jsonString, String id) {
        String result = new String();
        Index index = new Index();
        index.setIndexName(fileName);
        Document document = new Document(id, jsonString);
        index.addDocument(document);
        logDatabase.add(index);
        Iterator < Index > indexListIterator = logDatabase.iterator();
        while (indexListIterator.hasNext()) {
            Index searchIndex = indexListIterator.next();
            if (fileName.equals(searchIndex.getIndexName())) {
                ArrayList < Document > DocList = searchIndex.getDocumentList();
                Iterator < Document > docListIterator = DocList.iterator();
                while (docListIterator.hasNext()) {
                    Document doc = docListIterator.next();
                    if (id.equals(doc.getID())) {
                        result = doc.getJsonString();
                    }
                }
            }
        }
        return result;
    }

    //fetch documents from index according to searchRequest
    @Override
    public ImmutableList < SearchHit > getHitsFromIndex(SearchRequest searchRequest) {
        Builder < SearchHit > searchResultBuilder = ImmutableList.< SearchHit > builder();
        return searchResultBuilder.build();
    }

    //Stores the documents into the database by performing multiple indexing operations
    @Override
    public void bulkStoreLog(String fileName,
     ImmutableList < Document > documentList) {
        Index index = new Index();
        index.setIndexName(fileName);
        for (Document document: documentList) {
            index.addDocument(document);
        }
        logDatabase.add(index);
    }

    //returns the jsonString stored in the document
    @Override
    public String getJsonStringById (String fileName, String id) {
        String result = null;
        Iterator < Index > indexListIterator = logDatabase.iterator();
        while (indexListIterator.hasNext()) {
            Index searchIndex = indexListIterator.next();
            if (fileName.equals(searchIndex.getIndexName())) {
                ArrayList < Document > DocList = searchIndex.getDocumentList();
                Iterator < Document > docListIterator = DocList.iterator();
                while (docListIterator.hasNext()) {
                    Document doc = docListIterator.next();
                    if (id.equals(doc.getID())) {
                        result = doc.getJsonString();
                    }
                }
            }
        }
        return result;
    }
    

}