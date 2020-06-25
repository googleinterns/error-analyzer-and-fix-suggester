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
import com.google.common.collect.ImmutableList.Builder;
import com.google.error_analyzer.backend.LogDaoHelper;
import com.google.error_analyzer.data.constant.LogFields;
import com.google.error_analyzer.data.constant.StackTraceFormat;
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
    private Integer BATCH_SIZE = 10;
    private Integer ALLOWED_MESSAGES = 5;
    private static final Logger logger = LogManager.getLogger(StackTrace.class);
    public DaoInterface logDao = new LogDao();

    //returns logText of the document where the 
    public ImmutableList < String > findStack(Integer errorLogLineNumber, String fileName)
    throws IOException {
        Builder < String > stackLogLines = ImmutableList.< String > builder();
        Integer countOfMsgsBeforeStack = 0;
        Integer lastCheckedLine = errorLogLineNumber;
        ArrayList < String > msgsBeforeStack = new ArrayList < String >();
        logger.info("Creating a new search request for error at ".concat(errorLogLineNumber.toString()));
        SearchRequest searchRequest = createSearchRequest(fileName, errorLogLineNumber);
        SearchHit[] rangeHits = logDao.getHitsFromIndex(searchRequest);
        Integer storedInStack = 0; //stackLogLines.size()

        for (SearchHit rangeHit : rangeHits) {
            Map < String ,Object > sourceMap = rangeHit.getSourceAsMap();
            String logText = (String) sourceMap.get(LogFields.LOG_TEXT);
            Integer logLineNumber = (Integer) sourceMap.get(LogFields.LOG_LINE_NUMBER);
            if(logLineNumber != lastCheckedLine + 1){
                return (storedInStack == 0 ? noStackFound() : stackLogLines.build());
            }
            lastCheckedLine = logLineNumber;
            if ( StackTraceFormat.matchesFormat(logText) ) {
                if (storedInStack == 0) {
                    storedInStack += msgsBeforeStack.size();
                    logger.info("Adding messages before error to stack for "
                    .concat(errorLogLineNumber.toString()));
                    stackLogLines.addAll(msgsBeforeStack);
                }
                logger.info("Adding to stack list log line number "
                .concat(logLineNumber.toString()));
                storedInStack++;
                stackLogLines.add(logText);
            } else {
                if (storedInStack == 0 && countOfMsgsBeforeStack < ALLOWED_MESSAGES) {
                    logger.info("Adding ".concat(logLineNumber.toString())
                    .concat(" to error msgs"));
                    msgsBeforeStack.add(logText);
                    countOfMsgsBeforeStack++;
                } else {
                    return (storedInStack == 0 ? noStackFound() : stackLogLines.build());
                }
            }
        }
        
        if(storedInStack == 0) {
            return noStackFound();
        }
        if(storedInStack == rangeHits.length){
            stackLogLines.addAll(findStackExceedingBATCH_SIZE(lastCheckedLine, fileName));
        }
        return stackLogLines.build();
    }

    public ImmutableList < String > noStackFound() {
        return ImmutableList.<String>builder() 
            .add("No stack found for this error").build(); 
    }

    private ArrayList < String > findStackExceedingBATCH_SIZE(Integer errorLogLineNumber,
    String fileName) throws IOException {
        ArrayList < String > stackLogLines = new ArrayList < String >();
        Integer lastCheckedLine = errorLogLineNumber;
        while (true) {
            logger.info("Exceeding batch size request. Creating a new search request");
            SearchRequest searchRequest = createSearchRequest(fileName, lastCheckedLine);
            SearchHit[] rangeHits = logDao.getHitsFromIndex(searchRequest);
            if (rangeHits.length == 0) {
                return stackLogLines;
            }
            for (SearchHit rangeHit : rangeHits) {
                Map < String ,Object > sourceMap = rangeHit.getSourceAsMap();
                String logText = (String) sourceMap.get(LogFields.LOG_TEXT);
                Integer logLineNumber = (Integer) sourceMap.get(LogFields.LOG_LINE_NUMBER);
                if(logLineNumber != lastCheckedLine + 1){
                    return stackLogLines;
                }
                lastCheckedLine = logLineNumber;
                if ( StackTraceFormat.matchesFormat(logText) ) {
                    logger.info("Adding to stack list log line number "
                    .concat(logLineNumber.toString()));
                    stackLogLines.add(logText);
                } else {
                    return stackLogLines;
                }
            }
        }
    }

    //create request for #BATCH_SIZE documents after found error
    private SearchRequest createSearchRequest(String fileName, 
    Integer errorLogLineNumber) {
        RangeQueryBuilder rangeQuery = buildRangeQuery(errorLogLineNumber, BATCH_SIZE);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .query(rangeQuery)
            .sort(LogFields.LOG_LINE_NUMBER)
            .size(BATCH_SIZE);
        SearchRequest searchRequest = new SearchRequest(fileName);
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }

    private  RangeQueryBuilder buildRangeQuery(Integer logLineNumber, 
    Integer BATCH_SIZE) {
        RangeQueryBuilder rangeQuery = new RangeQueryBuilder(LogFields.LOG_LINE_NUMBER)
            .gt(logLineNumber)
            .lte(logLineNumber + BATCH_SIZE);
        return rangeQuery;
    }
}