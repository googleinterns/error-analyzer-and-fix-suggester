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

import com.google.error_analyzer.backend.Database;
import com.google.error_analyzer.backend.MockDatabase;
import com.google.error_analyzer.backend.Search;
import java.util.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito.*;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.mockito.InjectMocks;


@RunWith(MockitoJUnitRunner.class)
public final class SearchTest {

    private String searchString = "scheduler appengine";
    private String field = "name";
    private MockDatabase mockdb;
    private String fileName = "file";

    @Mock
    Database database;

    @InjectMocks
    Search search;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        search = new Search();
        search.database = database;
        mockdb = new MockDatabase();
    }

    // maintaining window of given length
    @Test
    public void fullTextSearch() throws IOException {
        ArrayList < SearchHit > searchHits = mockdb.fullTextSearch(fileName, searchString, field);
        HashMap < String, String > highlights = mockdb.getHighLightedText(searchHits, field);
        when(database.fullTextSearch(fileName, searchString, field)).thenReturn(searchHits);
        when(database.getHighLightedText(searchHits, field)).thenReturn(highlights);
        HashMap < String, String > actual = search.searchDataBase(fileName, searchString);
        HashMap < String, String > expected = new HashMap();
        expected.put("1", "info: start appengine");
        expected.put("2", "scheduler shutting down");
        Assert.assertTrue(expected.equals(actual));
    }
}