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
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;


@RunWith(MockitoJUnitRunner.class)
public final class StackTraceTest {
    private final String FILE_NAME = "file"; 
    private StackTrace stackTrace;
    private final String MSG_BEFORE_STACK =  
    "Running com.google.error_analyzer at maven";
    private final String STACK_LOG_LINE = 
    "cvr123 At com.stack.stacktrace.StackTraceExample.methodB";
    private final static Integer ALLOWED_MESSAGES = 1; 
    private final static Integer BATCH_SIZE = 2;
    private final Integer ERROR_LINE = 1;

    @Before
    public void setUp() {
        stackTrace = new StackTrace();
        stackTrace.logDao = Mockito.mock(MockLogDao.class);
        ReflectionTestUtils.setField(stackTrace, "BATCH_SIZE", BATCH_SIZE);
        ReflectionTestUtils.setField(stackTrace, "ALLOWED_MESSAGES",
         ALLOWED_MESSAGES);
    }

    @Test
    public void findStack_noHitsFromRangeQuery() throws IOException {
        // No hit is returned from searchRequest i.e. End of file
        ImmutableList < SearchHit > searchHits = ImmutableList.< SearchHit > builder().build();
        when(stackTrace.logDao.getHitsFromIndex(any(SearchRequest.class)))
        .thenReturn(searchHits);

        ImmutableList < String > actual = stackTrace.findStack(ERROR_LINE, FILE_NAME);
        ImmutableList < String > expected = ImmutableList.<String>builder() .build(); 
        Assert.assertEquals(expected, expected);
    }

    @Test
    public void findStack_notEnoughRangeHits() throws IOException {
        // When there are not enough searchHits, stored messages get disrcarded
        Builder < SearchHit > searchHitsBuilder = ImmutableList.< SearchHit > builder();
        SearchHit hit = Mockito.mock(SearchHit.class);
        Map<String ,Object> sourceMap = createSourceMap(ERROR_LINE+1,
        MSG_BEFORE_STACK);
        when(hit.getSourceAsMap()).thenReturn(sourceMap);
        searchHitsBuilder.add(hit);
        when(stackTrace.logDao.getHitsFromIndex(any(SearchRequest.class)))
        .thenReturn(searchHitsBuilder.build());

        ImmutableList < String > actual = stackTrace.findStack(ERROR_LINE, FILE_NAME);
        ImmutableList < String > expected = ImmutableList.<String>builder() .build(); 
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void findStack_exceedNumberOfAllowedMsgs() throws IOException {
        // Number of allowed messages before stack trace is exceeded
        Builder < SearchHit > searchHitsBuilder = ImmutableList.< SearchHit > builder();
        for (int i = 0; i <= ALLOWED_MESSAGES; i++) {
            Map<String ,Object> sourceMap = createSourceMap(ERROR_LINE+1+i,
            MSG_BEFORE_STACK);
            SearchHit hit = Mockito.mock(SearchHit.class);
            when(hit.getSourceAsMap()).thenReturn(sourceMap);
            searchHitsBuilder.add(hit);
        }
        when(stackTrace.logDao.getHitsFromIndex(any(SearchRequest.class)))
        .thenReturn(searchHitsBuilder.build());

        ImmutableList < String > actual = stackTrace.findStack(ERROR_LINE, FILE_NAME);
        ImmutableList < String > expected = ImmutableList.<String>builder() .build(); 
        Assert.assertEquals(expected, actual);
    }
    
    @Test
    public void findStack_fitInAllowedNumberOfMsgs() throws IOException {
        // Number of log lines before stack trace <= allowed messages before stack
        Builder < SearchHit > searchHitsBuilder = ImmutableList.< SearchHit > builder();
        for (int i = 0; i < ALLOWED_MESSAGES+1; i++) {
            SearchHit hit = Mockito.mock(SearchHit.class);
            searchHitsBuilder.add(hit);
            Map<String ,Object> sourceMap;
            if (i == ALLOWED_MESSAGES) {
                sourceMap = createSourceMap
                (ERROR_LINE+i+1, STACK_LOG_LINE);
            } else {
                sourceMap = createSourceMap(ERROR_LINE+1+i,
                MSG_BEFORE_STACK);
            }
            when(hit.getSourceAsMap()).thenReturn(sourceMap);
        }
        when(stackTrace.logDao.getHitsFromIndex(any(SearchRequest.class)))
        .thenReturn(searchHitsBuilder.build());

        ImmutableList < String > actual = stackTrace.findStack(ERROR_LINE, FILE_NAME);
        Builder < String > expected = ImmutableList.< String > builder();
        for(int i = 0 ; i < ALLOWED_MESSAGES; i++) {
            expected.add(MSG_BEFORE_STACK);
        }
        expected.add(STACK_LOG_LINE);
        Assert.assertEquals(expected.build(), actual);
    }

    @Test
    public void findStack_stackExceedsBatchSizeRequest() throws IOException {
        //Stack exceeds number of loglines fetched. Enters while loop
        Builder < SearchHit > searchHitsBuilder1 = ImmutableList.< SearchHit > builder();
        for (int i = 0; i < BATCH_SIZE; i++) {
            Map<String ,Object> sourceMap = createSourceMap(ERROR_LINE+1+i,
            STACK_LOG_LINE);
            SearchHit hit = Mockito.mock(SearchHit.class);
            when(hit.getSourceAsMap()).thenReturn(sourceMap);
            searchHitsBuilder1.add(hit);
        }
        Builder < SearchHit > searchHitsBuilder2 = ImmutableList.< SearchHit > builder();
        Map<String ,Object> sourceMap2 = createSourceMap(ERROR_LINE+BATCH_SIZE+1, MSG_BEFORE_STACK);
        SearchHit hit = Mockito.mock(SearchHit.class);
        when(hit.getSourceAsMap()).thenReturn(sourceMap2);
        searchHitsBuilder2.add(hit);
        when(stackTrace.logDao.getHitsFromIndex(any(SearchRequest.class)))
        .thenAnswer(new Answer() {
            private int count = 0;
            public Object answer(InvocationOnMock invocation) {
                if (count == 0){
                    count++;
                    return searchHitsBuilder1.build();
                }
                return searchHitsBuilder2.build();
            }
        });

        ImmutableList < String > actual = stackTrace.findStack(ERROR_LINE, 
        FILE_NAME);
        Builder < String > expected = ImmutableList.< String > builder();
        for (int i = 0 ;i < BATCH_SIZE; i++) {
            expected.add(STACK_LOG_LINE);
        }
        Assert.assertEquals(expected.build(), actual);
    }

    private Map <String, Object > createSourceMap(Integer logLineNumber, 
    String logText) {
        Map <String, Object> map = new HashMap();
        map.put(LogFields.LOG_LINE_NUMBER, logLineNumber);
        map.put(LogFields.LOG_TEXT, logText);
        return map;
    }
}