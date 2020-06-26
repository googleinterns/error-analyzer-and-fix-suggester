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
import java.io.IOException;
import javax.servlet.http.*;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IndexNameTest {
    private Cookie cookie;

    @Mock
    HttpServletRequest request;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        request = Mockito.mock(HttpServletRequest.class);
        cookie = new Cookie(IndexName.SESSIONID, "abcd");
    }

    //append sessionID to fileName to get indexName
    @Test
    public void getIndexNameTest() {   
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        String fileName = "file1";
        String expected = "abcdfile1";
        String actual = IndexName.getIndexName(request,fileName);
        Assert.assertEquals(expected, actual);
    }

    //remove sessionID from indexName to get fileName
    @Test
    public void getFileNameTest() { 
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        String indexName = "abcdfile1";
        String expected = "file1";
        String actual = IndexName.getFileName(request,indexName);
        Assert.assertEquals(expected, actual);
    } 
}