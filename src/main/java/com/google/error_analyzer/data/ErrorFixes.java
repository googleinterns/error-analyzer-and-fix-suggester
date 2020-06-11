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
package com.google.sps.data;

import com.google.gson.Gson;
import java.util.*;
import java.lang.*;
import com.google.sps.data.SearchErrors;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.security.GeneralSecurityException;

import java.util.*;
import java.net.*;
import java.io.*;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.Customsearch.Cse;
import com.google.api.services.customsearch.CustomsearchRequestInitializer;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch.Cse.List;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;

@WebServlet("/fix")
public class ErrorFixes extends HttpServlet  {
    // public static String findFixes(String searchQuery) throws GeneralSecurityException, IOException {
     @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try{
    String searchQuery=request.getParameter("fix");
    String searchEngine = "cx"; //Your search engine

    //Instance Customsearch
    Customsearch customeSearch = new Customsearch.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), null) 
                   .setApplicationName("errorFixes") 
                   .setGoogleClientRequestInitializer(new CustomsearchRequestInitializer("your_api_key")) 
                   .build();

    //Set search parameter
    Customsearch.Cse.List list = customeSearch.cse().list(searchQuery).setCx(searchEngine); 
    response.setContentType("text/html");
    //  response.getWriter().println("hi");
    //Execute search
    Search result = list.execute();
    if (result.getItems()!=null){
        for (Result ri : result.getItems()) {
            //Get title, link, body etc. from search
            response.getWriter().println(ri.getTitle() + ", " + ri.getLink());
        }
    }else{
        return new String();
    }
    }catch(GeneralSecurityException e){
        return new String();
    }

}
}