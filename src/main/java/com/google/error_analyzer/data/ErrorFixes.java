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

import java.lang.*;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.Customsearch.Cse;
import com.google.api.services.customsearch.CustomsearchRequestInitializer;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch.Cse.List;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ErrorFixes{
    
    private static final Logger LOG = LogManager.getLogger(ErrorFixes.class);
    public static String findFixes(String searchQuery) throws IOException {
    
        try{
            String searchEngine = "cx"; //Your search engine

            //Instance Customsearch
            Customsearch customeSearch = new Customsearch.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), null) 
                        .setApplicationName("errorFixes") 
                        .setGoogleClientRequestInitializer(new CustomsearchRequestInitializer("your_api_keys")) 
                        .build();

            //Set search parameter
            Customsearch.Cse.List list = customeSearch.cse().list(searchQuery).setCx(searchEngine); 
            
            //Execute search
            Search result = list.execute();
            
            // return url in a tag 
            if (result.getItems()!=null){
                Result stackoverflowResult= result.getItems().get(0);
                String fix= stackoverflowResult.getLink();
                fix=" <a href="+fix+"> click </a> ";
                return fix;
            }else{
                return new String();
            }
        }catch(GeneralSecurityException e ){
            LOG.error("exception in custom search"+ e);
        }
        catch(IOException e ){
            LOG.error("exception in custom search"+ e);
        }
        return new String();
    }
}