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

import com.google.error_analyzer.backend.FileLogs;
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

/*this class contains tests for the methods of FileLogs*/
public final class FileLogTest {
    private FileLogs fileLogs;
    private Cookie cookie;
    private static final String SESSIONID_VALUE = "abcd";
    private static final String FILE_CONTENT =
        "error1\nerror2\nerror3\nerror4\nerror5\nerror6\n";

    @Mock
    HttpServletRequest request;

    @Before
    public void setUp() {
        fileLogs = new FileLogs();
        fileLogs.storeLogs.logDao = new MockLogDao();
        fileLogs.MaxLogLines = 5;
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
        String actual = fileLogs.checkAndStoreFileLog(request, fileName, inputStream);
        System.out.println(actual);
        String nullPointerExceptionString = "java.lang.NullPointerException";
        String expected = String.format(
            fileLogs.storeLogs.ERROR_TEMPLATE_RESPONSE, nullPointerExceptionString);
        Assert.assertEquals(expected, actual);
    }

    /*storing maximum 5 lines in a single API call*/
    @Test
    public void storeFileLogsTest() throws IOException {
        String fileName = "file1";
        InputStream inputStream =
            new ByteArrayInputStream(FILE_CONTENT.getBytes());
        when(request.getCookies()).thenReturn(new Cookie[] {
            cookie
        });
        fileLogs.storeFileLogs(request, fileName, inputStream);
        String actual =
            fileLogs.storeLogs.logDao.getJsonStringById(fileName, "1");
        String expected = "{\"logLineNumber\":1,\"logText\":\"error1\"}";
        assertEquals(expected, actual);
        actual = fileLogs.storeLogs.logDao.getJsonStringById(fileName, "2");
        expected = "{\"logLineNumber\":2,\"logText\":\"error2\"}";
        assertEquals(expected, actual);
        actual = fileLogs.storeLogs.logDao.getJsonStringById(fileName, "3");
        expected = "{\"logLineNumber\":3,\"logText\":\"error3\"}";
        assertEquals(expected, actual);
        actual = fileLogs.storeLogs.logDao.getJsonStringById(fileName, "4");
        expected = "{\"logLineNumber\":4,\"logText\":\"error4\"}";
        assertEquals(expected, actual);
        actual = fileLogs.storeLogs.logDao.getJsonStringById(fileName, "5");
        expected = "{\"logLineNumber\":5,\"logText\":\"error5\"}";
        assertEquals(expected, actual);
        actual = fileLogs.storeLogs.logDao.getJsonStringById(fileName, "6");
        expected = "{\"logLineNumber\":6,\"logText\":\"error6\"}";
        assertEquals(expected, actual);
        actual = fileLogs.storeLogs.logDao.getJsonStringById(fileName, "7");
        expected = null;
        assertEquals(expected, actual);
    }

}