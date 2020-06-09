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
package com.google.sps.servlets;

import com.google.gson.Gson;
import java.util.*;
import java.lang.*;
import com.google.sps.data.SearchErrors;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

@WebServlet("/pagination")
public class Pagination extends HttpServlet {

    private final int recordsPerPage = 3;
    private final int noOfPages = 5;
    private final int extraPageInFrontAndBack = 2;
    private final String field = "name";
    private int noOfRecordsOnLastPage = recordsPerPage;
    private int lastPage = Integer.MAX_VALUE;
    private ArrayList < String > data = new ArrayList();
    private RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("35.194.181.238", 9200, "http")));
    private SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

    // return object 
    private class logOrErrorResponse {
        boolean lastPage;
        ArrayList < String > logOrError;
        public logOrErrorResponse(ArrayList < String > logOrError, boolean lastPage) {
            this.logOrError = logOrError;
            this.lastPage = lastPage;
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int page = Integer.parseInt(request.getParameter("requestedPage"));
        String fileName = request.getParameter("fileName");
        String fileType = request.getParameter("fileType");
        String next = request.getParameter("next");

        SearchErrors searchErrors = new SearchErrors();
        HashMap < String, String > search = searchErrors.getSearchedErrors();


        // if file field is empty the user haven't choosen anyfile so return empty object
        if (fileName.length() == 0) {
            returnEmptyObject(response);
            return;
        }
        boolean isLastPage = page == lastPage ? true : false;
        //  if user is asking for 1st page return that page and make a window of fully finished and ready to use data for that file
        // if user is not on 1st page but someother page just maintain the already created window of pages
        if (page == 1) {
            // the user won't be knowing the name of error file generated corresponding to his log file so we need to change fileName depending on type of file
            if (fileType.equals("error"))
                fileName = "error" + fileName;
            // create window
            addToData(0, (recordsPerPage * noOfPages) - 1, data, 0, fileName, page, search);
            returnResponse(data, 0, recordsPerPage - 1, isLastPage, response);
        } else {
            int start = recordsPerPage * ((page - 1) % noOfPages);
            int stop = start + recordsPerPage - 1;
            if (page == lastPage)
                stop = start + noOfRecordsOnLastPage - 1;
            returnResponse(data, start, stop, isLastPage, response);
            // maintain window
            maintainWindow(recordsPerPage, page, next, extraPageInFrontAndBack, fileName, search);
        }
    }

    // picks content from maintained window on the basis of required page and convert it into json format
    private void returnResponse(ArrayList < String > data, int startIdx, int stopIdx, boolean isLastPage, HttpServletResponse response) throws IOException {
        ArrayList < String > display = new ArrayList();
        for (int i = startIdx; i <= stopIdx && i < data.size(); i++) {
            display.add(data.get(i));
        }
        String json = convertToJson(new logOrErrorResponse(display, isLastPage));
        response.setContentType("application/json");
        response.getWriter().println(json);
    }

    // put/change content of data for maintaining continuous window of pages(here window of 5 pages)
    private void addToData(int start, int size, ArrayList < String > data, int startIdx, String fileName, int page, HashMap < String, String > search) throws IOException {
        SearchRequest searchRequest = new SearchRequest(fileName);
        searchSourceBuilder.query(QueryBuilders.matchAllQuery()).size(size).from(start);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        int i = startIdx;
        for (SearchHit hit: searchHits) {
            String id = hit.getId();
            String resultString = String.valueOf(hit.getSourceAsMap().get(field));
            if (search.containsKey(id)) {
                resultString = search.get(id);
            }
            if (i >= data.size())
                data.add(resultString);
            else
                data.set(i, resultString);
            i++;
        }

        // check if the updated page is the last page  
        if (searchHits.length == 0 || searchHits.length < recordsPerPage) {
            if (page == 1)
                lastPage = 1;
            else if (page != 1 && searchHits.length != 0) {
                lastPage = page + extraPageInFrontAndBack;
                noOfRecordsOnLastPage = searchHits.length;
            } else if (page != 1 && searchHits.length == 0)
                lastPage = page + extraPageInFrontAndBack - 1;
        }
    }

    // return json for java object
    private String convertToJson(logOrErrorResponse display) {
        Gson gson = new Gson();
        String json = gson.toJson(display);
        return json;
    }

    // return empty object 
    private void returnEmptyObject(HttpServletResponse response) throws IOException {
        String json = convertToJson(new logOrErrorResponse(new ArrayList(), true));
        response.setContentType("application/json");
        response.getWriter().println(json);
    }

    // maintains window of size totalpages
    private void maintainWindow(int recordsPerPage, int page, String next, int extraPageInFrontAndBack, String fileName, HashMap < String, String > search) throws IOException {
        if (next.equals("true") && page + extraPageInFrontAndBack <= lastPage) {
            int Start = recordsPerPage * (page + extraPageInFrontAndBack - 1);
            int startIdx = recordsPerPage * ((page + extraPageInFrontAndBack - 1) % noOfPages);
            addToData(Start, recordsPerPage, data, startIdx, fileName, page, search);
        } else if (next.equals("false") && page - extraPageInFrontAndBack > 0) {
            int Start = recordsPerPage * (page - extraPageInFrontAndBack - 1);
            int startIdx = recordsPerPage * ((page - extraPageInFrontAndBack - 1) % noOfPages);
            addToData(Start, recordsPerPage, data, startIdx, fileName, page, search);
        }
    }

}