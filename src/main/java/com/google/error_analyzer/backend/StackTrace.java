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

import com.google.error_analyzer.data.LogFields;
import com.google.error_analyzer.data.StackTraceFormat;
import com.google.error_analyzer.backend.LogDao;
import java.io.IOException;
import java.util.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

public class StackTrace {
    private final LogDao logDao = new LogDao();
    private final String logTextField = LogFields.logTextField;
    private final String logLineNumberField = LogFields.logLineNumberField; 
    private static final Logger logger = LogManager.getLogger(StackTrace.class);

    public void findStackTraceOfErrors(String fileName) throws IOException {
        SearchHits hits = logDao.findErrors(fileName);
        String stackFileName = fileName.concat("stack");
        for (SearchHit hit : hits) {
            Map <String, Object > sourceAsMap = hit.getSourceAsMap();
            Integer logLineNumber = (Integer) sourceAsMap.get(logLineNumberField);
            System.out.println("------------------------" + logLineNumber);
            String logText = (String) sourceAsMap.get(logTextField); 
            logger.info(logText);
            SearchRequest searchRequest = createSearchRequest(fileName, logLineNumber);
            SearchHits rangeHits = logDao.rangeQueryHits(searchRequest);
            for (SearchHit rangeHit : rangeHits) {
                Integer lognumber = matchesCallStackFormat(rangeHit);
                if ( lognumber != -1 ){
                    String jsonString = rangeHit.getSourceAsString();
                logDao.storeLogLine(stackFileName, jsonString, lognumber.toString());
                }else{
                    break;
                }
            }
        }
    }
    private findStack(SearchHit hits, String fileName) {

    }

    private Integer matchesCallStackFormat (SearchHit hit) {
        Map<String, Object> sourceAsMap = hit.getSourceAsMap();
        String logText = (String) sourceAsMap.get(logTextField);
        if(StackTraceFormat.matchesFormat(logText)){
            logger.info(logText);
            return (Integer) sourceAsMap.get(logLineNumberField);
        } else {
            return -1;
        }
    }



    private  RangeQueryBuilder buildRangeQuery(Integer logLineNumber) {
        RangeQueryBuilder rangeQuery = new RangeQueryBuilder(logLineNumberField)
            .gt(logLineNumber)
            .lte(logLineNumber + 100);
        return rangeQuery;
    }

    private SearchRequest createSearchRequest(String fileName, Integer logLineNumber) {
        RangeQueryBuilder rangeQuery = buildRangeQuery(logLineNumber);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .query(rangeQuery)
            .sort(logLineNumberField);
        SearchRequest searchRequest = new SearchRequest(fileName);
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }
}