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

/**
* Servlet used to see the results of fullText search query.
* Connects to the RestHighLevelClient and stores the name of index file to be queried. 
*/
package com.google.error_analyzer.servlets;

import com.google.error_analyzer.data.Keywords;
import com.google.error_analyzer.data.ErrorLine;
import com.google.error_analyzer.backend.FulltextSearchQuery;

import com.google.gson.Gson;
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

@WebServlet("/fulltext_query")
public class FulltextSearch extends HttpServlet {

    private String indexFile = "trial_index"; //later fetched from request
    private static final Logger logger = LogManager.getLogger(FulltextSearch.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        try{
            RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("35.194.181.238", 9200, "http")));
            FulltextSearchQuery searchQuery = new FulltextSearchQuery();

            String errorData = searchQuery.getErrorsAsString(indexFile, client);
            response.getWriter().println(errorData);
        }catch(Exception e){
            logger.error("Could not connect to server." + e);
            response.getWriter().println("Could not connect to database." );
        }
        
    }
}