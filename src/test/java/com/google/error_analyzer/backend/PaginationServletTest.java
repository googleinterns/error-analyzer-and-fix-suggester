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

import com.google.common.collect.ImmutableList;
import com.google.error_analyzer.backend.LogDao;
import com.google.error_analyzer.backend.LogDaoHelper;
import com.google.error_analyzer.backend.PaginationServlet;
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
        MockitoAnnotations.initMocks(this);
    }

    // access private method addFetchResultToData
    private Object getPrivateAddErrorFixesAndHighlights(
    String fileType, ImmutableList < String > hitIds,
    ImmutableList < String > hitFieldContent, HashMap<String,String>searches) 
    throws Exception {
        Method method = PaginationServlet.class.getDeclaredMethod(
        "addErrorFixesAndHighlights", new Class[] {
        String.class, ImmutableList.class, ImmutableList.class,
        HashMap.class});
        method.setAccessible(true);
        return method.invoke(pagination, fileType, hitIds, 
            hitFieldContent, searches);
    }

    // addFetchResultToData
    @Test
    public void addErrors() throws Exception {
        HashMap<String,String>searches=new HashMap();
        searches.put("1","searchError");
        ImmutableList<String> hitIds = ImmutableList.<String>builder() 
                                                    .add("1","2")
                                                    .build();
            
        ImmutableList<String> hitContent = ImmutableList.<String>builder() 
                                                    .add("error1","error2")
                                                    .build();
        String actual = (String)getPrivateAddErrorFixesAndHighlights(
        fileType1,hitIds,hitContent,searches);
        String expected = new String("[\"error2\",\"searchError\"]");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void addLogs() throws Exception {
        HashMap<String,String>searches=new HashMap();
        searches.put("1","searchError");
        ImmutableList<String> hitIds = ImmutableList.<String>builder() 
                                                    .add("1","2")
                                                    .build();
            
        ImmutableList<String> hitContent = ImmutableList.<String>builder() 
                                                    .add("log1","log2")
                                                    .build();
        String actual = (String)getPrivateAddErrorFixesAndHighlights(
        fileType2,hitIds,hitContent,searches);
        String expected = new String("[\"searchError\",\"log2\"]");
        Assert.assertEquals(expected, actual);
    }

    // test dopost
    @Test
    public void validFileName() throws Exception {
        ImmutableList<String> immutableListId = ImmutableList.of("2");	
        ImmutableList<String> immutableListContent = 	
            ImmutableList.of("log2");	
        when(logDao.fileExists(any(String.class))).thenReturn(true);
        when(logDaoHelper.hitId(any())).thenReturn(immutableListId);	
        when(logDaoHelper.hitFieldContent(any(),any())).	
            thenReturn(immutableListContent);
        when(request.getParameter("start")).thenReturn("1");
        when(request.getParameter("size")).thenReturn("1");
        when(request.getParameter("fileName")).thenReturn(fileName);
        when(request.getParameter("fileType")).thenReturn(fileType2);
        when(request.getParameter("searchString")).thenReturn("");
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        pagination.doPost(request, response);
        String actual = stringWriter.toString();
        String expected =  new String("[\"log2\"]\n");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void emptyFileName() throws Exception {
        when(request.getParameter("start")).thenReturn("1");
        when(request.getParameter("size")).thenReturn("1");
        when(request.getParameter("fileName")).thenReturn("");
        when(request.getParameter("fileType")).thenReturn(fileType2);
        when(request.getParameter("searchString")).thenReturn("");
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        pagination.doPost(request, response);
        String actual = stringWriter.toString();
        String expected =  new String("[]\n");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void noMatchingIndexForFileName() throws Exception {
        when(logDao.fileExists(any(String.class))).thenReturn(false);
        when(request.getParameter("start")).thenReturn("1");
        when(request.getParameter("size")).thenReturn("1");
        when(request.getParameter("fileName")).thenReturn(fileName);
        when(request.getParameter("fileType")).thenReturn(fileType2);
        when(request.getParameter("searchString")).thenReturn("");
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        pagination.doPost(request, response);
        String actual = stringWriter.toString();
        String expected =  new String("[]\n");
        Assert.assertEquals(expected, actual);
    }

}