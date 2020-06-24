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
    private final static Integer batchSize = 100;
    private final static Integer allowedMsgs = 5;
    private final static String noStackFoundMsg = "No stack found for this error";
    private static final Logger logger = LogManager.getLogger(StackTrace.class);
    public DaoInterface logDao = new LogDao();

    //returns logLineNumber of the document where the stack ends
    //returns -1 if no stack is found.
    public ArrayList < String > findStack(Integer errorLogLineNumber, String fileName)
    throws IOException {
        ArrayList < String > stackLogLines = new ArrayList < String >();
        Integer countOfMsgsBeforeStack = 0;
        Integer endOfStack = -1;
        ArrayList < String > msgsBeforeStack = new ArrayList < String >();
        logger.info("Creating a new search request for error at ".concat(errorLogLineNumber.toString()));
        SearchRequest searchRequest = createSearchRequest(fileName, errorLogLineNumber);
        SearchHit[] rangeHits = logDao.getHitsFromIndex(searchRequest);
        if (rangeHits.length == 0) {
            stackLogLines.add(noStackFoundMsg);
            return stackLogLines;
        }
        for (SearchHit rangeHit : rangeHits) {
            Map < String ,Object > sourceMap = rangeHit.getSourceAsMap();
            String logText = (String) sourceMap.get(LogFields.LOG_TEXT);
            Integer logLineNumber = (Integer) sourceMap.get(LogFields.LOG_LINE_NUMBER);
            if ( StackTraceFormat.matchesFormat(logText) ) {
                if (endOfStack == -1) {
                    logger.info("Storing messages before error for ".concat(errorLogLineNumber.toString()));
                    stackLogLines.addAll(msgsBeforeStack);
                }
                logger.info("Adding to stack list log line number ".concat(logLineNumber.toString()));
                stackLogLines.add(logText);
                endOfStack = logLineNumber;
            } else {
                if (endOfStack == -1 && countOfMsgsBeforeStack < allowedMsgs) {
                    logger.info("Adding ".concat(logLineNumber.toString())
                    .concat(" to error msgs"));
                    msgsBeforeStack.add(logText);
                    countOfMsgsBeforeStack++;
                } else {
                    if(stackLogLines.size() == 0) {
                        stackLogLines.add("No stack found for this error");
                    }
                    return stackLogLines;
                }
            }
        }
        stackLogLines.addAll(findStackExceedingBatchSize(endOfStack, fileName));
        return stackLogLines;
    }

    private ArrayList < String > findStackExceedingBatchSize(Integer errorLogLineNumber,
    String fileName) throws IOException {
        ArrayList < String > stackLogLines = new ArrayList < String >();
        Integer lastCheckedLine = errorLogLineNumber;
        logger.info("Exceeding batch size request");
        while (true) {
            logger.info("Creating a new search request");
            SearchRequest searchRequest = createSearchRequest(fileName, lastCheckedLine);
            SearchHit[] rangeHits = logDao.getHitsFromIndex(searchRequest);
            if (rangeHits.length == 0) {
                return stackLogLines;
            }
            for (SearchHit rangeHit : rangeHits) {
                Map < String ,Object > sourceMap = rangeHit.getSourceAsMap();
                String logText = (String) sourceMap.get(LogFields.LOG_TEXT);
                Integer logLineNumber = (Integer) sourceMap.get(LogFields.LOG_LINE_NUMBER);
                if ( StackTraceFormat.matchesFormat(logText) ) {
                    logger.info("Call stack for: ".concat(errorLogLineNumber.toString())
                    .concat(" : line = ").concat(logLineNumber.toString()));
                    stackLogLines.add(logText);
                    lastCheckedLine = logLineNumber;
                } else {
                    return stackLogLines;
                }
            }
        }
    }

    //create request for #batchSize documents after found error
    private SearchRequest createSearchRequest(String fileName, 
    Integer errorLogLineNumber) {
        RangeQueryBuilder rangeQuery = buildRangeQuery(errorLogLineNumber, batchSize);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .query(rangeQuery)
            .sort(LogFields.LOG_LINE_NUMBER)
            .size(batchSize);
        SearchRequest searchRequest = new SearchRequest(fileName);
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }

    private  RangeQueryBuilder buildRangeQuery(Integer logLineNumber, 
    Integer batchSize) {
        RangeQueryBuilder rangeQuery = new RangeQueryBuilder(LogFields.LOG_LINE_NUMBER)
            .gt(logLineNumber)
            .lte(logLineNumber + batchSize);
        return rangeQuery;
    }
}