package com.google.error_analyzer;

import com.google.error_analyzer.data.constant.LogFields;
import com.google.error_analyzer.servlets.StackTraceServlet;
import com.google.error_analyzer.backend.MockLogDao;
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Test;
import org.mockito.*;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
// import static org.mockito.Mockito.thenThrow;

public class StackTraceServletTest {
    private StackTraceServlet servlet;
    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        servlet = new StackTraceServlet();
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        servlet.stackTrace.logDao = new MockLogDao();

    }

    @Test
    public void servletTest() throws IOException {
        when(request.getParameter(LogFields.FILE_NAME)).thenReturn("file1");
        when(request.getParameter(LogFields.LOG_LINE_NUMBER)).thenReturn("1");
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        servlet.doPost(request, response);
        String actual = stringWriter.toString();
        String expected = "[\"No stack found for this error\"]\n";
        assertEquals(expected, actual);
    }

    @Test
    public void numberParseError() throws IOException {
        when(request.getParameter(LogFields.LOG_LINE_NUMBER)).thenReturn("a");
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        servlet.doPost(request, response);
        String actual = stringWriter.toString();
        String expected = "Could not parse logLineNumber java.lang.NumberFormatException: For input string: \"a\"\n";
        assertEquals(expected, actual);
    }

    @Test
    public void fileNameisNull() throws IOException {
        when(request.getParameter(LogFields.LOG_LINE_NUMBER)).thenReturn("1");
        when(request.getParameter(LogFields.FILE_NAME)).thenReturn(null);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        servlet.stackTrace.logDao = Mockito.mock(MockLogDao.class);
        when(response.getWriter()).thenReturn(writer);
        servlet.doPost(request, response);
        String actual = stringWriter.toString();
        System.out.println(actual);
        String expected = "Could not complete requestjava.lang.NullPointerException: index must not be null\n";
        assertEquals(expected, actual);
    }
}