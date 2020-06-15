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

import com.google.error_analyzer.backend.StoreLogs;
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
import  org.elasticsearch.action.get.GetResponse;
import  org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import java.util.*;
import org.elasticsearch.ElasticsearchException;;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import com.google.error_analyzer.data.Keywords;
import com.google.error_analyzer.data.RegexExpressions;
import org.elasticsearch.index.query.RegexpQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
 
public class BooleanQuery {

    private static final Logger logger = LogManager.getLogger(BooleanQuery.class);
    private final String logTextField = "logText";

    //search db using regex and keywords and store back in db searchHits sorted by logLineNumber
    public SearchRequest createSearchRequest(String fileName) {
        SearchRequest searchRequest = new SearchRequest(fileName);
        Keywords errorKeywords = new Keywords();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        RegexExpressions regexExpressions = new RegexExpressions();
        String regexQueryString = regexExpressions.getQueryString();
        RegexpQueryBuilder regexQuery = new RegexpQueryBuilder(logTextField,regexQueryString);
        String keywordsQueryString = errorKeywords.getQueryString();
        QueryBuilder fulltextQuery = QueryBuilders.matchQuery(logTextField, keywordsQueryString);
        QueryBuilder errorQuery = new BoolQueryBuilder()
            .minimumShouldMatch(1)
            .should(regexQuery)
            .should(fulltextQuery);
        searchSourceBuilder.query(errorQuery); 
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }
    
    //Sort the searchHits acc to ids (which is also logLineNumber) and return document json strings
    public ArrayList<String> sortErrorDocuments(SearchHits hits) {
        ArrayList<Integer> searchHitIds = new ArrayList<>();
        HashMap<Integer, String> hitsHashMap = new HashMap();
        for (SearchHit hit : hits) {
            Integer id = Integer.parseInt(hit.getId());
            searchHitIds.add(id);
            String jsonDocument = hit.getSourceAsString();
            hitsHashMap.put(id, jsonDocument);
        }
        Collections.sort(searchHitIds);
        ArrayList<String> sortedSourceStrings = new ArrayList();
        for (Integer id : searchHitIds) {
            String errorJsonString = hitsHashMap.get(id);
            sortedSourceStrings.add(errorJsonString);
        }
        return sortedSourceStrings;
    }


}