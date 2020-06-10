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

import com.google.sps.data.Keywords;
import com.google.sps.data.ErrorLine;

import com.google.gson.Gson;
import java.util.*;
import java.lang.*;
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

public class FulltextSearchQuery {
    private String logTextField = "logText"; //subject to field name in the field used while storing.
    private String logLineNumberField = "logLineNumber"; //subject to field name in the field used while storing.

    public ArrayList<ErrorLine> getErrors(String indexFile, RestHighLevelClient client) throws IOException{
        SearchHits hits = getQueryHits(indexFile,client);
        ArrayList<ErrorLine> errorData = getLogData(hits);
        return errorData;
    }
    private SearchHits getQueryHits(String indexFile, RestHighLevelClient client) throws IOException{
        SearchRequest searchRequest = new SearchRequest(indexFile);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 

        // kewords class contains all the terms used combined in OR logic.
        Keywords errorKeywords = new Keywords();
        searchSourceBuilder.query(QueryBuilders.matchQuery(logTextField,errorKeywords.getQueryString() )); 
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
            return lineNumber;
        }catch(NumberFormatException e){
            // log.error("Integer.parseInt error: " + e);
            return -1;
        }
        
    }
    
    private ArrayList<ErrorLine> getLogData(SearchHits hits){

        ArrayList<ErrorLine> errorMsgs = new ArrayList<>();
        for (SearchHit hit : hits){
            int lineNumber = extractLogLineNumber(hit);
            if( lineNumber != -1){
                errorMsgs.add(new ErrorLine( extractLogText(hit),lineNumber) );
            }
        }
        return errorMsgs;
    }

}