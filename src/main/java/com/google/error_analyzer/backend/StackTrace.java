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
import java.lang.*;
import java.util.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
/**
* Find the stack trace following an error. The algorithm fetches documents from
* the index and see if they fit they stacktrace format.
*/
public class StackTrace {
    private Integer BATCH_SIZE = 100;
    private Integer ALLOWED_MESSAGES = 5;
    private static final Logger logger = LogManager.getLogger(StackTrace.class);
    public DaoInterface logDao = new LogDao();

    // Returns logText of the documents which form the stack trace
    // following an error
    public ImmutableList < String > findStack(Integer errorLogLineNumber,
    String fileName) throws IOException {
        SearchRequest searchRequest = createSearchRequest(fileName,
        errorLogLineNumber);
        logger.info("Finding stack for ".concat(errorLogLineNumber.toString()));
        ImmutableList < SearchHit > rangeHits = logDao.getHitsFromIndex(searchRequest);
        Builder < String > stackLogLinesBuilder = ImmutableList.< String > builder();
        Integer startOfStack = findStartOfStack(rangeHits);
        if (startOfStack == -1) {
            return stackLogLinesBuilder.build();
        } else {
            //add message brfore stack starts to stackLogLines
            logger.info("Adding ".concat(startOfStack.toString())
            .concat(" messages to stack list"));
            stackLogLinesBuilder.addAll(extractLogTextFormHits(rangeHits, 0, startOfStack));
        }
        ImmutableList < String > stackList = iterateHitsForFindingStack(rangeHits, startOfStack);
        stackLogLinesBuilder.addAll(stackList);
        if (stackList.size() < rangeHits.size() - startOfStack) {
            return stackLogLinesBuilder.build();
        }
        while (true) {
            logger.info("Exceeding batch size request");
            Integer nextLine = errorLogLineNumber + stackLogLinesBuilder.build().size();
            searchRequest = createSearchRequest(fileName, nextLine);
            rangeHits = logDao.getHitsFromIndex(searchRequest);
            stackList = iterateHitsForFindingStack(rangeHits, 0);
            stackLogLinesBuilder.addAll(stackList);
            if (stackList.size() < rangeHits.size() || rangeHits.size() == 0) {
                return stackLogLinesBuilder.build();
            }
        }
    }

    // Iterate over range query to find call stack. Return if end is found
    private ImmutableList < String > iterateHitsForFindingStack (ImmutableList < SearchHit > hits,
    Integer start) {
        Builder < String > stackLogLinesBuilder = ImmutableList.< String > builder();
        for (int i = start; i < hits.size(); i++) {
            Map < String ,Object > sourceMap = hits.get(i).getSourceAsMap();
            String logText = (String) sourceMap.get(LogFields.LOG_TEXT);
            Integer logLineNumber = (Integer) sourceMap.get(LogFields.LOG_LINE_NUMBER);
            if (StackTraceFormat.matchesFormat(logText)) {
                logger.info("Adding line:"
                .concat(logLineNumber.toString()).concat(" to stack list"));
                stackLogLinesBuilder.add(logText);
            } else {
                logger.info("End of stack trace found");
                return stackLogLinesBuilder.build();
            }
        }
        return stackLogLinesBuilder.build();
    }

    //returns index of the first searchHit that matches the stack format
    // -1 if start is not found
    private Integer findStartOfStack(ImmutableList < SearchHit > hits) {
        Integer end = Math.min(ALLOWED_MESSAGES+1, hits.size());
        for (int i = 0; i < end; i++) {
            Map < String ,Object > sourceMap = hits.get(i).getSourceAsMap();
            String logText = (String) sourceMap.get(LogFields.LOG_TEXT);
            if(StackTraceFormat.matchesFormat(logText)) {
                return i;
            }
        }
        logger.info("No stack found");
        return -1;
    }

    //create list of logText from SearchHits[start, end)
    private ImmutableList < String > extractLogTextFormHits(ImmutableList < SearchHit > hits,
    int start, int end) {
        Builder < String > logTextListBuilder = ImmutableList.< String > builder();
        for (int i = start;i < end; i++) {
            String logText = (String) hits.get(i)
                .getSourceAsMap().get(LogFields.LOG_TEXT);
            logTextListBuilder.add(logText);
        }
        return logTextListBuilder.build();
    }

    //create request for BATCH_SIZE documents after found error
    private SearchRequest createSearchRequest(String fileName, 
    Integer errorLogLineNumber) {
        RangeQueryBuilder rangeQuery = 
        buildRangeQuery(errorLogLineNumber, BATCH_SIZE);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .query(rangeQuery)
            .sort(LogFields.LOG_LINE_NUMBER)
            .size(BATCH_SIZE);
        SearchRequest searchRequest = new SearchRequest(fileName);
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }

    private  RangeQueryBuilder buildRangeQuery(Integer logLineNumber, 
    Integer batchSize) {
        RangeQueryBuilder rangeQuery = 
        new RangeQueryBuilder(LogFields.LOG_LINE_NUMBER)
            .gt(logLineNumber)
            .lte(logLineNumber + batchSize);
        return rangeQuery;
    }
}