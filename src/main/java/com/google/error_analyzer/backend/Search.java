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
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder.Field;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

@WebServlet("/searchString")

public class Search extends HttpServlet {
    private RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("35.194.181.238", 9200, "http")));
    private SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    private String field="name";
    private int windowSize=10;


    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String searchString=request.getParameter("searchString");
        String fileName=request.getParameter("fileName");
        SearchErrors SearchErrors= new SearchErrors();

       
        HashMap<String,String> searchResult=new HashMap();
        searchDataBase(fileName,searchString,searchResult);

        SearchErrors.setSearchedErrors(searchResult);
    }

    // run full-text search for given string 
    private void searchDataBase(String fileName,String searchString, HashMap<String,String> searchResult)throws IOException{
        int offset=0;
        SearchHit[] searchHits= null;
        while(searchHits==null || searchHits.length!=0){
            SearchRequest searchRequest=new SearchRequest(fileName); 
            SimpleQueryStringBuilder simpleQueryBuilder = QueryBuilders.simpleQueryStringQuery(searchString);
            searchSourceBuilder.query(simpleQueryBuilder).size(windowSize).from(offset);
            HighlightBuilder highlightBuilder =addHighLighter();
            searchSourceBuilder.highlighter(highlightBuilder);
            searchRequest.source(searchSourceBuilder);
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            searchHits= hits.getHits();
            for(SearchHit hit:searchHits){
              addHit(searchResult,hit);
            }
            offset+=windowSize;
        }
    }

    // store search results
    private void addHit(HashMap<String,String> searchResult,SearchHit hit){
        Map<String, HighlightField> highlightFields = hit.getHighlightFields();
        HighlightField highlight = highlightFields.get(field); 
        String fragmentString = (highlight.fragments())[0].string();
        searchResult.put(hit.getId(),fragmentString);
    }

    // highlight searched text
    private HighlightBuilder addHighLighter(){
        HighlightBuilder highlightBuilder = new HighlightBuilder().preTags("<b>").postTags("</b>");
        HighlightBuilder.Field highlightTitle =new HighlightBuilder.Field(field); 
        highlightTitle.highlighterType("unified");  
        highlightBuilder.field(highlightTitle);
        return highlightBuilder;
    }
}