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

import com.google.gson.Gson;
import java.util.*;
import java.lang.*;
import com.google.error_analyzer.data.SearchErrors;
import com.google.error_analyzer.data.ErrorFixes;
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

    public int recordsPerPage = 3;
    // keep it a odd no so that there are equal pages in front and back 
    public int noOfPages = 5;
    public int extraPageInFrontAndBack = noOfPages / 2;
    public String field = "name";
    public String ERROR="errors";
    public int noOfRecordsOnLastPage = recordsPerPage;
    public int lastPage = Integer.MAX_VALUE;
    public ArrayList < String > data = new ArrayList();
    private static final Logger LOG = LogManager.getLogger(Pagination.class);
    public RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("35.194.181.238", 9200, "http")));
    public SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

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
        if (page <= 0)
            page = 1;
        // if file field is empty the user haven't choosen anyfile so return empty object
        if (fileName.length() == 0) {
            returnEmptyObject(response);
            return;
        }

        // if fileName is not empty fetch the data from database and return response
        fetchAndReturnResponse(page, fileName, fileType, next, search, response);

    }

    //  if user is asking for 1st page return that page and make a window of fully finished and ready to use data for that file
    // if user is not on 1st page but someother page just maintain the already created window of pages

    public void fetchAndReturnResponse(int page, String fileName, String fileType, String next, HashMap < String, String > search, HttpServletResponse response) throws IOException {
        if (page == 1) {
            // the user won't be knowing the name of error file generated corresponding to his log file so we need to change fileName depending on type of file
            if (fileType.equals(ERROR))
                fileName = fileName + "error";
            lastPage=Integer.MAX_VALUE;
            // create window
            fetchData(0, (recordsPerPage * noOfPages) - 1, 0, fileName,fileType, page, search);
            returnResponse(0, recordsPerPage - 1, isLastPage(page), response);
        } else {
            int start = recordsPerPage * ((page - 1) % noOfPages);
            int stop = start + recordsPerPage - 1;
            if (isLastPage(page))
                stop = start + noOfRecordsOnLastPage - 1;
            returnResponse(start, stop, isLastPage(page), response);
            // maintain window
            maintainWindow(page, next, fileName,fileType, search);
        }
    }
    

    // returns true if the asked page is last page
    public boolean isLastPage(int page){
        return page == lastPage ? true : false;
    }

    // picks content from maintained window on the basis of required page and convert it into json format
    private void returnResponse(int startIdx, int stopIdx, boolean isLastPage, HttpServletResponse response) throws IOException {
        ArrayList < String > display = new ArrayList();
        for (int i = startIdx; i <= stopIdx && i < data.size(); i++) {
            display.add(data.get(i));
        }
        
        String json = convertToJson(new logOrErrorResponse(display, isLastPage));
        response.setContentType("application/json");
        response.getWriter().println(json);
    }

    // put/change content of data for maintaining continuous window of pages(here window of 5 pages)
    private void fetchData(int start, int size, int startIdx, String fileName,String fileType, int page, HashMap < String, String > search) throws IOException {

        SearchRequest searchRequest = new SearchRequest(fileName);
        searchSourceBuilder.query(QueryBuilders.matchAllQuery()).size(size).from(start);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
       
        addFetchResultToData(startIdx,fileType,searchHits,search);
        updateLastPage(searchHits.length, page);
    }

    // add fetched results to data and apply search results and error-fixes if applicable
    private void addFetchResultToData(int startIdx,String fileType,SearchHit[] searchHits,HashMap < String, String > search)throws IOException{
        int i = startIdx;
        ErrorFixes errorFix=new ErrorFixes();
        for (SearchHit hit: searchHits) {
            String id = hit.getId();
            String resultString = String.valueOf(hit.getSourceAsMap().get(field));
            String fix=new String();

            if(fileType.equals(ERROR))
                fix = errorFix.findFixes(resultString);
           
            if (search.containsKey(id)) {
                resultString = search.get(id);
            }
            // append fix to the error string 
            resultString+=fix;
            if (i >= data.size())
                data.add(resultString);
            else
                data.set(i, resultString);
            i++;
        }
    }

    // check if the updated page is the last page 
    public void updateLastPage(int searchHitLength, int page) {
        int fetchedPage=page + extraPageInFrontAndBack;
        if (page == 1 && searchHitLength< fetchedPage*recordsPerPage) {
            lastPage = (int)Math.ceil((double)searchHitLength/(double)recordsPerPage);
            noOfRecordsOnLastPage = searchHitLength % recordsPerPage;
        }else if (page !=1 && searchHitLength == 0) {
            lastPage = fetchedPage - 1;
            noOfRecordsOnLastPage = recordsPerPage;
        }else if (page !=1 && searchHitLength < recordsPerPage) {
            lastPage = fetchedPage;
            noOfRecordsOnLastPage = searchHitLength;
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
    public int[] maintainWindow(int page, String next, String fileName,String fileType, HashMap < String, String > search) throws IOException {
        int Start = 0;
        int startIdx = 0;
        
        if (next.equals("true") && page + extraPageInFrontAndBack <= lastPage) {
            Start = recordsPerPage * (page + extraPageInFrontAndBack - 1);
            startIdx = recordsPerPage * ((page + extraPageInFrontAndBack - 1) % noOfPages);
            fetchData(Start, recordsPerPage, startIdx, fileName,fileType, page, search);
        } else if (next.equals("false") && page - extraPageInFrontAndBack > 0) {
            Start = recordsPerPage * (page - extraPageInFrontAndBack - 1);
            startIdx = recordsPerPage * ((page - extraPageInFrontAndBack - 1) % noOfPages);
            fetchData(Start, recordsPerPage, startIdx, fileName,fileType, page, search);
        }
         LOG.info("indexes and lastpage"+Start+" "+startIdx+" "+(page + extraPageInFrontAndBack));
        return new int[] {
            Start, startIdx
        };
    }

}