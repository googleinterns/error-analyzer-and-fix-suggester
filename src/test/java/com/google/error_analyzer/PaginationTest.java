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

import com.google.error_analyzer.backend.Database;
import com.google.error_analyzer.backend.Pagination;
import java.util.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.mockito.InjectMocks;




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

    @Mock
    Database database;

    @InjectMocks
    Pagination pagination;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        pagination = new Pagination();
        pagination.database = database;
        MockitoAnnotations.initMocks(this);
    }

    // database related mocked functions
    public void databaseHelper() throws IOException {
        when(database.getAll(0, 0, fileName)).thenReturn(new SearchHit[0]);
        when(database.hitId(new SearchHit[0])).thenReturn(new ArrayList());
        when(database.hitFieldContent(new SearchHit[0], fileName)).thenReturn(new ArrayList());
    }

    // maintaining window of given length
    @Test
    public void equalstartingIdxForDataBaseAndArray() throws IOException {
        databaseHelper();
        int[] actual = pagination.maintainWindow(page2, nextPage, fileName, fileType1, new HashMap());
        int[] expected = new int[] {
            9, 9
        };
        Assert.assertArrayEquals(expected, actual);
    }
    @Test
    public void differentstartingIdxForDataBaseAndArray() throws IOException {
        databaseHelper();
        int[] actual = pagination.maintainWindow(page9, nextPage, fileName, fileType1, new HashMap());
        int[] expected = new int[] {
            30, 0
        };
        Assert.assertArrayEquals(expected, actual);
    }
    @Test
    public void onepageWindow() throws IOException {
        databaseHelper();
        pagination.noOfPages = noOfPages1;
        int[] actual = pagination.maintainWindow(page9, nextPage, fileName, fileType1, new HashMap());
        int[] expected = new int[] {
            30, 0
        };

        Assert.assertArrayEquals(expected, actual);
    }
    @Test
    public void onepageOneRecord() throws IOException {
        databaseHelper();
        pagination.recordsPerPage = recordsPerPage1;
        int[] actual = pagination.maintainWindow(page9, nextPage, fileName, fileType1, new HashMap());
        int[] expected = new int[] {
            10, 0
        };

        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void userPressPrevButton() throws IOException {
        databaseHelper();
        int[] actual = pagination.maintainWindow(page9, prevPage, fileName, fileType1, new HashMap());
        int[] expected = new int[] {
            18, 3
        };

        Assert.assertArrayEquals(expected, actual);
    }
    @Test
    public void pageToBeFetchedIsNegative() throws IOException {
        databaseHelper();
        int[] actual = pagination.maintainWindow(page2, prevPage, fileName, fileType1, new HashMap());
        int[] expected = new int[] {
            0, 0
        };

        Assert.assertArrayEquals(expected, actual);
    }

    //  lastpage  
    @Test
    public void lastPageHaveLessRecords() {
        pagination.recordsPerPage = recordsPerPage8;
        pagination.updateLastPage(3, page9);
        int actual = pagination.lastPage;
        int expected = 11;

        Assert.assertEquals(expected, actual);

        actual = pagination.noOfRecordsOnLastPage;
        expected = 3;
        Assert.assertEquals(expected, actual);
    }
    @Test
    public void lastPageHaveEqualRecords() {
        pagination.recordsPerPage = recordsPerPage8;
        pagination.updateLastPage(0, page9);
        int actual = pagination.lastPage;
        int expected = 10;

        Assert.assertEquals(expected, actual);

        actual = pagination.noOfRecordsOnLastPage;
        expected = recordsPerPage8;
        Assert.assertEquals(expected, actual);
    }
    @Test
    public void lastPageNotFoundYet() {
        pagination.recordsPerPage = recordsPerPage8;
        pagination.updateLastPage(recordsPerPage8, page9);
        int actual = pagination.lastPage;
        int expected = Integer.MAX_VALUE;

        Assert.assertEquals(expected, actual);

        actual = pagination.noOfRecordsOnLastPage;
        expected = recordsPerPage8;
        Assert.assertEquals(expected, actual);
    }

    // fetchAndStoreData
    @Test
    public void onFirstPage() throws IOException {
        databaseHelper();
        int[] actual = pagination.fetchAndStoreData(page1, fileName, fileType1, nextPage, new HashMap());
        int[] expected = new int[] {
            0, 2
        };

        Assert.assertArrayEquals(expected, actual);
    }
    @Test
    public void onLastPage() throws IOException {
        pagination.lastPage = page9;
        pagination.noOfRecordsOnLastPage = recordsPerPage1;
        databaseHelper();
        int[] actual = pagination.fetchAndStoreData(page9, fileName, fileType1, nextPage, new HashMap());
        int[] expected = new int[] {
            9, 9
        };

        Assert.assertArrayEquals(expected, actual);
    }

}