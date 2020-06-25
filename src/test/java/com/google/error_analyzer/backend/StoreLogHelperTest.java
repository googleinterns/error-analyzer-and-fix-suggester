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
package com.google;

import com.google.error_analyzer.backend.StoreLogHelper;
import java.io.IOException;
import java.util.*;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpHost;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)

/*This class contains unit tests for all the methods of StoreLogHelper*/
public final class StoreLogHelperTest {

    private StoreLogHelper storeLogHelper;

    @Before
    public void setUp() throws Exception {
        storeLogHelper = new StoreLogHelper();
    }

    //unit test to check the conversion of log into json string
    @Test
    public void convertToJsonStringTest() throws IOException {
        String actual = storeLogHelper
            .convertToJsonString("Error1:\"index not found\"", 5);
        String expected = new String("{\"logLineNumber\":5,\"logText\":" +
            "\"Error1:\\\"index not found\\\"\"}");
        assertEquals(expected, actual);
    }

    // remove special characters from logText
    @Test
    public void RemoveSpecialCharactersTest() {
        String actual =
            storeLogHelper.cleanLogText("Error1:\"^&index not found?/,*\"");
        String expected = new String("Error1:\" index not found \"");
        assertEquals(expected, actual);
    }

}