
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
import com.google.error_analyzer.backend.Pagination;
import com.google.error_analyzer.data.SearchErrors;
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
public final class PaginationTest {

    private String fileName = "file";
    private String fileType1 = "errors";
    private String filetType2 = "logs";
    private int page1 = 1;
    private int page2 = 2;

    @Mock
    LogDao database;

    @Mock
    LogDaoHelper databaseHelper;

    @InjectMocks
    Pagination pagination;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        pagination = new Pagination();
        ReflectionTestUtils.setField(pagination, "database", database);
        ReflectionTestUtils.setField(pagination, "databaseHelper", databaseHelper);
        MockitoAnnotations.initMocks(this);
    }

    // database related mocked functions
    private void databaseHelper() throws IOException {
        
        ImmutableList<String> immutableListId = ImmutableList.of("2");
        ImmutableList<String> immutableListContent = ImmutableList.of("error2");
        when(databaseHelper.hitId(any())).thenReturn(immutableListId);
        when(databaseHelper.hitFieldContent(any(),any())).thenReturn(immutableListContent);
    }

    // access private method addFetchResultToData
    private Object getPrivateAddFetchResultToData(String fileType, ImmutableList < String > hitIds,
    ImmutableList < String > hitFieldContent) throws Exception 
    {
        Method method = Pagination.class.getDeclaredMethod("addFetchResultToData", new Class[] {
            String.class, ImmutableList.class, ImmutableList.class});
        method.setAccessible(true);
        return method.invoke(pagination, fileType, hitIds, hitFieldContent);
    }

    // access private method fetchAndReturnResponse
    private Object getPrivateMethodFetchAndReturnResponse(int page, String fileName, String fileType, int recordsPerPage)
     throws Exception {
        Method method = Pagination.class.getDeclaredMethod("fetchAndReturnResponse", new Class[] {
            int.class, String.class, String.class, int.class});
        method.setAccessible(true);
        return method.invoke(pagination, page, fileName, fileType, recordsPerPage);
    }

    // addFetchResultToData
    @Test
    public void addresult() throws Exception {
        HashMap<String,String>searches=new HashMap();
        searches.put("1","searchError");
        SearchErrors searchErrors = new SearchErrors();
        searchErrors.setSearchedErrors(searches);
        ImmutableList<String> hitIds = ImmutableList.<String>builder() 
                                                    .add("1","2")
                                                    .build();
            
        ImmutableList<String> hitContent = ImmutableList.<String>builder() 
                                                    .add("error1","error2")
                                                    .build();
        String actual = (String)getPrivateAddFetchResultToData(fileType1,hitIds,hitContent);
        String expected = new String("[\"searchError\",\"error2\"]");
        Assert.assertEquals(expected, actual);

    }

    // fetchAndReturnResponse
    @Test
    public void returnRequestedPageFromDb() throws Exception {
        databaseHelper();
        String actual = (String) getPrivateMethodFetchAndReturnResponse(2, fileName, fileType1, 1);
        String expected = new String ("[\"error2\"]");
        Assert.assertEquals(expected, actual);
    }
    
}