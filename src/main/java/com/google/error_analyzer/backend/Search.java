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

import com.google.common.collect.ImmutableList;
import com.google.error_analyzer.data.SearchErrors;
import java.io.IOException;
import java.lang.*;
import java.util.*;
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

    private static LogDao database = new LogDao();
    private static LogDaoHelper databaseHelper= new LogDaoHelper();
    private final String field = "name";
    private static final Logger LOG = LogManager.getLogger(Search.class);
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String searchString = request.getParameter("searchString");
        String fileName = request.getParameter("fileName");
        SearchErrors SearchErrors = new SearchErrors();
        HashMap < String, String > searchResult = searchDataBase(fileName, searchString);
        SearchErrors.setSearchedErrors(searchResult);
        
    }

    // run full-text search for given string 
    private HashMap < String, String > searchDataBase(String fileName, String searchString) {
        try{
            ImmutableList < SearchHit > searchHits = database.fullTextSearch(fileName, searchString, field);
            return databaseHelper.getHighLightedText(searchHits, field);
        } catch(IOException exception) {
            LOG.error(exception);
        }
        return new HashMap();
    }

}