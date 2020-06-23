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
package com.google.error_analyzer.data;

// class contains hits of latest full-text search
import java.util.*;

public class SearchErrors {

    // map contains hit id as key and highlighted hit content (content of specified field) as value
    private static HashMap < String, String > searchedErrors;

    public SearchErrors() {
        if (searchedErrors == null)
            searchedErrors = new HashMap();
    }

    // method to add hits
    public HashMap < String, String > getSearchedErrors() {
        return searchedErrors;
    }

    // method to fetch hits
    public void setSearchedErrors(HashMap < String, String > list) {
        searchedErrors = list;
    }
}