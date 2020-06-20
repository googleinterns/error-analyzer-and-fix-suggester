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
import com.google.error_analyzer.backend.LogDaoHelper;
import com.google.error_analyzer.backend.MockLogDao;
import com.google.error_analyzer.data.Keywords;
import com.google.error_analyzer.data.RegexStrings;
import java.io.IOException;
import java.util.ArrayList;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.search.SearchHits;
import org.junit.Assert;
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

    @Mock
    LogDao database;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Test
    public void createSearchRequestWithBoolQuery() {
        BooleanQuery boolQuery = new BooleanQuery();
        SearchRequest searchRequest = boolQuery.createSearchRequest(fileName);
        String[] indices = searchRequest.indices();
        Assert.assertEquals(indices.length, 1);
        Assert.assertEquals(indices[0], fileName);
    }

    @Test
    public void keyWordQueryString() {
        String actual = Keywords.getQueryString();
        String expected = "error OR fatal OR severe OR exit OR exception";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void regexpQueryString() {
        String actual = RegexStrings.getQueryString();
        String expected = ".*exception";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void errorFileNameAppend() {
        String actual = LogDaoHelper.getErrorIndexName(fileName);
        Assert.assertEquals("fileerror", actual);
    }

    @Test
    public void findErrorsTest() throws IOException {
        MockLogDao mockDatabase = new MockLogDao();
        String errorFileName = mockDatabase.findAndStoreErrors(fileName);
        ArrayList<String> actual = mockDatabase.errorDatabase;
        when(database.findAndStoreErrors(fileName)).thenReturn(errorFileName);
        database.findAndStoreErrors(fileName);
        ArrayList<String> expected = new ArrayList<String>(){
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