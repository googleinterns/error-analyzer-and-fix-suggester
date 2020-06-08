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
import com.google.sps.data.ErrorsLogs;
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

@
WebServlet("/pagination")
public class Pagination extends HttpServlet {

    private final int recordsPerPage = 3;
    private final int noOfPages = 5;
    private final int extraPageInFrontAndBack = 2;
    private int totalPages = 1;
    private ArrayList < Map < String, Object >> data = new ArrayList();
    private RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("35.194.181.238", 9200, "http")));
    private SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

    private class ReturnObj {
        ArrayList < Map < String, Object > > logsOrErrors;
        int totalPages;
        public ReturnObj(ArrayList < Map < String, Object > > logsOrErrors, int totalPages) {
            this.logsOrErrors = logsOrErrors;
            this.totalPages = totalPages;
        }
    }@
    Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int page = Integer.parseInt(request.getParameter("requestedPage"));
        String fileName = request.getParameter("fileName");
        String fileType = request.getParameter("fileType");
        String next = request.getParameter("next");

        ErrorsLogs errLogObj = new ErrorsLogs();
        HashSet < String > search = errLogObj.getSearchedErrors();


        // if file field is empty the user haven't choosen anyfile so return empty object
        if (fileName.length() == 0) {
            ReturnObj display = new ReturnObj(data, 1);
            String json = convertToJson(display);
            response.setContentType("application/json");
            response.getWriter().println(json);
            return;
        }
        int sizeOfFile = 1;
        //  if user is asking for 1st page return that page and make a window of fully finished and ready to use data for that file
        // if user is not on 1st page but someother page just maintain the already created window of pages
        if (page == 1) {

            // the user won't be knowing the name of error file generated corresponding to his log file so we need to change fileName depending on type of file
            if (fileType.equals("error"))
                fileName = "error" + fileName;
            sizeOfFile = totalNoOfPages(fileName);
            totalPages = (int) Math.ceil((double) sizeOfFile / (double) recordsPerPage);

            // create window
            addToData(0, (recordsPerPage * noOfPages) - 1, data, 0, fileName);
            String json = returnResponse(data, 0, recordsPerPage - 1, search, totalPages);
            response.setContentType("application/json");
            response.getWriter().println(json);

        } else {
            int start = recordsPerPage * ((page - 1) % noOfPages);
            int stop = start + recordsPerPage - 1;
            // if the page user is asking for is the last page then it can contain less data than the standard amount of data that we were showing
            if (page == totalPages && sizeOfFile % recordsPerPage != 0)
                stop = start + (int)(sizeOfFile % recordsPerPage) - 1;
            String json = returnResponse(data, start, stop, search, totalPages);
            response.setContentType("application/json");
            response.getWriter().println(json);

            // maintain window
            if (next.equals("true") && page + extraPageInFrontAndBack <= totalPages) {
                int Start = recordsPerPage * (page + extraPageInFrontAndBack - 1);
                int startIdx = recordsPerPage * ((page + extraPageInFrontAndBack - 1) % noOfPages);
                addToData(Start, recordsPerPage, data, startIdx, fileName);
            } else if (next.equals("false") && page - extraPageInFrontAndBack > 0) {
                int Start = recordsPerPage * (page - extraPageInFrontAndBack - 1);
                int startIdx = recordsPerPage * ((page - extraPageInFrontAndBack - 1) % noOfPages);
                addToData(Start, recordsPerPage, data, startIdx, fileName);
            }
        }
    }
    // picks content from maintained window on the basis of required page and convert it into json format
    private String returnResponse(ArrayList < Map < String, Object >> data, int startIdx, int stopIdx, HashSet < String > search, int totalPages) {
        ArrayList < Map < String, Object > > display = new ArrayList();
        for (int i = startIdx; i <= stopIdx && i < data.size(); i++) {
            Map < String, Object > element = data.get(i);
            if (search.contains(element)) {
                display.add(element);
            } else {
                display.add(element);
            }
        }
        ReturnObj displayObj = new ReturnObj(display, totalPages);
        return convertToJson(displayObj);
    }
    // put/change content of data for maintaining continuous window of pages(here window of 5 pages)
    private void addToData(int start, int size, ArrayList < Map < String, Object >> data, int startIdx, String fileName) {
        SearchRequest searchRequest = new SearchRequest(fileName);
        searchSourceBuilder.query(QueryBuilders.matchAllQuery()).size(size).from(start);
        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();

            int i = startIdx;
            for (SearchHit hit: searchHits) {
                if (i >= data.size())
                    data.add(hit.getSourceAsMap());
                else
                    data.set(i, hit.getSourceAsMap());
                i++;
            }
        } catch (Exception e) {
            return;
        }
    }
    // return json for java Map<String,Object>
    private String convertToJson(ReturnObj display) {
        Gson gson = new Gson();
        String json = gson.toJson(display);
        return json;
    }
    // returns total pages for a file 
    private int totalNoOfPages(String fileName) {
        try {
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            SearchRequest searchRequest = new SearchRequest(fileName);
            searchRequest.source(searchSourceBuilder);
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            return response.getHits().getHits().length;
        } catch (Exception e) {
            return 1;
        }
    }

}