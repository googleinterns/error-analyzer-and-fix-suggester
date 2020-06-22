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

import com.google.error_analyzer.backend.LogDaoHelper;
import com.google.error_analyzer.data.constant.LogFields;
import com.google.error_analyzer.data.StackTraceFormat;
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
    private final static Integer batchSize = 3;
    private final LogDao logDao = new LogDao();
    private static final Logger logger = LogManager.getLogger(StackTrace.class);

    public void findStackTraceOfErrors(String fileName) throws IOException {
        SearchHits errorHits = logDao.findErrors(fileName);
        String stackFileName = LogDaoHelper.getStackIndexName(fileName);
        Integer lastHitLogLineNumber = 0;
        for (SearchHit errorHit : errorHits) {
            Map < String, Object > sourceAsMap = errorHit.getSourceAsMap();
            Integer errorLogLineNumber = (Integer) sourceAsMap.get(LogFields.LOG_LINE_NUMBER);
            logger.info("Error found at: ".concat(errorLogLineNumber.toString()));
            if (lastHitLogLineNumber >= errorLogLineNumber) {
                logger.info("Skipping error hit: ".concat(errorLogLineNumber.toString()));
                continue;
            }
            lastHitLogLineNumber = errorLogLineNumber;
            Integer endOfStack = findStack(errorLogLineNumber, fileName);
            if (endOfStack != -1) {
                String jsonString = errorHit.getSourceAsString();
                logDao.storeLogLine(stackFileName, jsonString, errorLogLineNumber.toString());
                lastHitLogLineNumber = endOfStack;
            } else {
                logger.info("No stack found for error hit: ".concat(errorLogLineNumber.toString()));
            }
        }
    }

    //returns logLineNumber of the document where the stack ends
    //returns -1 if no stack is found.
    private Integer findStack(Integer errorLogLineNumber, String fileName) throws IOException {
        Integer lastCheckedLine = errorLogLineNumber;
        Integer endOfStack = -1;
        while (true) {
            logger.info("creating a new search request");
            SearchRequest searchRequest = createSearchRequest(fileName, lastCheckedLine);
            SearchHits rangeHits = logDao.rangeQueryHits(searchRequest);
            String stackFileName = LogDaoHelper.getStackIndexName(fileName);
            if(rangeHits.getHits().length == 0) {
                return endOfStack;
            }
            for (SearchHit rangeHit : rangeHits) {
                Integer logLineNumber = matchesCallStackFormat(rangeHit);
                if ( logLineNumber != -1 ) {
                    logger.info("call stack for: ".concat(errorLogLineNumber.toString()).concat(" : line = ").concat(logLineNumber.toString()));
                    String jsonString = rangeHit.getSourceAsString();
                    logDao.storeLogLine(stackFileName, jsonString, logLineNumber.toString());
                    endOfStack = logLineNumber;
                } else {
                    return endOfStack;
                }
            }
            lastCheckedLine += batchSize;
        }
    }

    //return logLineNumber of the document if it matches call stack format
    //return -1 otherwise
    private Integer matchesCallStackFormat (SearchHit hit) {
        Map<String, Object> sourceAsMap = hit.getSourceAsMap();
        String logText = (String) sourceAsMap.get(LogFields.LOG_TEXT);
        if(StackTraceFormat.matchesFormat(logText)){
            return (Integer) sourceAsMap.get(LogFields.LOG_LINE_NUMBER);
        } else {
            return -1;
        }
    }

    //create request for 100 documents after found error
    private SearchRequest createSearchRequest(String fileName, Integer errorLogLineNumber) {
        RangeQueryBuilder rangeQuery = buildRangeQuery(errorLogLineNumber);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .query(rangeQuery)
            .sort(LogFields.LOG_LINE_NUMBER)
            .size(batchSize);
        SearchRequest searchRequest = new SearchRequest(fileName);
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }

    private  RangeQueryBuilder buildRangeQuery(Integer logLineNumber) {
        RangeQueryBuilder rangeQuery = new RangeQueryBuilder(LogFields.LOG_LINE_NUMBER)
            .gt(logLineNumber)
            .lte(logLineNumber + batchSize);
        return rangeQuery;
    }
}