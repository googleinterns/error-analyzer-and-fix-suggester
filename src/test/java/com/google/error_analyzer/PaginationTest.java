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
import com.google.error_analyzer.backend.MockDatabase;
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
import  org.mockito.Mockito.*;
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
    private int recordsPerPage3 = 8;
    private int noOfPages1 = 1;
    private String fileName = "hey";
    private String fileType1="errors";
    private String filetType2="logs";
    private String nextPage = "true";
    private String prevPage = "false"; 

    @Mock
    Database database;

    @InjectMocks
    Pagination pagination;

    @Rule 
    public MockitoRule rule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        pagination = new Pagination();
        pagination.database=database;
        MockitoAnnotations.initMocks(this);
    }

    // maintaining window of given length
    @Test
    public void equalstartingIdxForDataBaseAndArray() throws IOException {
        when(database.getAll(9,3,fileName)).thenReturn(new SearchHit[0]);
        when(database.hitId(new SearchHit[0])).thenReturn(new ArrayList());
        when(database.hitFieldContent(new SearchHit[0],fileName)).thenReturn(new ArrayList());

        int[] actual = pagination.maintainWindow(2, nextPage, fileName,fileType1, new HashMap());
        int[] expected = new int[] {
            9, 9
        };
        Assert.assertArrayEquals(expected, actual);
    }
    @Test
    public void differentstartingIdxForDataBaseAndArray() throws IOException {
       when(database.getAll(9,3,fileName)).thenReturn(new SearchHit[0]);
        when(database.hitId(new SearchHit[0])).thenReturn(new ArrayList());
        when(database.hitFieldContent(new SearchHit[0],fileName)).thenReturn(new ArrayList());
        int[] actual = pagination.maintainWindow(9, nextPage, fileName,fileType1, new HashMap());
        int[] expected = new int[] {
            30, 0
        };

        Assert.assertArrayEquals(expected, actual);
    }
    @Test
    public void onepageWindow() throws IOException {
        when(database.getAll(9,3,fileName)).thenReturn(new SearchHit[0]);
        when(database.hitId(new SearchHit[0])).thenReturn(new ArrayList());
        when(database.hitFieldContent(new SearchHit[0],fileName)).thenReturn(new ArrayList());
        pagination.noOfPages = noOfPages1;
        int[] actual = pagination.maintainWindow(9, nextPage, fileName,fileType1, new HashMap());
        int[] expected = new int[] {
            30, 0
        };

        Assert.assertArrayEquals(expected, actual);
    }
    @Test
    public void onepageOneRecord() throws IOException {
        when(database.getAll(9,3,fileName)).thenReturn(new SearchHit[0]);
        when(database.hitId(new SearchHit[0])).thenReturn(new ArrayList());
        when(database.hitFieldContent(new SearchHit[0],fileName)).thenReturn(new ArrayList());
        pagination.recordsPerPage = recordsPerPage1;
        int[] actual = pagination.maintainWindow(9, nextPage, fileName,fileType1, new HashMap());
        int[] expected = new int[] {
            10, 0
        };

        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void userPressPrevButton() throws IOException {
      when(database.getAll(9,3,fileName)).thenReturn(new SearchHit[0]);
        when(database.hitId(new SearchHit[0])).thenReturn(new ArrayList());
        when(database.hitFieldContent(new SearchHit[0],fileName)).thenReturn(new ArrayList());
     int[] actual = pagination.maintainWindow(9, prevPage, fileName,fileType1, new HashMap());
        int[] expected = new int[] {
            18, 3
        };

        Assert.assertArrayEquals(expected, actual);
    }
    @Test
    public void pageToBeFetchedIsNegative() throws IOException {
        when(database.getAll(9,3,fileName)).thenReturn(new SearchHit[0]);
        when(database.hitId(new SearchHit[0])).thenReturn(new ArrayList());
        when(database.hitFieldContent(new SearchHit[0],fileName)).thenReturn(new ArrayList());
        int[] actual = pagination.maintainWindow(2, prevPage, fileName,fileType1, new HashMap());
        int[] expected = new int[] {
            0, 0
        };

        Assert.assertArrayEquals(expected, actual);
    }

    //  lastpage  
    @Test
    public void lastPageHaveLessRecords() {
        pagination.recordsPerPage = recordsPerPage3;
        pagination.updateLastPage(3, 9);
        int actual = pagination.lastPage;
        int expected = 11;

        Assert.assertEquals(expected, actual);

        actual = pagination.noOfRecordsOnLastPage;
        expected = 3;
        Assert.assertEquals(expected, actual);
    }
    @Test
    public void lastPageHaveEqualRecords() {
        pagination.recordsPerPage = recordsPerPage3;
        pagination.updateLastPage(0, 9);
        int actual = pagination.lastPage;
        int expected = 10;

        Assert.assertEquals(expected, actual);

        actual = pagination.noOfRecordsOnLastPage;
        expected = recordsPerPage3;
        Assert.assertEquals(expected, actual);
    }
    @Test
    public void lastPageNotFoundYet() {
        pagination.recordsPerPage = recordsPerPage3;
        pagination.updateLastPage(8, 9);
        int actual = pagination.lastPage;
        int expected = Integer.MAX_VALUE;

        Assert.assertEquals(expected, actual);
    }

}