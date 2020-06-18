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

import com.google.error_analyzer.data.Keywords;
import com.google.error_analyzer.data.RegexStrings;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RegexpQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;


public class BooleanQuery {
    private static final Logger logger = LogManager.getLogger(BooleanQuery.class);
    private final String logTextField = "logText";
    private final String logLineNumberField = "logLineumber";
    //search db using regex and keywords and store back in db searchHits sorted by logLineNumber
    public SearchRequest createSearchRequest(String fileName) {
        SearchRequest searchRequest = new SearchRequest(fileName);
        Keywords errorKeywords = new Keywords();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        RegexStrings regexExpressions = new RegexStrings();
        String regexQueryString = regexExpressions.getQueryString();
        RegexpQueryBuilder regexQuery = new RegexpQueryBuilder(logTextField,regexQueryString);
        String keywordsQueryString = errorKeywords.getQueryString();
        QueryBuilder fulltextQuery = QueryBuilders.matchQuery(logTextField, keywordsQueryString);
        QueryBuilder errorQuery = new BoolQueryBuilder()
            .minimumShouldMatch(1)
            .should(regexQuery)
            .should(fulltextQuery);
        searchSourceBuilder.size(1000);
        searchSourceBuilder.sort(logLineNumberField);
        searchSourceBuilder.query(errorQuery); 
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }
    
    //Sort the searchHits acc to ids and return document json strings
    public ArrayList<String> sortErrorDocuments(SearchHit[] hits) {
        ArrayList<Integer> searchHitIds = new ArrayList<>();
        HashMap<Integer, String> hitsHashMap = new HashMap();
        for (SearchHit hit : hits) {
            try {
                Integer id = Integer.parseInt(hit.getId());
                logger.info("-------------------------------"+id);
                searchHitIds.add(id);
                String jsonDocument = hit.getSourceAsString();
                hitsHashMap.put(id, jsonDocument);
            } catch(NumberFormatException e) {
                String errorMsg = "Could not parse: ";
                logger.error(errorMsg.concat(e.toString()));
            }
        }
        Collections.sort(searchHitIds);
        ArrayList<String> sortedSourceStrings = new ArrayList();
        for (Integer id : searchHitIds) {
            String errorJsonString = hitsHashMap.get(id);
            sortedSourceStrings.add(errorJsonString);
        }
        return sortedSourceStrings;
    }
}