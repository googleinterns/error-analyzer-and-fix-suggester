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

import com.google.error_analyzer.backend.LogDao;
import com.google.error_analyzer.backend.DocCountServlet;
import java.io.*;
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
public final class DocCountServletTest {

    private final String fileName = "file";
    
    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    LogDao logDao;

    @InjectMocks
    DocCountServlet docCount;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        docCount = new DocCountServlet();
        ReflectionTestUtils.setField(docCount, "logDao", logDao);
        MockitoAnnotations.initMocks(this);
    }

    // test dopost
    @Test
    public void returnCount() throws Exception {
        when(request.getParameter("index")).thenReturn(fileName);
        when(logDao.getDocCount(any(String.class))).thenReturn((long)5);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        docCount.doPost(request, response);
        String actual = stringWriter.toString();
        String expected =  new String("5\n");
        Assert.assertEquals(expected, actual);
    }
}