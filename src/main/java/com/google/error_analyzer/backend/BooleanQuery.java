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

import com.google.error_analyzer.data.constant.Keywords;
import com.google.error_analyzer.data.constant.LogFields;
import com.google.error_analyzer.data.constant.RegexStrings;
import java.util.*;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RegexpQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

/** Build search request for boolean query.
* Match query uses Keywords class for query string
* Regex query uses RegexStrings class for query string
*/

public class BooleanQuery {
    private final static Integer requestSize = 10000; //limited by ElasticSearch settings
    private final static Integer minimumMatch = 1;

    //create searchRequest to seach index file for errors
    public SearchRequest createSearchRequest(String fileName) {
        String matchQueryString = Keywords.getQueryString();
        String regexQueryString = RegexStrings.getQueryString();
        BoolQueryBuilder boolQuery = buildBoolQuery(matchQueryString, regexQueryString);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .size(requestSize)
            .query(boolQuery)
            .sort(LogFields.LOG_LINE_NUMBER);
        SearchRequest searchRequest = new SearchRequest(fileName);
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }

    //Combine matchquery and regex query and return a bool query
    private BoolQueryBuilder buildBoolQuery(String matchQueryString, 
    String regexQueryString) {
        MatchQueryBuilder matchQuery = buildMatchQuery(matchQueryString);
        RegexpQueryBuilder regexQuery = buildRegexQuery(regexQueryString);
        BoolQueryBuilder boolQuery = new BoolQueryBuilder()
            .minimumShouldMatch(minimumMatch)
            .should(regexQuery)
            .should(matchQuery);
        return boolQuery;
    }

    private MatchQueryBuilder buildMatchQuery(String matchQueryString) {
        return new MatchQueryBuilder
            (LogFields.LOG_TEXT, matchQueryString);
    }

    private RegexpQueryBuilder buildRegexQuery(String regexQueryString) {
        return new RegexpQueryBuilder
            (LogFields.LOG_TEXT,regexQueryString);
    }
}