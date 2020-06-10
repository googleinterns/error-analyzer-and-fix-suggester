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

import com.google.sps.data.Keywords;
import com.google.sps.data.ErrorLine;
import com.google.sps.FulltextSearchQuery;

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
public class FulltextSearch extends HttpServlet {
    private String indexFile = "trial_index"; //later fetched from request

    private RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("35.194.181.238", 9200, "http")));

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        FulltextSearchQuery searchQuery = new FulltextSearchQuery();

        ArrayList<ErrorLine> errorData = searchQuery.getErrors(indexFile, client);
        Gson gson = new Gson();
        String json = gson.toJson(errorData);
        response.getWriter().println(json);
    }
}