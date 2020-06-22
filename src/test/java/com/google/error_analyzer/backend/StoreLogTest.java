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

import com.google.error_analyzer.backend.LogDao;
import com.google.error_analyzer.backend.MockLogDao;
import com.google.error_analyzer.backend.StoreLogs;
import java.io.IOException;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)

/*this class contains tests for the methods used for 
storing logs to the database*/
public final class StoreLogTest {

    @Mock
    MockLogDao mockLogDao;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @InjectMocks
    StoreLogs storeLogs;

    @Before
    public void setUp() {
        mockLogDao = new MockLogDao();
        storeLogs = new StoreLogs();
        storeLogs.logDao = mockLogDao;
    }

    //store the log into the database when index with name same as the
    //file name does not exist in the db
    @Test
    public void checkAndStoreLogTest() throws IOException {
        String log = "error1";
        String fileName = "samplefile";
        String expected = "\t\t\t<h2> File Stored</h2>";
        String actual = storeLogs.checkAndStoreLog(fileName, log);
        assertEquals(expected, actual);

    }

    //store the log into the database when index with name same as the
    //file name already exist in the database
    @Test
    public void checkAndStoreLogInAlreadyExistingFileTest()
    throws IOException {
        String log = "error2";
        String fileName = "samplefile1";
        storeLogs.checkAndStoreLog(fileName, log);
        String expected = "\t\t\t<h2> Sorry! the file already exists</h2>";
        String actual = storeLogs.checkAndStoreLog(fileName, log);
        assertEquals(expected, actual);

    }

    //convert loglines to json Strings
    @Test
    public void convertToJsonStringTest() {
        String actual = storeLogs.
        convertToJsonString("Error1:\"index not found\"", 5);
        String expected = new String("{\"logLineNumber\":5," +
            "\"logText\":\"Error1:\\\"index not found\\\"\"}");
        assertEquals(expected, actual);
    }

    //test the removal of special characters from json string
    @Test
    public void RemoveSpecialCharactersTest() {
        String actual = storeLogs.
        convertToJsonString("Error1:\"^&index not found?/,*\"", 5);
        String expected = new String("{\"logLineNumber\":5," +
            "\"logText\":\"Error1:\\\" index not found \\\"\"}");
        assertEquals(expected, actual);
    }

    //test for storeLog method
    @Test
    public void storeLogTest() throws IOException {
        String log = "error2";
        String fileName = "samplefile2";
        String actual = storeLogs.storeLog(fileName, log);
        String expected = "\t\t\t<h2> File Stored</h2>";
        assertEquals(expected, actual);
    }

}