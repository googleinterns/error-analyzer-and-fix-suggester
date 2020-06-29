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

import com.google.error_analyzer.backend.FileAndUrlLogs;
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
    private FileAndUrlLogs fileAndUrlLogs;
    private static final String SESSIONID_VALUE = "abcd";

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
        fileAndUrlLogs = new FileAndUrlLogs();
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        cookie = new Cookie(IndexName.SESSIONID, SESSIONID_VALUE);
        MockitoAnnotations.initMocks(this);
        fileAndUrlLogs.storeLogs.logDao = new MockLogDao();
        servlet.fileAndUrlLogs.storeLogs.logDao = new MockLogDao();
    }

    //unit test for catch block of UrlServlet 
    @Test
    public void urlServletTestExceptionCase() throws ServletException,
     IOException {
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
        String nullPointerExceptionString = "java.lang.NullPointerException";
        String expected = String.format(fileAndUrlLogs.storeLogs
        .ERROR_TEMPLATE_RESPONSE, nullPointerExceptionString);
        assertTrue(actual.contains(expected));
    }


}