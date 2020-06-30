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
import com.google.error_analyzer.data.constant.StackTraceFormat;
import com.google.error_analyzer.data.Document;
import java.util.Set;
import java.util.HashSet;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

/**
* This class filters out searchHits from BooleanQuery.
* Remove SearchHit that are part of stack.
* Remove error Searchit that have occured previously.
*/
public class FilterErrors {
    private static final Logger logger = LogManager.getLogger(StoreLogs.class);
    
    //Filter SeacrchHits and return Document List for storing.
    public ImmutableList < Document > filterErrorSearchHits (SearchHits hits) {
        Set < String > errorSet = new HashSet < String >();
        Builder < Document > documentList = ImmutableList.< Document > builder(); 
        for (SearchHit hit : hits) {
            String logText = (String) hit.getSourceAsMap().get(LogFields.LOG_TEXT);
            if (shouldIncludeError(logText, errorSet)) {
                Document document = new Document(hit.getId(), hit.getSourceAsString());
                documentList.add(document);
            }
        }
        return documentList.build();
    }

    //return false if error is repeated or part of stack
    private Boolean shouldIncludeError(String logText, Set < String > errorSet) {
        if (StackTraceFormat.matchesFormat(logText)) {
            return false;
        }
        logText = logText.replaceAll("[^a-zA-Z]", " ");
        if (errorSet.contains(logText)) {
            logger.info("Repeated error");
            return false;
        }
        errorSet.add(logText);
        return true;
    }
}