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
import com.google.error_analyzer.data.constant.LogFields;
import com.google.error_analyzer.backend.FilterErrors;
import com.google.error_analyzer.data.Document;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.TotalHits.Relation;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.when;


public class FilterErrorsTest {
    private final FilterErrors filterErrors = new FilterErrors();
    private static final String NO_ERROR_FOUND_MSG = "No errors were found in this file";
    private final String ERROR_LOG_LINE = "01 Error: nullPointerException";
    private final String ERROR_LOG_LINE_JSON = 
    "{\"logLineNumber\" : 1, \"logText\" : \"01 Error: nullPointerException\"}";
    private final String REPEATED_ERROR_LOG_LINE = "02 Error: nullPointerException";
    private final String REPEATED_ERROR_LOG_LINE_JSON = 
    "{\"logLineNumber\" : 2, \"logText\" : \"01 Error: nullPointerException\"}";
    private final String STACK_LOG_LINE = 
    "cvr123 At com.stack.stacktrace.StackTraceExample.methodB";
    private final String STACK_LOG_LINE_JSON = 
    "{\"logLineNumber\" : 3, \"logText\" : \"cvr123 At com.stack.stacktrace.StackTraceExample.methodB\"}";
    // private final 
    SearchHit errorHit;
    SearchHit repeatedErrorHit;
    SearchHit partOfStackHit;

    @Before
    public void setUp() {
        errorHit = Mockito.mock(SearchHit.class);
        when(errorHit.getSourceAsMap()).thenReturn(createSourceMap(1, ERROR_LOG_LINE));
        when(errorHit.getId()).thenReturn("1");
        when(errorHit.getSourceAsString()).thenReturn(ERROR_LOG_LINE_JSON);
        
        repeatedErrorHit = Mockito.mock(SearchHit.class);
        when(repeatedErrorHit.getSourceAsMap()).thenReturn(createSourceMap(2, REPEATED_ERROR_LOG_LINE));
        when(repeatedErrorHit.getId()).thenReturn("2");
        when(repeatedErrorHit.getSourceAsString()).thenReturn(REPEATED_ERROR_LOG_LINE_JSON);
        
        partOfStackHit = Mockito.mock(SearchHit.class);
        when(partOfStackHit.getSourceAsMap()).thenReturn(createSourceMap(3, STACK_LOG_LINE));
        when(partOfStackHit.getId()).thenReturn("3");
        when(partOfStackHit.getSourceAsString()).thenReturn(STACK_LOG_LINE_JSON);
    }

    @Test
    public void filterErrorSearchHits_errorsAreRepeated()
    throws IOException {
        TotalHits totalHits = new TotalHits(2, Relation.valueOf("EQUAL_TO"));
        SearchHit[] hitArray = new SearchHit[]{errorHit, repeatedErrorHit};
        SearchHits hits = new SearchHits(hitArray, totalHits, 1);
        ImmutableList < Document > actual = filterErrors.filterErrorSearchHits(hits);

        Document errorDocument = new Document ("1", ERROR_LOG_LINE_JSON);
        ImmutableList < Document > expected = ImmutableList.<Document>builder() 
            .add(errorDocument).build();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void filterErrorSearchHits_searchHitHasStackLogLine() 
    throws IOException {
        TotalHits totalHits = new TotalHits(2, Relation.valueOf("EQUAL_TO"));
        SearchHit[] hitArray = new SearchHit[]{errorHit, partOfStackHit};
        SearchHits hits = new SearchHits(hitArray, totalHits, 1);
        ImmutableList < Document > actual = filterErrors.filterErrorSearchHits(hits);

        Document errorDocument = new Document ("1", ERROR_LOG_LINE_JSON);
        ImmutableList < Document > expected = ImmutableList.<Document>builder() 
            .add(errorDocument).build();
        Assert.assertEquals(expected, actual);
    }
    
    @Test
    public void filterErrorSearchHits_noErrorsAreFound() 
    throws IOException {
        TotalHits totalHits = new TotalHits(2, Relation.valueOf("EQUAL_TO"));
        SearchHit[] hitArray = new SearchHit[]{partOfStackHit, partOfStackHit};
        SearchHits hits = new SearchHits(hitArray, totalHits, 1);
        ImmutableList < Document > actual = filterErrors.filterErrorSearchHits(hits);

        Document errorDocument = 
        new Document ("1", 1, NO_ERROR_FOUND_MSG);
        ImmutableList < Document > expected = ImmutableList.<Document>builder() 
            .add(errorDocument).build();
        Assert.assertEquals(expected, actual);
    }

    private Map <String, Object > createSourceMap(Integer logLineNumber, 
        String logText) {
        Map <String, Object> map = new HashMap();
        map.put(LogFields.LOG_LINE_NUMBER, logLineNumber);
        map.put(LogFields.LOG_TEXT, logText);
        return map;
    }
}