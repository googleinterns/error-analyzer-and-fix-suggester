package com.google.error_analyzer;

import com.google.error_analyzer.backend.MockLogDao;
import com.google.error_analyzer.data.constant.LogFields;
import com.google.error_analyzer.data.constant.PageConstants;
import com.google.error_analyzer.servlets.TextServlet;
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.http.HttpHost;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//unit tests for TextServlet
public class TextServletTest {

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    Cookie mockCookie;

    @InjectMocks
    TextServlet servlet;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        servlet = new TextServlet();
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        mockCookie = Mockito.mock(Cookie.class);
        MockitoAnnotations.initMocks(this);
        servlet.storeLog.logDao = new MockLogDao();

    }

    //store the log into the database when index with name same as the
    //file name does not exist in the db and then trying to store another 
    //file with different file name
    @Test
    public void servletTest() throws ServletException, IOException {
        when(request.getParameter(LogFields.FILE_NAME)).thenReturn("file1");
        when(request.getParameter(LogFields.LOG)).thenReturn("error");
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        RequestDispatcher requestDispatcher =
            Mockito.mock(RequestDispatcher.class);
        when(request.getRequestDispatcher(PageConstants.LANDING_PAGE))
            .thenReturn(requestDispatcher);
        when(mockCookie.getValue()).thenReturn("abcd");
        when(mockCookie.getName()).thenReturn("JSESSIONID");
        when(request.getCookies()).thenReturn(new Cookie[] {mockCookie});
        servlet.doPost(request, response);
        String actual = stringWriter.toString();
        String expected = String.format(
            servlet.storeLog.FILE_STORED_TEMPLATE_RESPONSE, "file1");
        assertTrue(actual.contains(expected));
        when(request.getParameter(LogFields.FILE_NAME)).thenReturn("file2");
        servlet.doPost(request, response);
        actual = stringWriter.toString();
        expected = String.format(
            servlet.storeLog.FILE_STORED_TEMPLATE_RESPONSE, "file2");;
        assertTrue(actual.contains(expected));

    }

    //store the log into the database when index with name same as the
    //file name does not exist in the database and then trying to
    //store another file with same fileName
    @Test
    public void servletTestWhenFileAlreadyExists()
    throws ServletException, IOException {
        when(request.getParameter(LogFields.FILE_NAME)).thenReturn("file1");
        when(request.getParameter(LogFields.LOG)).thenReturn("error");
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        RequestDispatcher requestDispatcher =
            Mockito.mock(RequestDispatcher.class);
        when(request.getRequestDispatcher(PageConstants.LANDING_PAGE))
            .thenReturn(requestDispatcher);
        when(mockCookie.getValue()).thenReturn("abcd");
        when(mockCookie.getName()).thenReturn("JSESSIONID");
        when(request.getCookies()).thenReturn(new Cookie[] {mockCookie});
        servlet.doPost(request, response);
        String actual = stringWriter.toString();
        String expected = String.format(
            servlet.storeLog.FILE_STORED_TEMPLATE_RESPONSE, "file1");
        assertTrue(actual.contains(expected));
        servlet.doPost(request, response);
        actual = stringWriter.toString();
        expected = String.format(
            servlet.storeLog.FILE_STORED_TEMPLATE_RESPONSE, "file1(1)");
        assertTrue(actual.contains(expected));

    }
}