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

package com.google.sps.servlets;

import com.google.gson.Gson;
import java.util.*;
import java.lang.*;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHost;
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

@WebServlet("/fulltext_query")
public class fulltextSearch extends HttpServlet {
    private String indexFile = "trial_index";
    private RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("35.194.181.238", 9200, "http")));
    private SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SearchHits hits = getHits();
        ArrayList<String> errorMsgs = new ArrayList(getLogText(hits));
        response.setContentType("application/json");
        
        Gson gson = new Gson();
        String json = gson.toJson(errorMsgs);
        response.getWriter().println(json);
    }
    private String extractLogData(SearchHit hit){
        String sourceAsString = hit.getSourceAsString();
        Map<String, Object> sourceAsMap = hit.getSourceAsMap();
        return (String) sourceAsMap.get("log_text");
    }
    private SearchHits getHits() throws IOException{
        SearchRequest searchRequest = new SearchRequest(indexFile);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
        searchSourceBuilder.query(QueryBuilders.termQuery("log_text","error")); 
        searchRequest.source(searchSourceBuilder); 

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        return hits;
    }
    private ArrayList<String> getLogText(SearchHits hits){
        ArrayList<String> errorMsgs = new ArrayList();
        for (SearchHit hit : hits){
            errorMsgs.add(extractLogData(hit));
        }
        return errorMsgs;
    }

}