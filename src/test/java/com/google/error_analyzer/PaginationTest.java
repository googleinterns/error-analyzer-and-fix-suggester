
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
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.Mock;
import org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public final class PaginationTest {

    private int recordsPerPage1 = 1;
    private int recordsPerPage8 = 8;
    private int noOfPages1 = 1;
    private String fileName = "file";
    private String fileType1 = "errors";
    private String filetType2 = "logs";
    private String nextPage = "true";
    private String prevPage = "false";
    private int page1 = 1;
    private int page2 = 2;
    private int page9 = 9;
    // private LogDaoHelper databaseHelper = new LogDaoHelper();

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
        when(database.getAll(fileName, 0, 0)).thenReturn(new SearchHit[0]);
        ImmutableList<String> immutableList = ImmutableList.of();
        when(databaseHelper.hitId(new SearchHit[0])).thenReturn(immutableList);
        when(databaseHelper.hitFieldContent(new SearchHit[0], fileName)).thenReturn(immutableList);
    }
    // return private class variable object
    private Object getPrivateVariable(String variableName) throws Exception {
        Field privateVariable = Pagination.class.getDeclaredField(variableName);
        privateVariable.setAccessible(true);
        return privateVariable.get(pagination);
    }

    // access private method maintainWindow
    private Object getPrivateMethodMaintainWindow(int page, String next, String fileName, 
    String fileType, HashMap < String, String > search) throws Exception 
    {
        Method method = Pagination.class.getDeclaredMethod("maintainWindow", new Class[] {
            int.class, String.class, String.class, String.class, HashMap.class
        });
        method.setAccessible(true);
        return method.invoke(pagination, page, next, fileName, fileType, search);
    }

    // access private method fetchAndStoreData
    private Object getPrivateMethodFetchAndStoreData(int page, String fileName, String fileType,
    String next, HashMap < String, String > search) throws Exception 
    {
        Method method = Pagination.class.getDeclaredMethod("fetchAndStoreData", new Class[] {
            int.class, String.class, String.class, String.class, HashMap.class
        });
        method.setAccessible(true);
        return method.invoke(pagination, page, fileName, fileType, next, search);
    }

    // access private method updateLastPage
    private Object getPrivateMethodUpdateLastPage(int searchHitLength, int page) throws Exception {
        Method method = Pagination.class.getDeclaredMethod("updateLastPage", new Class[] {
            int.class, int.class
        });
        method.setAccessible(true);
        return method.invoke(pagination, searchHitLength, page);
    }

    // maintaining window of given length
    @Test
    public void equalstartingIdxForDataBaseAndArray() throws Exception {
        databaseHelper();
        int[] actual = (int[]) getPrivateMethodMaintainWindow(page2, nextPage, fileName, fileType1, new HashMap());
        int[] expected = new int[] {
            9, 9
        };
        Assert.assertArrayEquals(expected, actual);
    }
    
    @Test
    public void differentstartingIdxForDataBaseAndArray() throws Exception {
        databaseHelper();
        int[] actual = (int[]) getPrivateMethodMaintainWindow(page9, nextPage, fileName, fileType1, new HashMap());
        int[] expected = new int[] {
            30, 0
        };
        Assert.assertArrayEquals(expected, actual);
    }
    
    @Test
    public void onepageWindow() throws Exception {
        databaseHelper();
        ReflectionTestUtils.setField(pagination, "noOfPages", noOfPages1);
        int[] actual = (int[]) getPrivateMethodMaintainWindow(page9, nextPage, fileName, fileType1, new HashMap());
        int[] expected = new int[] {
            30, 0
        };

        Assert.assertArrayEquals(expected, actual);
    }
    
    @Test
    public void onepageOneRecord() throws Exception {
        databaseHelper();
        ReflectionTestUtils.setField(pagination, "recordsPerPage", recordsPerPage1);
        int[] actual = (int[]) getPrivateMethodMaintainWindow(page9, nextPage, fileName, fileType1, new HashMap());
        int[] expected = new int[] {
            10, 0
        };

        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void userPressPrevButton() throws Exception {
        databaseHelper();
        int[] actual = (int[]) getPrivateMethodMaintainWindow(page9, prevPage, fileName, fileType1, new HashMap());
        int[] expected = new int[] {
            18, 3
        };

        Assert.assertArrayEquals(expected, actual);
    }
    
    @Test
    public void pageToBeFetchedIsNegative() throws Exception {
        databaseHelper();
        int[] actual = (int[]) getPrivateMethodMaintainWindow(page2, prevPage, fileName, fileType1, new HashMap());
        int[] expected = new int[] {
            0, 0
        };

        Assert.assertArrayEquals(expected, actual);
    }

    //  lastpage  
    @Test
    public void lastPageHaveLessRecords() throws Exception {
        ReflectionTestUtils.setField(pagination, "recordsPerPage", recordsPerPage8);
        getPrivateMethodUpdateLastPage(3, page9);
        int actual = (int) getPrivateVariable("lastPage");
        int expected = 11;

        Assert.assertEquals(expected, actual);

        actual = (int) getPrivateVariable("noOfRecordsOnLastPage");
        expected = 3;
        Assert.assertEquals(expected, actual);
    }
    
    @Test
    public void lastPageHaveEqualRecords() throws Exception {
        ReflectionTestUtils.setField(pagination, "recordsPerPage", recordsPerPage8);
        getPrivateMethodUpdateLastPage(0, page9);
        int actual = (int) getPrivateVariable("lastPage");
        int expected = 10;

        Assert.assertEquals(expected, actual);

        actual = (int) getPrivateVariable("noOfRecordsOnLastPage");
        expected = recordsPerPage8;
        Assert.assertEquals(expected, actual);
    }
    
    @Test
    public void lastPageNotFoundYet() throws Exception {
        ReflectionTestUtils.setField(pagination, "recordsPerPage", recordsPerPage8);
        getPrivateMethodUpdateLastPage(recordsPerPage8, page9);
        int actual = (int) getPrivateVariable("lastPage");
        int expected = Integer.MAX_VALUE;

        Assert.assertEquals(expected, actual);

        actual = (int) getPrivateVariable("noOfRecordsOnLastPage");
        expected = recordsPerPage8;
        Assert.assertEquals(expected, actual);
    }

    // fetchAndStoreData
    @Test
    public void onFirstPage() throws Exception {
        databaseHelper();
        int[] actual = (int[]) getPrivateMethodFetchAndStoreData(page1, fileName, fileType1, nextPage, new HashMap());
        int[] expected = new int[] {
            0, 2
        };

        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void onLastPage() throws Exception {
        ReflectionTestUtils.setField(pagination, "lastPage", page9);
        ReflectionTestUtils.setField(pagination, "noOfRecordsOnLastPage", recordsPerPage1);
        databaseHelper();
        int[] actual = (int[]) getPrivateMethodFetchAndStoreData(page9, fileName, fileType1, nextPage, new HashMap());
        int[] expected = new int[] {
            9, 9
        };

        Assert.assertArrayEquals(expected, actual);
    }

}