// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.error_analyzer;

import com.google.error_analyzer.backend.FulltextSearchQuery;
import com.google.error_analyzer.data.ErrorLine;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.apache.http.HttpHost;
import java.util.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
* Class to test fulltext search query. Mock object for ES to br created later.
*/

@RunWith(JUnit4.class)
public final class FulltextSearchQueryTest{
    private String indexFile = "trial_index"; //later fetched from request
    RestHighLevelClient client;
    @Before
    public void setUp() {
        client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
    }

@Test
public void keywordSearchQuery(){
    // Mock object for client will be added soon.
    FulltextSearchQuery searchQuery = new FulltextSearchQuery();
    String actual = searchQuery.getErrorsAsString(indexFile, client);

    String expected = "[{\"logText\":\"ERROR c.g.s.FulltextSearchQuery could not complete query request\",\"logLineNumber\":90},{\"logText\":\"ERROR c.g.s.FulltextSearchQuery could not complete query request\",\"logLineNumber\":3},{\"logText\":\"ERROR: c.g.s.FulltextSearchQuery could not complete query request\",\"logLineNumber\":23}]";
    Assert.assertEquals(expected, actual);
}


}