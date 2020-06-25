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
import com.google.error_analyzer.data.constant.LogFields;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.RegexpQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Test;

public final class BooleanQueryTest {

    @Test
    public void createSearchRequestWithBoolQuery() {
        String fileName = "file"; 
        String expectedMatchQueryString = "error OR fatal OR severe OR exit OR exception";
        String expectedRegexQueryString = ".*exception";
        Integer expectedRequestSize = 10000;
        BoolQueryBuilder boolQuery = buildBoolQuery(expectedMatchQueryString, expectedRegexQueryString);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .size(expectedRequestSize)
            .query(boolQuery)
            .sort(LogFields.LOG_LINE_NUMBER);
        SearchRequest expected = new SearchRequest(fileName);
        expected.source(searchSourceBuilder);
        BooleanQuery booleanQuery = new BooleanQuery();
        SearchRequest actual = booleanQuery.createSearchRequest(fileName);
        Assert.assertEquals(actual, expected);
    }
    
    private BoolQueryBuilder buildBoolQuery(String matchQueryString, 
    String regexQueryString) {
        MatchQueryBuilder matchQuery = buildMatchQuery(matchQueryString);
        RegexpQueryBuilder regexQuery = buildRegexQuery(regexQueryString);
        BoolQueryBuilder boolQuery = new BoolQueryBuilder()
            .minimumShouldMatch(1)
            .should(regexQuery)
            .should(matchQuery);
        return boolQuery;
    }

    private MatchQueryBuilder buildMatchQuery(String matchQueryString) {
        MatchQueryBuilder matchQuery = new MatchQueryBuilder
            (LogFields.LOG_TEXT, matchQueryString);
        return matchQuery;
    }

    private RegexpQueryBuilder buildRegexQuery(String regexQueryString) {
        RegexpQueryBuilder regexQuery = new RegexpQueryBuilder
            (LogFields.LOG_TEXT,regexQueryString);
        return regexQuery;
    }
}