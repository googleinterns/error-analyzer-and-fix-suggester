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


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.http.HttpHost;
import java.io.IOException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.SimpleQueryStringBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import java.util.*;
 
public class Database implements DaoInterface {

    private static final RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("35.194.181.238", 9200, "http")));
    private static final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

    //search db using keywords and return searchHits having highlight field added 
    public ArrayList<SearchHit> fullTextSearch(String fileName, String SearchString){
        return new ArrayList();
    };

    //search db using user provided regex and return searchHits having highlight field added
    public SearchHit[] regexQuery(String filename, String regex){
        return new SearchHit[0];
    };

    //return a section of given index starting from start and of length equal to given size
    public SearchHit[] getAll(int start, int size, String fileName) throws IOException{
        SearchRequest searchRequest = new SearchRequest(fileName);
        searchSourceBuilder.query(QueryBuilders.matchAllQuery()).size(size).from(start);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        return searchHits;
    };

    //search db using regex and keywords and store back in db searchHits sorted by logLineNumber
    public void errorQuery(String filename){
        return ;
    };

    //checks whether index with name fileName already exists in the database;
    public boolean FileExists(String fileName){
        return false;
    };


    //Stores the jsonString at index with name filename and returns the logText of the document stored
    public String storeLogLine(String filename, String jsonString){
        return new String();
    };

    //Stores the log into the database if an index with name fileName does not exist in the database and returns a string that contains the status of the log string whether the log string was stored in the database or not.
    public String checkAndStoreLog(String fileName, String log){
        return new String();
    };
}