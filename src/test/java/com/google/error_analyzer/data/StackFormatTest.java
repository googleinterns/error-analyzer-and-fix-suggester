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

package com.google.error_analyzer;

import com.google.error_analyzer.data.constant.StackTraceFormat;
import java.io.IOException;
import java.util.ArrayList;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.search.SearchHits;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Rule;
import org.junit.Test;

public final class StackFormatTest {

    @Test
    public void startsWithAtWithSpace() {
        String logText = " at com.stack.stacktrace.StackTraceExample.methodB";
        boolean actual = StackTraceFormat.matchesFormat(logText);
        boolean expected = true;
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void atPreceededBy20Characters() {
        String logText = "01234567890123456789 at com.stack.stacktrace.StackTraceExample.methodB";
        boolean actual = StackTraceFormat.matchesFormat(logText);
        boolean expected = true;
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void atInUpperCase() {
        String logText = "cvr123 At com.stack.stacktrace.StackTraceExample.methodB";
        boolean actual = StackTraceFormat.matchesFormat(logText);
        boolean expected = true;
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void atAppearsLaterInString() {
        String logText = "Running com.google.error_analyzer at maven";
        boolean actual = StackTraceFormat.matchesFormat(logText);
        boolean expected = false;
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void atPreceededBy21Characters() {
        String logText = "012345678901234567890 at com.stack.stacktrace.StackTraceExample.methodB";
        boolean actual = StackTraceFormat.matchesFormat(logText);
        boolean expected = false;
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void whenAtIsNotFollowedByAnything() {
        String logText = "01234567891234 at ";
        boolean actual = StackTraceFormat.matchesFormat(logText);
        boolean expected = false;
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void startsWithAtWithoutSpace() {
        String logText = "at com.stack.stacktrace.StackTraceExample.methodB";
        boolean actual = StackTraceFormat.matchesFormat(logText);
        boolean expected = false;
        Assert.assertEquals(actual, expected);
    }

    
}