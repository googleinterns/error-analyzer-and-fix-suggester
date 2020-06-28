// Copyright 2020 Google LLC
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// https://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.error_analyzer;

import com.google.error_analyzer.backend.FileLogs;
import com.google.error_analyzer.backend.IndexName;
import com.google.error_analyzer.backend.MockLogDao;
import com.google.error_analyzer.data.constant.LogFields;
import com.google.error_analyzer.data.constant.PageConstants;
import com.google.error_analyzer.servlets.UrlServlet;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import org.apache.http.HttpHost;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*this class contains the unit test for the methods used in 
UrlServlet*/
public class UrlServletTest {
    private Cookie cookie;
    private FileLogs fileLogs;
    private static final String SESSIONID_VALUE = "abcd";
    private static final String URL_CONTENT =
        "<html>error1\nerror2\nerror3\nerror4\nerror5\n<html>";

    @Mock
    HttpServletRequest request;
 
    @Mock
    HttpServletResponse response;
 
    @InjectMocks
    UrlServlet servlet;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        fileLogs = new FileLogs();
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        cookie = new Cookie(IndexName.SESSIONID, SESSIONID_VALUE);
        MockitoAnnotations.initMocks(this);
        fileLogs.storeLogs.logDao = new MockLogDao();
        servlet.fileLogs.storeLogs.logDao = new MockLogDao();
    }

    /*store the url content using storeFileLogs method*/
    @Test
    public void urlServletTest() throws ServletException, IOException {
        String fileName = "file1";
        InputStream inputStream =
            new ByteArrayInputStream(URL_CONTENT.getBytes());
        when(request.getCookies()).thenReturn(new Cookie[] {cookie});
        boolean isUrl = true;
        fileLogs.storeFileLogs(request, fileName, inputStream, isUrl);
        for (int id = 1; id < 6; id++) {
            String actual = fileLogs.storeLogs.logDao
                .getJsonStringById(fileName, Integer.toString(id));
            String expected = String.format(
                "{\"logLineNumber\":%1$s,\"logText\":\"error%1$s\"}", id);
            assertEquals(expected, actual);
        }
        String actual = fileLogs.storeLogs.logDao
            .getJsonStringById(fileName, "6");
        String expected = null;
        assertEquals(expected, actual);   
    }

    //unit test for catch block of UrlServlet 
    @Test
    public void urlServletTestExceptionCase() throws ServletException, IOException {
        when(request.getParameter(LogFields.URL))
            .thenThrow(NullPointerException.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        RequestDispatcher requestDispatcher =
            Mockito.mock(RequestDispatcher.class);
        when(request.getRequestDispatcher(PageConstants.LANDING_PAGE))
            .thenReturn(requestDispatcher);
        when(request.getCookies()).thenReturn(new Cookie[] {cookie});
        servlet.doPost(request, response);
        String actual = stringWriter.toString();
        System.out.println(actual);
        String nullPointerExceptionString = "java.lang.NullPointerException";
        String expected = String.format(fileLogs.storeLogs.ERROR_TEMPLATE_RESPONSE, 
            nullPointerExceptionString);
        assertTrue(actual.contains(expected));
    }


}