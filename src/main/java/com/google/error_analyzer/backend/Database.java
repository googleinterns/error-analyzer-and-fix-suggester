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
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder.Field;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import java.util.*;
import org.elasticsearch.ElasticsearchException;;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.RegexpQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;

public class Database implements DaoInterface {

    private static final RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
    private static final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    private static final int windowSize = 10;
    private static final Logger logger = LogManager.getLogger(Database.class);

    //search db using keywords and return searchHits having highlight field added 
    public ArrayList < SearchHit > fullTextSearch(String fileName, String searchString, String field) throws IOException {
        int offset = 0;
        SearchHit[] searchHits = null;
        ArrayList < SearchHit > searchResult = new ArrayList();
        while (searchHits == null || searchHits.length != 0) {
            SearchRequest searchRequest = new SearchRequest(fileName);
            SimpleQueryStringBuilder simpleQueryBuilder = QueryBuilders.simpleQueryStringQuery(searchString);
            searchSourceBuilder.query(simpleQueryBuilder).size(windowSize).from(offset);
            HighlightBuilder highlightBuilder = addHighLighter(field);
            searchSourceBuilder.highlighter(highlightBuilder);
            searchRequest.source(searchSourceBuilder);
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            searchHits = hits.getHits();
            for (SearchHit hit: searchHits) {
                searchResult.add(hit);
            }
            offset += windowSize;
        }
        return searchResult;
    }
    // highlight searched text
    private HighlightBuilder addHighLighter(String field) {
        HighlightBuilder highlightBuilder = new HighlightBuilder().preTags("<b>").postTags("</b>");
        HighlightBuilder.Field highlightTitle = new HighlightBuilder.Field(field);
        highlightTitle.highlighterType("unified");
        highlightBuilder.field(highlightTitle);
        return highlightBuilder;
    }

    // return ArrayList of hit ids corresponding to given searchhit list
    public ArrayList < String > hitId(SearchHit[] searchHits) throws IOException {
        ArrayList < String > ids = new ArrayList();
        for (SearchHit hit: searchHits) {
            String id = hit.getId();
            ids.add(id);
        }
        return ids;
    }

    // return ArrayList of content for specified field  corresponding to given searchhit list
    public ArrayList < String > hitFieldContent(SearchHit[] searchHits, String field) throws IOException {
        ArrayList < String > fieldContent = new ArrayList();
        for (SearchHit hit: searchHits) {
            String resultString = String.valueOf(hit.getSourceAsMap().get(field));
            fieldContent.add(resultString);
        }
        return fieldContent;
    }

    //search db using user provided regex and return searchHits having highlight field added
    public SearchHit[] regexQuery(String filename, String regex) {
        return new SearchHit[0];
    }

    //return a section of given index starting from start and of length equal to given size
    public SearchHit[] getAll(int start, int size, String fileName) throws IOException {
        SearchRequest searchRequest = new SearchRequest(fileName);
        searchSourceBuilder.query(QueryBuilders.matchAllQuery()).size(size).from(start);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        return searchHits;
    }
    //returns hashmap of hit ids and highlighted content 
    public HashMap < String, String > getHighLightedText(ArrayList < SearchHit > searchHits, String field) throws IOException {
        HashMap < String, String > searchResult = new HashMap();
        for (SearchHit hit: searchHits) {
            Map < String, HighlightField > highlightFields = hit.getHighlightFields();
            HighlightField highlight = highlightFields.get(field);
            String fragmentString = (highlight.fragments())[0].string();
            searchResult.put(hit.getId(), fragmentString);
        }
        return searchResult;
    }

    //search db using regex and keywords and store back in db searchHits sorted by logLineNumber
    public boolean errorQuery(String fileName) throws IOException {
        return true;
    }

    //store identified errors back in database
    public void storeErrorLogs(String fileName, SearchHits hits) throws IOException {
        return;
    }

    //checks whether index with name fileName already exists in the database;
    public boolean FileExists(String fileName) throws IOException {
        return true;
    }


    //Stores the jsonString at index with name filename and returns the logText of the document stored
    public String storeLogLine(String Filename, String jsonString, String Id) throws IOException {
        return new String();
    }

    //Stores the log into the database if an index with name fileName does not exist in the database and returns a string that contains the status of the log string whether the log string was stored in the database or not.
    public String checkAndStoreLog(String fileName, String log) throws IOException {
        return new String();
    }
}

