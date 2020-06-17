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

import java.util.*;
import com.google.common.collect.ImmutableList;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder.Field;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

public class LogDaoHelper {

    // return ArrayList of hit ids corresponding to given searchhit list
    public ImmutableList < String > hitId(SearchHit[] searchHits) {
        ArrayList < String > ids = new ArrayList();
        for (SearchHit hit: searchHits) {
            String id = hit.getId();
            ids.add(id);
        }
        return ImmutableList.copyOf(ids);
    }

    // return ArrayList of content of specified field  corresponding to given searchhit list
    public ImmutableList < String > hitFieldContent(SearchHit[] searchHits, String field) 
    {
        ArrayList < String > fieldContent = new ArrayList();
        for (SearchHit hit: searchHits) {
            String resultString = String.valueOf(hit.getSourceAsMap().get(field));
            fieldContent.add(resultString);
        }
        return ImmutableList.copyOf(fieldContent);
    }

    //returns hashmap of hit ids and highlighted content 
    public HashMap < String, String > getHighLightedText
    (ImmutableList < SearchHit > searchHits, String field)
    {
        HashMap < String, String > searchResult = new HashMap();
        for (SearchHit hit: searchHits) {
            Map < String, HighlightField > highlightFields = hit.getHighlightFields();
            HighlightField highlight = highlightFields.get(field);
            if(highlight.fragments().length != 0) {
                String fragmentString = (highlight.fragments())[0].string();
                searchResult.put(hit.getId(), fragmentString);
            }
        }
        return searchResult;
    }
}