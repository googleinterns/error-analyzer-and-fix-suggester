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
import com.google.error_analyzer.backend.BooleanQuery;
import com.google.error_analyzer.backend.LogDaoHelper;
import com.google.error_analyzer.data.constant.LogFields;
import com.google.error_analyzer.data.Document;
import java.io.IOException;
import java.util.*;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
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
    private static final Logger logger = LogManager.getLogger(LogDao.class);

    //search db using keywords and return searchHits having highlight field added  
    @Override 
    public ImmutableList < SearchHit > fullTextSearch(String fileName, 
    String searchString, String field, int start, int size)throws IOException {
        SearchRequest searchRequest = new SearchRequest(fileName);
        SimpleQueryStringBuilder simpleQueryBuilder = 
            QueryBuilders.simpleQueryStringQuery(searchString);
        searchSourceBuilder.query(simpleQueryBuilder)
            .size(size).from(start).sort(LogFields.LOG_LINE_NUMBER);
        searchSourceBuilder.highlighter(addHighLighter(field));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = 
            client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits= searchResponse.getHits();
        SearchHit[] searchHits= hits.getHits();
        return ImmutableList.copyOf(Arrays.asList(searchHits)); 
    }

    //return a section of given index starting from start and of 
    // length equal to given size
    @Override 
    public ImmutableList<SearchHit> getAll(String fileName, int start, int size) 
    throws IOException {
        SearchRequest searchRequest = new SearchRequest(fileName);
        searchSourceBuilder.query(QueryBuilders.matchAllQuery())
            .size(size).from(start);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = 
            client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        return ImmutableList.copyOf(Arrays.asList(searchHits));
    }

    // returns no of documents in an index
    @Override 
    public long getDocumentCount (String index) throws IOException {
        CountRequest countRequest = new CountRequest(index);
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        countRequest.source(searchSourceBuilder);
        CountResponse countResponse = 
            client.count(countRequest, RequestOptions.DEFAULT);
        return countResponse.getCount();
    }

    //search an index for errors using regex and keywords and store back in db
    //Returns name of the new index 
    @Override 
    public String findAndStoreErrors(String fileName) throws IOException {
        SearchHits hits = findErrors(fileName);
        String errorFileName = LogDaoHelper.getErrorIndexName(fileName);
        storeErrors(errorFileName, hits);
        logger.info("Errors stored in index ".concat(errorFileName));
        return errorFileName;
    }

    //checks whether index with name fileName already exists in the database; 
    @Override
    public boolean fileExists(String fileName) throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest();
        getIndexRequest.indices(fileName);
        boolean indexExists = 
            client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        return indexExists;
    }

    //Stores the jsonString at index with name filename and returns the stored
    // string
    @Override
    public String storeLogLine(String fileName, String jsonString, String id)
    throws IOException {
        IndexRequest indexRequest = new IndexRequest(fileName);
        indexRequest.id(id);
        indexRequest.source(jsonString, XContentType.JSON);
        client.index(indexRequest, RequestOptions.DEFAULT);
        GetRequest getRequest = new GetRequest(fileName, id);
        GetResponse getResponse =
            client.get(getRequest, RequestOptions.DEFAULT);
        return getResponse.getSourceAsString();
    }

    //fetch documents from index according to searchRequest
    @Override
    public ImmutableList < SearchHit > getHitsFromIndex(SearchRequest searchRequest)
    throws IOException {
        Builder < SearchHit > searchResultBuilder = ImmutableList.< SearchHit > builder();
        SearchResponse searchResponse = client
            .search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        for (SearchHit hit : hits){
            searchResultBuilder.add(hit);
        }
        return searchResultBuilder.build();
    }
    
    //Stores the documents into the database by performing multiple indexing operations
    //in a single API call
    @Override
    public void bulkStoreLog(String fileName, 
    ImmutableList < Document > documentList) throws IOException {
        BulkRequest request = new BulkRequest();
        for (Document document: documentList) {
            String jsonString = document.getJsonString();
            IndexRequest indexRequest = new IndexRequest(fileName);
            String id = document.getID();
            indexRequest.id(id);
            indexRequest.source(jsonString, XContentType.JSON);
            request.add(indexRequest);
        }
        client.bulk(request, RequestOptions.DEFAULT);;
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

    //find errors in a given index
    private SearchHits findErrors(String fileName) 
    throws IOException {
        logger.info("Finding errors in ".concat(fileName));
        BooleanQuery booleanQuery = new BooleanQuery();
        SearchRequest searchRequest = booleanQuery.createSearchRequest(fileName);
        SearchResponse searchResponse = client
            .search(searchRequest, RequestOptions.DEFAULT);
        return searchResponse.getHits();
    }

    //store errors found in the log file
    private void storeErrors(String errorFileName, SearchHits hits)
    throws IOException {
        Integer numberOfErrorsFound = hits.getHits().length;
        logger.info("Storing ".concat(numberOfErrorsFound.toString()).concat(" into database"));
        for (SearchHit hit : hits) {
            String jsonSource =  hit.getSourceAsString();
            String id = hit.getId();
            storeLogLine(errorFileName, jsonSource, id);
        }
    }
}