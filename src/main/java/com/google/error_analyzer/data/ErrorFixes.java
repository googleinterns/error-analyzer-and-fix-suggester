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
package com.google.error_analyzer.data;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.CustomsearchRequestInitializer;
import com.google.api.services.customsearch.Customsearch.Cse;
import com.google.api.services.customsearch.Customsearch.Cse.List;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;
import java.io.IOException;
import java.lang.*;
import java.security.GeneralSecurityException;
import java.util.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ErrorFixes{
    
    private static final Logger logger = LogManager.getLogger(ErrorFixes.class);
    private Customsearch.Cse.List list;
    public String findFixes(String searchQuery) {
    
        try{
            String searchEngine = "search_engine_id"; //Your search engine

            //Instance Customsearch
            Customsearch customSearch = 
                new Customsearch.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(), null).
                setApplicationName("errorFixes").setGoogleClientRequestInitializer(
                new CustomsearchRequestInitializer("your_api_key")).build();

            //Set search parameter
             list = customSearch.cse()
                                .list(searchQuery)
                                .setCx(searchEngine); 
            
            //Execute search
            Search result = list.execute();
            
            // return url in a tag 
            if (result.getItems()!=null){
                Result stackoverflowResult= result.getItems().get(0);
                String fix= stackoverflowResult.getLink();
                return resultString(fix);
            } else {
                return new String();
            }
        } catch(GeneralSecurityException exception) {
            logger.error(exception);
        }
        catch(IOException exception) {
            logger.error(exception);
        }
        return new String();
    }

    private String resultString(String fix) {
        return String.format(" <a href = \"%s\" > FIX </a>",fix);
    } 
}