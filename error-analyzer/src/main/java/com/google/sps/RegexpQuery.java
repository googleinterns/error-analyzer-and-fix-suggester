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

package com.google.sps;

import com.google.sps.data.ErrorLine;
import com.google.sps.data.RegexExpressions;
import com.google.gson.Gson;
import java.util.*;
import java.lang.*;
import java.io.IOException;

import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.index.query.RegexpQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegexpQuery{
    private static final Logger logger = LogManager.getLogger(RegexpQuery.class);

    private String logTextField = "logText"; //subject to field name in the field used while storing.
    private String logLineNumberField = "logLineNumber"; //subject to field name in the field used while storing.

    public String getResultAsString(String indexFile, RestHighLevelClient client){
        ArrayList<ErrorLine> errorData = getResults(indexFile,client);
        Gson gson = new Gson();
        String json = gson.toJson(errorData);
        return json;

    }


    public ArrayList<ErrorLine> getResults(String indexFile, RestHighLevelClient client){
        ArrayList<ErrorLine> errorData = new ArrayList<>();
        try{
            SearchHits hits = getQueryHits(indexFile,client);
            errorData = getLogData(hits);
            return errorData;
        }catch (IOException e){
            logger.error("could not complete query request." + e);
            return errorData;
        }

    }
    private SearchHits getQueryHits(String indexFile, RestHighLevelClient client) throws IOException{

        RegexExpressions regexExpressions = new RegexExpressions();
        
        RegexpQueryBuilder regexQuery = new RegexpQueryBuilder(logTextField, regexExpressions.getQueryString() ); 
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
        searchSourceBuilder.query(regexQuery); 
        
        SearchRequest searchRequest = new SearchRequest(indexFile);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        SearchHits hits = searchResponse.getHits();
        return hits;
    }

    private String extractLogText(SearchHit hit){
        String sourceAsString = hit.getSourceAsString();
        Map<String, Object> sourceAsMap = hit.getSourceAsMap();
        return (String) sourceAsMap.get(logTextField);
    }
    private int extractLogLineNumber(SearchHit hit){
        String sourceAsString = hit.getSourceAsString();
        Map<String, Object> sourceAsMap = hit.getSourceAsMap();
        //subject to the format logLineNumber is stored in index, string or integer.
        try{
            int lineNumber = Integer.parseInt((String) sourceAsMap.get(logLineNumberField));
            logger.info("Integer.parseInt successfully = " + lineNumber);
            return lineNumber;
        }catch(NumberFormatException e){
            logger.error("Integer.parseInt error: " + e);
            return -1;
        }catch(NullPointerException e2){
            logger.error("null error: " + e2);
            return -1;
        }
        
    }
    
    private ArrayList<ErrorLine> getLogData(SearchHits hits){
        ArrayList<ErrorLine> errorMsgs = new ArrayList<>();
        for (SearchHit hit : hits){
            int lineNumber = extractLogLineNumber(hit);
            if( lineNumber != -1){
                errorMsgs.add(new ErrorLine( extractLogText(hit),lineNumber) );
            }else{
                logger.error("Could not process log line.");
            }
        }
        return errorMsgs;
    }
}