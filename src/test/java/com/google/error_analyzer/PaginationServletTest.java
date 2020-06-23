
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
import com.google.error_analyzer.data.ErrorFixes;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
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
import org.mockito.quality.Strictness;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.BDDMockito.any;


@RunWith(MockitoJUnitRunner.class)
public final class PaginationServletTest {

    private String fileName = "file";
    private String fileType1 = "errors";
    private String filetType2 = "logs";
    private int page1 = 1;
    private int page2 = 2;

    PaginationServlet pagination;

    @Mock 
    ErrorFixes errorFix;

    @Before
    public void setUp() {
        pagination = new PaginationServlet();
        ReflectionTestUtils.setField(pagination, "errorFix", 
        errorFix);
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
    public void addresult() throws Exception {
        HashMap<String,String>searches=new HashMap();
        searches.put("1","searchError");
        when(errorFix.findFixes(any(String.class))).thenReturn(new String());
        ImmutableList<String> hitIds = ImmutableList.<String>builder() 
                                                    .add("1","2")
                                                    .build();
            
        ImmutableList<String> hitContent = ImmutableList.<String>builder() 
                                                    .add("error1","error2")
                                                    .build();
        String actual = (String)getPrivateAddErrorFixesAndHighlights(
        fileType1,hitIds,hitContent,searches);
        String expected = new String("[\"error2 \",\"searchError \"]");
        Assert.assertEquals(expected, actual);
    }
}