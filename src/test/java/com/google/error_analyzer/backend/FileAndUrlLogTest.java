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

import com.google.error_analyzer.backend.FileAndUrlLogs;
import com.google.error_analyzer.backend.IndexName;
import com.google.error_analyzer.backend.LogDao;
import com.google.error_analyzer.backend.MockLogDao;
import com.google.error_analyzer.backend.StoreLogs;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)

/*this class contains tests for the methods of FileAndUrlLogs*/
public final class FileAndUrlLogTest {
    private FileAndUrlLogs fileAndUrlLogs;
    private Cookie cookie;
    private static final String SESSIONID_VALUE = "abcd";
    private static final String FILE_CONTENT =
        "error1\nerror2\nerror3\nerror4\nerror5\nerror6\n";
    private static final String URL_CONTENT =
        "<html>error1\nerror2\nerror3\nerror4\nerror5\n<html>";

    @Mock
    HttpServletRequest request;

    @Before
    public void setUp() {
        fileAndUrlLogs = new FileAndUrlLogs();
        fileAndUrlLogs.storeLogs.logDao = new MockLogDao();
        fileAndUrlLogs.MaxLogLines = 5;
        request = Mockito.mock(HttpServletRequest.class);
        cookie = new Cookie(IndexName.SESSIONID, SESSIONID_VALUE);
    }

    /*unit test for the catch block of checkAndStoreFileLogs */
    @Test
    public void checkAndStoreFileLogExceptionCase() {
        String fileName = "file1";
        when(request.getCookies()).thenThrow(NullPointerException.class);
        InputStream inputStream =
            new ByteArrayInputStream(FILE_CONTENT.getBytes());
        boolean isUrl = false;
        String actual = fileAndUrlLogs
            .checkAndStoreFileAndUrlLog(request, fileName, inputStream, isUrl);
        System.out.println(actual);
        String nullPointerExceptionString = "java.lang.NullPointerException";
        String expected = String.format(fileAndUrlLogs
            .storeLogs.ERROR_TEMPLATE_RESPONSE, nullPointerExceptionString);
        Assert.assertEquals(expected, actual);
    }

    /*store the url logs maximum 5 lines in a single API call*/
    @Test
    public void toreFileAndUrlLogsForFile() throws IOException {
        String fileName = "file1";
        InputStream inputStream =
            new ByteArrayInputStream(FILE_CONTENT.getBytes());
        when(request.getCookies()).thenReturn(new Cookie[] {cookie});
        boolean isUrl = false;
        fileAndUrlLogs.storeFileAndUrlLogs(
            request, fileName, inputStream, isUrl);
        for (int id = 1; id < 7; id++) {
            String actual = fileAndUrlLogs.storeLogs.logDao
                .getJsonStringById(fileName, Integer.toString(id));
            String expected = String.format(
                "{\"logLineNumber\":%1$s,\"logText\":\"error%1$s\"}", id);
            assertEquals(expected, actual);
        }
        String actual = fileAndUrlLogs.storeLogs.logDao
            .getJsonStringById(fileName, "7");
        String expected = null;
        assertEquals(expected, actual);
    }

    /*store the url logs maximum 5 lines in a single API call*/
    @Test
    public void storeFileAndUrlLogsForUrl() throws  IOException {
        String fileName = "file1";
        InputStream inputStream =
            new ByteArrayInputStream(URL_CONTENT.getBytes());
        when(request.getCookies()).thenReturn(new Cookie[] {cookie});
        boolean isUrl = true;
        fileAndUrlLogs.storeFileAndUrlLogs(
            request, fileName, inputStream, isUrl);
        for (int id = 1; id < 6; id++) {
            String actual = fileAndUrlLogs.storeLogs.logDao
                .getJsonStringById(fileName, Integer.toString(id));
            String expected = String.format(
                "{\"logLineNumber\":%1$s,\"logText\":\"error%1$s\"}", id);
            assertEquals(expected, actual);
        }
        String actual = fileAndUrlLogs.storeLogs.logDao
            .getJsonStringById(fileName, "6");
        String expected = null;
        assertEquals(expected, actual);   
    }

}

