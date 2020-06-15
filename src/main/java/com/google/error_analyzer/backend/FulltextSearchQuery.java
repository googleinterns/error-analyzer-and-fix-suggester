// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.error_analyzer.backend;

import com.google.error_analyzer.data.Keywords;
import com.google.error_analyzer.data.ErrorLine;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Map;
import java.io.IOException;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
* This class is for fulltext search query on a given index file using the keywords query string.
* Can return Search hits as an Array of Errorline objects or json sring.
* Depends on the RestHighLevelClientprovided and index filename provided in public functions.
*/

public class FulltextSearchQuery {

    private static final Logger logger = LogManager.getLogger(FulltextSearchQuery.class);

    private String logTextField = "logText"; //subject to field name in the document used while storing.
    private String logLineNumberField = "logLineNumber"; //subject to field name in the document used while storing.

    //returns an Errorline object array as a json string. 
    public String getErrorsAsString (String indexFile, RestHighLevelClient client) {
        try {
            ArrayList<ErrorLine> errorData = getErrors(indexFile, client);
            Gson gson = new Gson();
            String json = gson.toJson(errorData);
            return json;
        } catch (IOException e) {
            String errorMsg = "Could not complete query request:";
            errorMsg = errorMsg.concat(e.toString()); 
            logger.error(errorMsg);
            return "Could not complete request.";
        }

    }

    //returns all search hits as Errorline object array
    public ArrayList<ErrorLine> getErrors(String indexFile, RestHighLevelClient client) throws IOException {
        ArrayList<ErrorLine> errorData = new ArrayList<>();
        SearchHits hits = getQueryHits(indexFile, client);
        errorData = getLogData(hits);
        return errorData;
    }

    //Make the search request and  return Search hits from match query.
    private SearchHits getQueryHits(String indexFile, RestHighLevelClient client) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexFile);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 

        // kewords class contains all the terms used combined in OR logic.
        Keywords errorKeywords = new Keywords();
        searchSourceBuilder.query(
            QueryBuilders.matchQuery(
                logTextField, errorKeywords.getQueryString())); 
        searchRequest.source(searchSourceBuilder); 

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        return hits;
    }

    // return log text from a hit.
    private String extractLogText(SearchHit hit) {
        Map<String, Object> sourceAsMap = hit.getSourceAsMap();
        return (String) sourceAsMap.get(logTextField);
    }

    // return log line number from a hit 
    private int extractLogLineNumber(SearchHit hit) { 
        Map<String, Object> sourceAsMap = hit.getSourceAsMap();
        //subject to the format logLineNumber is stored in index, string or integer.
        try{
            int lineNumber = Integer.parseInt((String) sourceAsMap.get(logLineNumberField));
            String loggerInfo = "Integer.parseInt successfully = ";
            loggerInfo.concat(Integer.toString(lineNumber)); 
            logger.info(loggerInfo);
            return lineNumber;
        } catch (NumberFormatException e) {
            String errorMsg = "Integer.parseInt error: ";
            errorMsg.concat(e.toString());
            logger.error(errorMsg);
            return -1;
        } catch (NullPointerException e2) {
            String errorMsg = "Integer.parseInt error: ";
            errorMsg.concat(e2.toString());
            logger.error(errorMsg);
            return -1;
        }
    }
    
    //Create Errorline array from search hits.
    private ArrayList<ErrorLine> getLogData(SearchHits hits) {
        ArrayList<ErrorLine> errorMsgs = new ArrayList<>();
        for (SearchHit hit : hits) {
            int lineNumber = extractLogLineNumber(hit);
            if ( lineNumber != -1) {
                errorMsgs.add(new ErrorLine( extractLogText(hit), lineNumber));
            } else {
                logger.error("Could process log line.");
            }
        }
        return errorMsgs;
    }

}