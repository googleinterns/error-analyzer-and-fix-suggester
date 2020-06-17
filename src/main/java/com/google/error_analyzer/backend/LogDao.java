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
import java.io.IOException;
import java.util.*;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.SimpleQueryStringBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;


public class LogDao implements DaoInterface {

    private static final RestHighLevelClient client = new RestHighLevelClient
        (RestClient.builder(new HttpHost("localhost", 9200, "http")));
    private static final SearchSourceBuilder searchSourceBuilder 
        = new SearchSourceBuilder();
    private static final int windowSize = 10;
    private static final Logger logger = LogManager.getLogger(LogDao.class);

    //search db using keywords and return searchHits having highlight field added  
    @Override 
    public ImmutableList < SearchHit > fullTextSearch(
    String fileName, String searchString, String field)throws IOException {
        int offset = 0;
        SearchHit[] searchHits = null;
        Builder<SearchHit> searchResultBuilder = ImmutableList.<SearchHit>builder();

        // we check for matching keywords in a specific windowsize in each 
        // iteration and do this until the the end of index .this way we 
        // have traverse whole index 
        while (true) {
            SearchRequest searchRequest = new SearchRequest(fileName);
            SimpleQueryStringBuilder simpleQueryBuilder = 
                QueryBuilders.simpleQueryStringQuery(searchString);
            searchSourceBuilder.query(simpleQueryBuilder)
                .size(windowSize).from(offset);
            searchSourceBuilder.highlighter(addHighLighter(field));
            searchRequest.source(searchSourceBuilder);
            SearchResponse searchResponse = 
                client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            searchHits = hits.getHits();

            if( searchHits.length == 0) {
                break;
            }
            for (SearchHit hit: searchHits) {
                searchResultBuilder.add(hit);
            }
            offset += windowSize;
        }
        return searchResultBuilder.build();
    }

    //return a section of given index starting from start and of 
    // length equal to given size
    @Override 
    public SearchHit[] getAll(String fileName, int start, int size) 
    throws IOException {
        SearchRequest searchRequest = new SearchRequest(fileName);
        searchSourceBuilder.query(QueryBuilders.matchAllQuery())
            .size(size).from(start);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = 
            client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        return searchHits;
    }

    //search db using regex and keywords and store back in db searchHits
    //  sorted by logLineNumber
    @Override 
    public boolean errorQuery(String fileName) throws IOException {
        return true;
    }

    //store sorted identified errors back in database
    public void storeErrorLogs(String fileName, SearchHits hits) throws IOException {
        return;
    }
    
    //checks whether index with name fileName already exists in the database;
    public boolean fileExists(String fileName) throws IOException {
        return true;
    }

    //Stores the jsonString at index with name filename and returns the logText 
    // of the document stored
    @Override 
    public String storeLogLine(String Filename, String jsonString, String Id)
    throws IOException {
        return new String();
    }

    //Stores the log into the database if an index with name fileName does not exist
    //  in the database and returns a string that contains the status of the log 
    // string whether the log string was stored in the database or not.
    @Override 
    public String checkAndStoreLog(String fileName, String log) throws IOException {
        return new String();
    }

    // highlight searched text
    private HighlightBuilder addHighLighter(String field) {
        HighlightBuilder highlightBuilder = new HighlightBuilder()
            .preTags("<b>").postTags("</b>");
        HighlightBuilder.Field highlightTitle = new HighlightBuilder.Field(field);
        highlightTitle.highlighterType("unified");
        highlightBuilder.field(highlightTitle);
        return highlightBuilder;
    }
}

