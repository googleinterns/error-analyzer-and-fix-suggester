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

package com.google.error_analyzer.data.constant;

import java.util.ArrayList;

/**
* This class contains all the regular expressions used in regex query.
* A log line will apear in search hits if a a term matches the regex.
*/
public class RegexStrings{
    private final static String PIPE_OPERATOR = "|";
    private static final ArrayList < String > REGEX_LIST = 
    new ArrayList < String > () {
        {
            add(".*exception");
            add(".*error");
        }
    };

    public static String getQueryString() {
        String queryString = "";
        for (String keyword : REGEX_LIST) {
            if (queryString.isEmpty()) {
                queryString = queryString.concat(keyword);
            } else {
                queryString = queryString.concat(PIPE_OPERATOR.concat(keyword));
            }
        }
        return queryString;
    }
}