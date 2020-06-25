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
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
// import org.mockito.Mock;
import org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// @RunWith(MockitoJUnitRunner.class)
public final class StackTraceTest {
    private final String fileName = "file"; 
    private StackTrace stackTrace;

    @Mock
    SearchHit[] searchHits;

    @Before
    public void setUp() {
        stackTrace = new StackTrace();
        stackTrace.logDao = new MockLogDao();
    }

    @Test
    public void noHitsFromRangeQuery() throws IOException {
        ArrayList < String > actual = stackTrace.findStack(1, fileName);
        ArrayList < String > expected = stackTrace.noStackFound();
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void notEnoughRangeHits() throws IOException {
        searchHits = new SearchHit[1];
        Map<String ,Object> s1 = new HashMap();
        s1.put("logLineNumber", 2);
        s1.put("logText", "Running com.google.error_analyzer at maven");
        searchHits[0] = Mockito.mock(SearchHit.class);
        when(searchHits[0].getSourceAsMap()).thenReturn(s1);
        stackTrace.logDao = Mockito.mock(MockLogDao.class);
        when(stackTrace.logDao.getHitsFromIndex(any(SearchRequest.class))).thenReturn(searchHits);
        ArrayList < String > actual = stackTrace.findStack(1, fileName);
        ArrayList < String > expected = stackTrace.noStackFound();
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void exceedNumberOfAllowedMsgs() throws IOException {
        searchHits = new SearchHit[6];
        for(int i = 0;i<6;i++){
            Map<String ,Object> sourceMap = new HashMap();
            sourceMap.put("logText", "Running com.google.error_analyzer at maven");
            sourceMap.put("logLineNumber", 2+i);
            searchHits[i] = Mockito.mock(SearchHit.class);
            when(searchHits[i].getSourceAsMap()).thenReturn(sourceMap);
        }
        stackTrace.logDao = Mockito.mock(MockLogDao.class);
        when(stackTrace.logDao.getHitsFromIndex(any(SearchRequest.class))).thenReturn(searchHits);
        ArrayList < String > actual = stackTrace.findStack(1, fileName);
        ArrayList < String > expected = stackTrace.noStackFound();
        Assert.assertEquals(actual, expected);
    }
    
}