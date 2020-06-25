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

package com.google.error_analyzer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.error_analyzer.data.constant.LogFields;
import com.google.error_analyzer.backend.StackTrace;
import com.google.error_analyzer.backend.MockLogDao;
import java.io.IOException;
import java.util.*;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.search.SearchHit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;


// @RunWith(MockitoJUnitRunner.class)
public final class StackTraceTest {
    private final String FILE_NAME = "file"; 
    private StackTrace stackTrace;
    private final String MSG_BEFORE_STACK =  "Running com.google.error_analyzer at maven";
    private final String STACK_LOG_LINE = "cvr123 At com.stack.stacktrace.StackTraceExample.methodB";
    private final static Integer ALLOWED_MESSAGES = 1; 
    private final static Integer BATCH_SIZE = 1;
    private final Integer ERROR_LINE = 1;

    @Before
    public void setUp() {
        stackTrace = new StackTrace();
        stackTrace.logDao = Mockito.mock(MockLogDao.class);
        ReflectionTestUtils.setField(stackTrace, "BATCH_SIZE", BATCH_SIZE);
        ReflectionTestUtils.setField(stackTrace, "ALLOWED_MESSAGES", ALLOWED_MESSAGES);
    }

    @Test
    public void noHitsFromRangeQuery() throws IOException {
        // No hit is returned from searchRequest i.e. End of file
        SearchHit[] searchHits = new SearchHit[0]; 
        when(stackTrace.logDao.getHitsFromIndex(any(SearchRequest.class))).thenReturn(searchHits);
        ImmutableList < String > actual = stackTrace.findStack(ERROR_LINE, FILE_NAME);
        ImmutableList < String > expected = stackTrace.noStackFound();
        Assert.assertEquals(expected, expected);
    }

    @Test
    public void notEnoughRangeHits() throws IOException {
        SearchHit[] searchHits = new SearchHit[1];
        searchHits[0] = Mockito.mock(SearchHit.class);
        Map<String ,Object> sourceMap = createSourceMap(ERROR_LINE+1, MSG_BEFORE_STACK);
        when(searchHits[0].getSourceAsMap()).thenReturn(sourceMap);
        when(stackTrace.logDao.getHitsFromIndex(any(SearchRequest.class))).thenReturn(searchHits);
        ImmutableList < String > actual = stackTrace.findStack(1, FILE_NAME);
        ImmutableList < String > expected = stackTrace.noStackFound();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void exceedNumberOfAllowedMsgs() throws IOException {
        // Number of allowed messages before stack trace is exceeded 
        SearchHit[] searchHits = new SearchHit[ALLOWED_MESSAGES+1];
        for (int i = 0; i <= ALLOWED_MESSAGES; i++) {
            Map<String ,Object> sourceMap = createSourceMap(ERROR_LINE+1+i, MSG_BEFORE_STACK);
            searchHits[i] = Mockito.mock(SearchHit.class);
            when(searchHits[i].getSourceAsMap()).thenReturn(sourceMap);
        }
        when(stackTrace.logDao.getHitsFromIndex(any(SearchRequest.class))).thenReturn(searchHits);
        ImmutableList < String > actual = stackTrace.findStack(ERROR_LINE, FILE_NAME);
        ImmutableList < String > expected = stackTrace.noStackFound();
        Assert.assertEquals(expected, actual);
    }
    
    @Test
    public void fitInAllowedNumberOfMsgs() throws IOException {
        // Number of log lines before stack trace <= allowed messages before stack
        SearchHit[] searchHits = new SearchHit[ALLOWED_MESSAGES+1];
        for (int i = 0; i < ALLOWED_MESSAGES; i++) {
            Map<String ,Object> sourceMap = createSourceMap(ERROR_LINE+1+i, MSG_BEFORE_STACK);
            searchHits[i] = Mockito.mock(SearchHit.class);
            when(searchHits[i].getSourceAsMap()).thenReturn(sourceMap);
        }
        Map<String ,Object> sourceMap = createSourceMap(ERROR_LINE+ALLOWED_MESSAGES+1, STACK_LOG_LINE);
        searchHits[ALLOWED_MESSAGES] = Mockito.mock(SearchHit.class);
        when(searchHits[ALLOWED_MESSAGES].getSourceAsMap()).thenReturn(sourceMap);

        when(stackTrace.logDao.getHitsFromIndex(any(SearchRequest.class))).thenReturn(searchHits);
        ImmutableList < String > actual = stackTrace.findStack(ERROR_LINE, FILE_NAME);
        Builder < String > expected = ImmutableList.< String > builder();
        for(int i = 0 ; i < ALLOWED_MESSAGES; i++) {
            expected.add(MSG_BEFORE_STACK);
        }
        expected.add(STACK_LOG_LINE);
        Assert.assertEquals(expected.build(), actual);
    }

    @Test
    public void stackExceedsBatchSizeRequest() throws IOException {
        //stack exceeds number of loglines fetched. Enters while loop
        SearchHit[] searchHits1 = new SearchHit[BATCH_SIZE];
        for (int i = 0; i < BATCH_SIZE; i++) {
            Map<String ,Object> sourceMap = createSourceMap(ERROR_LINE+1+i, STACK_LOG_LINE);
            searchHits1[i] = Mockito.mock(SearchHit.class);
            when(searchHits1[i].getSourceAsMap()).thenReturn(sourceMap);
        }
        SearchHit[] searchHits2 = new SearchHit[1];
        Map<String ,Object> sourceMap2 = createSourceMap(ERROR_LINE+BATCH_SIZE+1, MSG_BEFORE_STACK);
        searchHits2[0] = Mockito.mock(SearchHit.class);
        when(searchHits2[0].getSourceAsMap()).thenReturn(sourceMap2);
        when(stackTrace.logDao.getHitsFromIndex(any(SearchRequest.class))).thenAnswer(new Answer() {
            private int count = 0;
            public SearchHit[] answer(InvocationOnMock invocation) {
                if (count == 0){
                    count++;
                    return searchHits1;
                }
                return searchHits2;
            }
        });

        ImmutableList < String > actual = stackTrace.findStack(ERROR_LINE, FILE_NAME);
        Builder < String > expected = ImmutableList.< String > builder();
        for (int i = 0 ;i < BATCH_SIZE; i++) {
            expected.add(STACK_LOG_LINE);
        }
        Assert.assertEquals(expected.build(), actual);
    }

    private Map <String, Object > createSourceMap(Integer logLineNumber, String logText) {
        Map <String, Object> map = new HashMap();
        map.put(LogFields.LOG_LINE_NUMBER, logLineNumber);
        map.put(LogFields.LOG_TEXT, logText);
        return map;
    }
}