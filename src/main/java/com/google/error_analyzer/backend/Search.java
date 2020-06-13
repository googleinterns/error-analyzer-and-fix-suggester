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
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder.Field;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

@WebServlet("/searchString")

public class Search extends HttpServlet {
  
    private final String field="name";
   
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
        Database database = new Database();
        ArrayList<SearchHit> searchHits = database.fullTextSearch(fileName, searchString, field);
        for(SearchHit hit : searchHits){
            addHit(searchResult,hit);
        }
    }

    // store search results
    private void addHit(HashMap<String,String> searchResult,SearchHit hit){
        Map<String, HighlightField> highlightFields = hit.getHighlightFields();
        HighlightField highlight = highlightFields.get(field); 
        String fragmentString = (highlight.fragments())[0].string();
        searchResult.put(hit.getId(),fragmentString);
    }

}