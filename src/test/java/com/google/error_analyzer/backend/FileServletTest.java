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

import com.google.error_analyzer.backend.IndexName;
import com.google.error_analyzer.backend.MockLogDao;
import com.google.error_analyzer.data.constant.LogFields;
import com.google.error_analyzer.data.constant.PageConstants;
import com.google.error_analyzer.servlets.FileServlet;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.annotation.WebServlet;
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

//unit tests for FileServlet
public class FileServletTest {
    private Cookie cookie;
    private static final String SESSIONID_VALUE = "abcd";
    private static final String FILE_CONTENT =
        "error1\nerror2\nerror3\n";
    private static final String EMPTY_FILE_CONTENT = "";

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @InjectMocks
    FileServlet servlet;

    @Mock
    Part filePart;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        servlet = new FileServlet();
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        filePart = Mockito.mock(Part.class);
        cookie = new Cookie(IndexName.SESSIONID, SESSIONID_VALUE);
        MockitoAnnotations.initMocks(this);
        servlet.fileAndUrlLogs.storeLogs.logDao = new MockLogDao();
    }

    //store the log into the database when index with name same as the
    //file name does not exist in the database and then trying to
    //store another file with same fileName
    @Test
    public void doPost_alreadyExistingFiles() 
    throws ServletException, IOException {
        String fileName1 = "file1";
        when(request.getParameter(LogFields.FILE_NAME)).thenReturn(fileName1);
        when(request.getPart(LogFields.FILE)).thenReturn(filePart);
        InputStream inputStream =
             new ByteArrayInputStream(FILE_CONTENT.getBytes());
        when(filePart.getInputStream()).thenReturn(inputStream);
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
        String expected = String.format(servlet.fileAndUrlLogs.storeLogs
            .FILE_STORED_TEMPLATE_RESPONSE, fileName1);
        assertTrue(actual.contains(expected));
        inputStream = new ByteArrayInputStream(FILE_CONTENT.getBytes());
        when(filePart.getInputStream()).thenReturn(inputStream);
        servlet.doPost(request, response);
        actual = stringWriter.toString();
        expected = String.format(servlet.fileAndUrlLogs
            .storeLogs.FILE_STORED_TEMPLATE_RESPONSE, fileName1 + "(1)");
        assertTrue(actual.contains(expected));

    }

    /*Storing empty file to the database*/
    @Test
    public void doPost_EmptyFileCase()
    throws ServletException, IOException {
        String fileName1 = "file1";
        when(request.getParameter(LogFields.FILE_NAME)).thenReturn(fileName1);
        when(request.getPart(LogFields.FILE)).thenReturn(filePart);
        InputStream inputStream =
            new ByteArrayInputStream(EMPTY_FILE_CONTENT.getBytes());
        when(filePart.getInputStream()).thenReturn(inputStream);
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
        String expected = String.format(
           servlet.fileAndUrlLogs.storeLogs.FILE_EMPTY_TEMPLATE_RESPONSE, fileName1);
        assertTrue(actual.contains(expected));    
    }
    
}