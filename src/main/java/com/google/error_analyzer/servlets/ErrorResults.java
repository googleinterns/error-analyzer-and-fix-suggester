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

package com.google.error_analyzer.servlets;

import com.google.error_analyzer.backend.BooleanQuery;
import com.google.gson.Gson;
import java.lang.*;
import java.util.*;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;

@WebServlet("/errorResults")
public class ErrorResults extends HttpServlet {
    private String indexFile = "trial_index"; //later fetched from request
    private static final Logger logger = LogManager.getLogger(ErrorResults.class);
    private String logTextField = "logText"; 
    RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("35.194.181.238", 9200, "http")));
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        try {
            BooleanQuery searchQuery = new BooleanQuery();
            SearchRequest searchRequest = searchQuery.createSearchRequest(indexFile);
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] hits = searchResponse.getHits().getHits();
            ArrayList<String> errorData = searchQuery.sortErrorDocuments(hits);
            response.getWriter().println(errorData);
            String infoMsg = "Completed Boolean query successfully on ";
            infoMsg = infoMsg.concat(indexFile);
            logger.info(infoMsg);
        } catch(IOException e) {
            String loggerError = "Could not connect to database.";
            loggerError = loggerError.concat(e.toString());
            logger.error(loggerError);
            response.getWriter().println(loggerError);
        }
    }
}