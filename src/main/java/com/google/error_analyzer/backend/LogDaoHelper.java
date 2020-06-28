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
import java.util.*;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder
                                                        .Field;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

public class LogDaoHelper {

    // return ImmutableList of hit ids corresponding to given searchhit list
    public ImmutableList < String > hitId(ImmutableList < SearchHit > searchHits) {
        Builder<String> ids = ImmutableList.<String>builder();
        for (SearchHit hit: searchHits) {
            String id = hit.getId();
            ids.add(id);
        }
        return ids.build();
    }

    // return ImmutableList of content of specified field  corresponding to 
    // given searchhit list
    public ImmutableList < String > hitFieldContent(
    ImmutableList < SearchHit > searchHits, String field) {
        Builder<String> fieldContent = ImmutableList.<String>builder();
        for (SearchHit hit: searchHits) {
            String resultString = 
                String.valueOf(hit.getSourceAsMap().get(field));
            fieldContent.add(resultString);
        }
        return fieldContent.build();
    }

    //returns list of highlighted content 
    public ImmutableList < String > getHighLightedText(
    ImmutableList < SearchHit > searchHits, String field) {
       Builder<String> searchResult = ImmutableList.<String>builder();
        for (SearchHit hit: searchHits) {
            Map < String, HighlightField > highlightFields 
                = hit.getHighlightFields();
            HighlightField highlight = highlightFields.get(field);
            if(highlight.fragments().length != 0) {
                String fragmentString = (highlight.fragments())[0].string();
                searchResult.add(fragmentString);
            }
        }
        return searchResult.build();
    }

    //returns index name which stores the error logs
    public static String getErrorIndexName (String fileName) {
        return fileName.concat("error");
    }
}