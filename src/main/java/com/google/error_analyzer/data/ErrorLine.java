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
import java.util.Comparator;
import java.util.*;
import java.lang.Object;

/**
* Search hits will be converted to ErrorLine objects and used further to strore in result index.
*/

public class ErrorLine {

    private String logText;
    private int logLineNumber;
    public ErrorLine (String log_text, int logLineNumber) {
        this.logText = log_text;
        this.logLineNumber = logLineNumber;
    } 

    public boolean equals(ErrorLine other) {
        return (other.logText.equals(this.logText) && (other.logLineNumber == this.logLineNumber)) ;
    }

}