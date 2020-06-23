package com.google.error_analyzer;

import com.google.error_analyzer.backend.MockLogDao;
import com.google.error_analyzer.servlets.TextServlet;
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.http.HttpHost;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
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

    @InjectMocks
    TextServlet servlet;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        servlet = new TextServlet();
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        MockitoAnnotations.initMocks(this);
        servlet.storeLog.logDao = new MockLogDao();

    }

    //store the log into the database when index with name same as the
    //file name does not exist in the db
    @Test
    public void servletTest() throws ServletException, IOException {
        when(request.getParameter("filename")).thenReturn("file1");
        when(request.getParameter("Log")).thenReturn("error");
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        RequestDispatcher requestDispatcher =
            Mockito.mock(RequestDispatcher.class);
        when(request.getRequestDispatcher("/index.html"))
            .thenReturn(requestDispatcher);
        servlet.doPost(request, response);
        String actual = stringWriter.toString();
        String expected = servlet.storeLog.FILE_STORED_RESPONSE;
        assertTrue(actual.contains(expected));

    }

    //store the log into the database when index with name same as the
    //file name already exist in the database
    @Test
    public void servletTestWhenFileAlreadyExists()
    throws ServletException, IOException {
        when(request.getParameter("filename")).thenReturn("file1");
        when(request.getParameter("Log")).thenReturn("error");
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        RequestDispatcher requestDispatcher =
            Mockito.mock(RequestDispatcher.class);
        when(request.getRequestDispatcher("/index.html"))
            .thenReturn(requestDispatcher);
        servlet.doPost(request, response);
        servlet.doPost(request, response);
        String actual = stringWriter.toString();
        System.out.println(actual);
        String expected = servlet.storeLog.FILE_NOT_STORED_RESPONSE;
        assertTrue(actual.contains(expected));

    }
}