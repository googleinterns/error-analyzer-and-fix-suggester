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
package com.google.error_analyzer.servlets;

import com.google.common.collect.ImmutableList;
import com.google.error_analyzer.backend.LogDao;
import com.google.error_analyzer.backend.LogDaoHelper;
import com.google.error_analyzer.data.constant.LogFields;
import com.google.error_analyzer.data.Document;
import com.google.error_analyzer.data.ErrorFixes;
import com.google.error_analyzer.servlets.PaginationServlet;
import com.google.gson.Gson;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runners.JUnit4;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mockito.ArgumentMatchers;	
import org.mockito.InjectMocks;	
import org.mockito.junit.MockitoJUnit;	
import org.mockito.junit.MockitoRule;	
import org.mockito.Mock;	
import org.mockito.Mockito.*;	
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.BDDMockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;	


@RunWith(MockitoJUnitRunner.class)
public final class PaginationServletTest {

    private final String fileName = "file";
    private final String fileType1 = "errors";
    private final String fileType2 = "logs";
    private final int page1 = 1;
    private final int page2 = 2;

    
    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    LogDao logDao;

    @Mock
    LogDaoHelper logDaoHelper;

    @Mock
    ErrorFixes errorFixes;

    @InjectMocks
    PaginationServlet pagination;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        pagination = new PaginationServlet();
        ReflectionTestUtils.setField(pagination, "logDao", logDao);
        ReflectionTestUtils.setField(pagination, "logDaoHelper", 	
        logDaoHelper);
        ReflectionTestUtils.setField(pagination, "errorFixes", errorFixes);
        MockitoAnnotations.initMocks(this);
    }

    // test dopost
    @Test
    public void doPost_validLogFileName() throws Exception {	
        ImmutableList<String> logText = ImmutableList.<String>builder() 
                                                    .add("log2")
                                                    .build();	
        ImmutableList<String> logLineNo = ImmutableList.<String>builder() 
                                                    .add("2")
                                                    .build();	
        when(logDao.fileExists(any(String.class))).thenReturn(true);	
        when(logDaoHelper.hitFieldContent(any(),eq(LogFields.LOG_TEXT))).	
            thenReturn(logText);
        when(logDaoHelper.hitFieldContent(any(),eq(LogFields.LOG_LINE_NUMBER))).	
            thenReturn(logLineNo);
        when(request.getParameter(LogFields.START)).thenReturn("1");
        when(request.getParameter(LogFields.SIZE)).thenReturn("1");
        when(request.getParameter(LogFields.FILE_NAME)).thenReturn(fileName);
        when(request.getParameter(LogFields.FILE_TYPE)).thenReturn(fileType2);
        when(request.getParameter(LogFields.SEARCH_STRING)).thenReturn("");
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        pagination.doPost(request, response);
        String actual = stringWriter.toString();
        ArrayList < Document > data = new ArrayList();
        data.add(new Document("",2,"log2"));
        String expected = convertToJson(data)+"\n";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void doPost_validErrorFileName() throws Exception {	
        ImmutableList<String> logText = ImmutableList.<String>builder() 
                                                    .add("error1","error2")
                                                    .build();	
        ImmutableList<String> logLineNo = ImmutableList.<String>builder() 
                                                    .add("1","2")
                                                    .build();	
        when(errorFixes.findFixes(any(String.class))).thenReturn("");                                         
        when(logDao.fileExists(any(String.class))).thenReturn(true);	
        when(logDaoHelper.hitFieldContent(any(),eq(LogFields.LOG_TEXT))).	
            thenReturn(logText);
        when(logDaoHelper.hitFieldContent(any(),eq(LogFields.LOG_LINE_NUMBER))).	
            thenReturn(logLineNo);
        when(request.getParameter(LogFields.START)).thenReturn("1");
        when(request.getParameter(LogFields.SIZE)).thenReturn("1");
        when(request.getParameter(LogFields.FILE_NAME)).thenReturn(fileName);
        when(request.getParameter(LogFields.FILE_TYPE)).thenReturn(fileType1);
        when(request.getParameter(LogFields.SEARCH_STRING)).thenReturn("");
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        pagination.doPost(request, response);
        String actual = stringWriter.toString();
        ArrayList < Document > data = new ArrayList();
        Document document1 =new Document("",2,"error2 ");
        Document document2 =new Document("",1,"error1 ");
        data.add(document1);
        data.add(document2);
        String expected = convertToJson(data)+"\n";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void doPost_emptyFileName() throws Exception {
        when(request.getParameter(LogFields.START)).thenReturn("1");
        when(request.getParameter(LogFields.SIZE)).thenReturn("1");
        when(request.getParameter(LogFields.FILE_NAME)).thenReturn("");
        when(request.getParameter(LogFields.FILE_TYPE)).thenReturn(fileType2);
        when(request.getParameter(LogFields.SEARCH_STRING)).thenReturn("");
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        pagination.doPost(request, response);
        String actual = stringWriter.toString();
        String expected = convertToJson(new ArrayList<Document>())+"\n";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void doPost_noMatchingIndexForFileName() throws Exception {
        when(logDao.fileExists(any(String.class))).thenReturn(false);
        when(request.getParameter(LogFields.START)).thenReturn("1");
        when(request.getParameter(LogFields.SIZE)).thenReturn("1");
        when(request.getParameter(LogFields.FILE_NAME)).thenReturn(fileName);
        when(request.getParameter(LogFields.FILE_TYPE)).thenReturn(fileType2);
        when(request.getParameter(LogFields.SEARCH_STRING)).thenReturn("");
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        pagination.doPost(request, response);
        String actual = stringWriter.toString();
        String expected = convertToJson(new ArrayList<Document>())+"\n";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void doPost_searchStringNotEmpty() throws Exception {
        ImmutableList<String> searchResult = 	
            ImmutableList.of("Google Intern");
        ImmutableList<String> logLineNo = ImmutableList.of("7");
        when(logDao.fileExists(any(String.class))).thenReturn(true);	
        when(logDaoHelper.getHighLightedText(any(),eq(LogFields.LOG_TEXT))).	
            thenReturn(searchResult);
        when(logDaoHelper.hitFieldContent(any(),eq(LogFields.LOG_LINE_NUMBER))).	
            thenReturn(logLineNo);
        when(request.getParameter(LogFields.START)).thenReturn("1");
        when(request.getParameter(LogFields.SIZE)).thenReturn("1");
        when(request.getParameter(LogFields.FILE_NAME)).thenReturn(fileName);
        when(request.getParameter(LogFields.FILE_TYPE)).thenReturn(fileType2);
        when(request.getParameter(LogFields.SEARCH_STRING)).thenReturn("Intern");
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        pagination.doPost(request, response);
        String actual = stringWriter.toString();
        ArrayList < Document > data = new ArrayList();
        data.add(new Document("",7,"Google Intern"));
        String expected = convertToJson(data)+"\n";
        Assert.assertEquals(expected, actual);
    }

    private String convertToJson(ArrayList < Document > data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return json;
    }
}