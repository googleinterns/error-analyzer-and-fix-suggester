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

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StackTraceFormat {

    private static final ArrayList < Pattern > regexStrings = new ArrayList < Pattern > () {
        {
            add(Pattern.compile(".{0,20} at .+"));
            add(Pattern.compile(".{0,20} new .+"));
            add(Pattern.compile(".{0,20} runnable .+"));
            add(Pattern.compile(".{0,20} lock .+"));
            add(Pattern.compile(".{0,20} locked .+"));
            add(Pattern.compile(".{0,20} blocked .+"));
            add(Pattern.compile(".{0,20} waiting .+"));
            add(Pattern.compile(".{0,20} timed waiting .+"));
            add(Pattern.compile(".{0,20} terminated .+"));
        }
    };

    public static boolean matchesFormat (String logText) {
        String logTextInLowerCase = logText.toLowerCase();
        for (Pattern regex : regexStrings ) {
            Matcher match = regex.matcher(logTextInLowerCase);
            if (match.matches()) {
                return true;
            }
        }
        return false;
    }
}