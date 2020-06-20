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

import com.google.error_analyzer.data.Keywords;
import com.google.error_analyzer.data.RegexStrings;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RegexpQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;


public class BooleanQuery {
    private static final Logger logger = LogManager.getLogger(BooleanQuery.class);
    private final String logTextField = "logText";
    private final String logLineNumberField = "logLineNumber";
    private final Integer requestSize = 10000;
    //search db using regex and keywords and store back in db searchHits sorted by logLineNumber
    public SearchRequest createSearchRequest(String fileName) {
        BoolQueryBuilder boolQuery = buildBoolQuery();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .size(1000)
            .query(boolQuery)
            .sort(logLineNumberField);
        SearchRequest searchRequest = new SearchRequest(fileName);
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }
    
    //private combine matchquery and regex query and return a bool query
    private BoolQueryBuilder buildBoolQuery() {
        MatchQueryBuilder matchQuery = buildMatchQuery();
        RegexpQueryBuilder regexQuery = buildRegexQuery();
        BoolQueryBuilder boolQuery = new BoolQueryBuilder()
            .minimumShouldMatch(1)
            .should(regexQuery)
            .should(matchQuery);
        return boolQuery;
    }

    private MatchQueryBuilder buildMatchQuery() {
        Keywords errorKeywords = new Keywords();
        String keywordsQueryString = errorKeywords.getQueryString();
        MatchQueryBuilder matchQuery = new MatchQueryBuilder
            (logTextField, keywordsQueryString);
        return matchQuery;
    }

    private RegexpQueryBuilder buildRegexQuery() {
        RegexStrings regexExpressions = new RegexStrings();
        String regexQueryString = regexExpressions.getQueryString();
        RegexpQueryBuilder regexQuery = new RegexpQueryBuilder(logTextField,regexQueryString);
        return regexQuery;
    }
}