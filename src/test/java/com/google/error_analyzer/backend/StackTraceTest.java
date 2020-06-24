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

import com.google.error_analyzer.backend.StackTrace;
import com.google.error_analyzer.backend.MockLogDao;
import java.io.IOException;
import java.util.ArrayList;
import org.elasticsearch.search.SearchHit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.Mock;
import org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// @RunWith(MockitoJUnitRunner.class)
public final class StackTraceTest {
    private final String fileName = "file"; 
    private StackTrace stackTrace;

    @Before
    public void setUp() {
        stackTrace = new StackTrace();
        stackTrace.logDao = new MockLogDao();
    }

    @Test
    public void whenThereAreNoHitsFromRangeQuery() throws IOException {
        ArrayList<String> stackList = stackTrace.findStack(1, fileName);
        Assert.assertEquals(stackList.size(), 1);
        Assert.assertEquals(stackList.get(0), "No stack found for this error");
    }

}