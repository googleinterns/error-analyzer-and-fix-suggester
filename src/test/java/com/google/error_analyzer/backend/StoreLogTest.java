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

import com.google.error_analyzer.backend.IndexName;
import com.google.error_analyzer.backend.LogDao;
import com.google.error_analyzer.backend.MockLogDao;
import com.google.error_analyzer.backend.StoreLogs;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import javax.servlet.http.*;
import org.apache.commons.codec.DecoderException;
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

/*this class contains tests for the methods used for 
storing logs to the database*/
public final class StoreLogTest {
    private StoreLogs storeLogs;
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
        storeLogs = new StoreLogs();
        storeLogs.logDao = new MockLogDao();
        storeLogs.MaxLogLines = 5;
        request = Mockito.mock(HttpServletRequest.class);
        cookie = new Cookie(IndexName.SESSIONID, SESSIONID_VALUE);    
    }

    //store the log into the database when index with name same as the
    //file name does not exist in the database and then trying to
    //store another file with same fileName
    @Test
    public void checkAndStoreLog_alreadyExistingFile()
    throws IOException {
        String fileName = "samplefile1";
        when(request.getCookies()).thenReturn(new Cookie[] {cookie});
        InputStream inputStream =
            new ByteArrayInputStream(FILE_CONTENT.getBytes());
        boolean isUrl = false;
        String expected = String.format(
            storeLogs.FILE_STORED_TEMPLATE_RESPONSE, fileName);
        String actual = storeLogs.checkAndStoreLog(
            request, fileName, inputStream, isUrl);
        assertEquals(expected, actual);
        inputStream =
            new ByteArrayInputStream(FILE_CONTENT.getBytes());
        expected = String.format(
            storeLogs.FILE_STORED_TEMPLATE_RESPONSE, fileName + "(1)");
        actual = storeLogs.checkAndStoreLog(request, fileName,  inputStream, isUrl);
        assertEquals(expected, actual);
    }

    /*unit test for the catch block of checkAndStoreLogs */
    @Test
    public void checkAndStoreLog_logExceptionCase() throws IOException{
        String fileName = "file1";
        when(request.getCookies()).thenThrow(NullPointerException.class);
        InputStream inputStream =
            new ByteArrayInputStream(FILE_CONTENT.getBytes());
        boolean isUrl = false;
        String actual = storeLogs
            .checkAndStoreLog(request, fileName, inputStream, isUrl);
        String nullPointerExceptionString = "java.lang.NullPointerException";
        String expected = String.format(
            storeLogs.ERROR_TEMPLATE_RESPONSE, nullPointerExceptionString);
        Assert.assertEquals(expected, actual);
    }

    //check the creation of errorIndex
    @Test
    public void checkAndStoreLog_creationOfErrorIndex() throws IOException {
        String fileName = "file1";
        when(request.getCookies()).thenReturn(new Cookie[] {cookie});
        boolean isUrl = false;
        InputStream inputStream =
            new ByteArrayInputStream(FILE_CONTENT.getBytes());
        storeLogs.checkAndStoreLog(request, fileName, inputStream, isUrl);
        String errorIndexName = "6162636466696c6531error";
        boolean actual = storeLogs.logDao.fileExists(errorIndexName);
        boolean expected = true;
        Assert.assertEquals(expected, actual);
    }

    /*store the file logs or plain text maximum 5 lines in a single API call*/
    @Test
    public void storeLogsInBatches_forFilesAndPlainText() throws IOException,
    DecoderException, UnsupportedEncodingException  {
        String indexName = "66696c6531";
        InputStream inputStream =
            new ByteArrayInputStream(FILE_CONTENT.getBytes());
        when(request.getCookies()).thenReturn(new Cookie[] {cookie});
        boolean isUrl = false;
        storeLogs.storeLogsInBatches(
            request, indexName, inputStream, isUrl);
        for (int id = 1; id < 7; id++) {
            String actual = storeLogs.logDao
                .getJsonStringById(indexName, Integer.toString(id));
            String expected = String.format(
                "{\"logLineNumber\":%1$s,\"logText\":\"error%1$s\"}", id);
            assertEquals(expected, actual);
        }
        String actual = storeLogs.logDao
            .getJsonStringById(indexName, "7");
        String expected = null;
        assertEquals(expected, actual);
    }

    /*store the url logs maximum 5 lines in a single API call*/
    @Test
    public void storeLogsInBatches_forUrl() throws  IOException,
    DecoderException, UnsupportedEncodingException  {
        String indexName = "66696c6531";
        InputStream inputStream =
            new ByteArrayInputStream(URL_CONTENT.getBytes());
        when(request.getCookies()).thenReturn(new Cookie[] {cookie});
        boolean isUrl = true;
        storeLogs.storeLogsInBatches(
            request, indexName, inputStream, isUrl);
        for (int id = 1; id < 6; id++) {
            String actual = storeLogs.logDao
                .getJsonStringById(indexName, Integer.toString(id));
            String expected = String.format(
                "{\"logLineNumber\":%1$s,\"logText\":\"error%1$s\"}", id);
            assertEquals(expected, actual);
        }
        String actual = storeLogs.logDao
            .getJsonStringById(indexName, "6");
        String expected = null;
        assertEquals(expected, actual);   
    }


    /*unit test for storeLog method when offset is 0*/
    @Test
    public void storeLog_zeroOffset () throws IOException,
    DecoderException, UnsupportedEncodingException {
        String log = "error1\nerror2\nerror3";
        String indexName = "66696c6531";
        int offset = 0;
        when(request.getCookies()).thenReturn(new Cookie[] {cookie});
        storeLogs.storeLog(request, indexName, log, offset);
        String actual = storeLogs.logDao.getJsonStringById(indexName, "1");
        String expected = "{\"logLineNumber\":1,\"logText\":\"error1\"}";
        assertEquals(expected, actual);
        actual = storeLogs.logDao.getJsonStringById(indexName, "2");
        expected = "{\"logLineNumber\":2,\"logText\":\"error2\"}";
        assertEquals(expected, actual);
        actual = storeLogs.logDao.getJsonStringById(indexName, "3");
        expected = "{\"logLineNumber\":3,\"logText\":\"error3\"}";
        assertEquals(expected, actual);
        actual = storeLogs.logDao.getJsonStringById(indexName, "4");
        expected = null;
        assertEquals(expected, actual);
    }

    /*unit test for storeLog method when offset is 3*/
    @Test
    public void storeLog_nonZeroOffset () throws IOException ,
    DecoderException, UnsupportedEncodingException  {
        String log = "error1\nerror2\nerror3";
        String indexName = "66696c6531";
        int offset = 3;
        when(request.getCookies()).thenReturn(new Cookie[] {cookie});
        storeLogs.storeLog(request, indexName, log, offset);
        String actual = storeLogs.logDao.getJsonStringById(indexName, "4");
        String expected = "{\"logLineNumber\":4,\"logText\":\"error1\"}";
        assertEquals(expected, actual);
        actual = storeLogs.logDao.getJsonStringById(indexName, "5");
        expected = "{\"logLineNumber\":5,\"logText\":\"error2\"}";
        assertEquals(expected, actual);
        actual = storeLogs.logDao.getJsonStringById(indexName, "6");
        expected = "{\"logLineNumber\":6,\"logText\":\"error3\"}";
        assertEquals(expected, actual);
        actual = storeLogs.logDao.getJsonStringById(indexName, "1");
        expected = null;
        assertEquals(expected, actual);
    }

}