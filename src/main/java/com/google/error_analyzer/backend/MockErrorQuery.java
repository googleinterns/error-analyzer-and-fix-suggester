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
import com.google.error_analyzer.data.RegexExpressions;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 
* This class mocks the BooleanQuery of elasticsearch.
*/

public class MockErrorQuery {
    private final Keywords keyWords = new Keywords();
    private final String keyWordsQueryString = keyWords.getQueryString();
    private final String[] keyWordsList = keyWordsQueryString.split(" OR ");
    private final HashSet<String> keyWordSet = new HashSet<>(Arrays.asList(keyWordsList));
    private final RegexExpressions regex = new RegexExpressions();
    private final String regexQueryString = regex.getQueryString();
    private final String[] regexList = regexQueryString.split(Pattern.quote("|"));
    
    //return true if document contains keyword or matches regex
    public boolean matchesCondition(String dbenntry) {
        String document = dbenntry.toLowerCase().replaceAll("[^a-zA-Z0-9]", " ");
        String[] termsList = document.split(" ");
        for (int i = 0; i < termsList.length; i++) {
            String term = termsList[i];
            if (keyWordSet.contains(term) || matchesRegex(term)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesRegex(String term) {
        for (int i = 0; i < regexList.length; i++){
            Pattern pattern = Pattern.compile(regexList[i]);
            Matcher match = pattern.matcher(term);
            if (match.matches()) {
                return true;
            }
        }
        return false;
    } 

    
}