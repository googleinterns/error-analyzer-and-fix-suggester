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

import com.google.error_analyzer.backend.BooleanQuery;
import com.google.error_analyzer.backend.LogDao;
import com.google.error_analyzer.backend.MockLogDao;
import java.io.IOException;
import java.util.*;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.RegexpQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.SearchHits;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class BooleanQueryTest {

    private final String fileName = "file"; 
    private final BooleanQuery boolQuery = new BooleanQuery();
    SearchRequest searchRequest = null;
    SearchSourceBuilder searchSourceBuilder = null;

    @Mock
    LogDao database;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        searchRequest = boolQuery.createSearchRequest(fileName);
        searchSourceBuilder = searchRequest.source();
    }

    @Test
    public void createSearchRequestWithBoolQuery() {
        String[] indices = searchRequest.indices();
        Assert.assertEquals(indices.length, 1);
        Assert.assertEquals(indices[0], fileName);
    }

    @Test
    public void keyWordQueryString() {
        BoolQueryBuilder boolQuery = (BoolQueryBuilder) searchSourceBuilder
            .query();
        List < QueryBuilder > queryList = boolQuery.should();
        MatchQueryBuilder matchQuery = (MatchQueryBuilder) queryList.get(1);
        String actual = (String) matchQuery.value();
        String expected = "error OR fatal OR severe OR exit OR exception";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void regexpQueryString() {
        BoolQueryBuilder boolQuery = (BoolQueryBuilder) searchSourceBuilder
            .query();
        List < QueryBuilder > queryList = boolQuery.should();
        RegexpQueryBuilder regexQuery = (RegexpQueryBuilder) queryList.get(0);
        String actual = regexQuery.value();
        String expected = ".*exception";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void findErrorsTest() throws IOException {
        MockLogDao mockDatabase = new MockLogDao();
        String errorFileName = mockDatabase.findAndStoreErrors(fileName);
        ArrayList < String > actual = mockDatabase.errorFile;
        when(database.findAndStoreErrors(fileName))
            .thenReturn(errorFileName);
        database.findAndStoreErrors(fileName);
        ArrayList < String > expected = new ArrayList < String > () {
            {
                add("Error: nullPointerException");
                add("Severe: Could not find index file");
                add("warning: NullPointerException");
            }
        };
        verify(database, times(1)).findAndStoreErrors(fileName);
        Assert.assertEquals(expected, actual);
    }
}