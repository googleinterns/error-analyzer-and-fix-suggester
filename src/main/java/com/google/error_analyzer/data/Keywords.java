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

package com.google.error_analyzer.data;

import java.util.ArrayList;

/**
* This class contains all the keywords used in fulltext query.
* A log line will apear in search hits if one or more of these terms appear in it.
*/

public class Keywords {
    
    private ArrayList<String> createList() {
        ArrayList<String> keywordsList = new ArrayList<String>();
        keywordsList.add("error");
        keywordsList.add("fatal");
        keywordsList.add("severe");
        keywordsList.add("exit");
        keywordsList.add("exception");
        return keywordsList;
    }

    //returns kewords as a single string combined together in OR logic.
    public String getQueryString() {
        ArrayList<String> keywordsList = createList();
        String queryString = "";
        for (String keyword : keywordsList) {
            if (queryString.length() == 0){
                queryString = queryString.concat(keyword);
            } else {
                queryString = queryString.concat(" OR " + keyword);
            }
        }
        return queryString;
    }
    
}