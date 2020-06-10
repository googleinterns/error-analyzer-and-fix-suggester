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
package com.google;

import java.util.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.sps.servlets.Pagination;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RunWith(JUnit4.class)
public final class PaginationTest {

    private Pagination pagination;
    private int recordsPerPage1 = 1;
    private int recordsPerPage3 = 8;
    private int noOfPages1 = 1;
    private String fileName = "schools";
    private String fileType = "log";
    private String nextPage = "true";
    private String prevPage = "false";@
    Before
    public void setUp() {
        pagination = new Pagination();
    }

    // maintaining window of given length
    @Test
    public void equalstartingIdxForDataBaseAndArray() throws IOException {

        int[] actual = pagination.maintainWindow(2, nextPage, fileName, new HashMap());
        int[] expected = new int[] {
            9, 9
        };

        Assert.assertArrayEquals(expected, actual);
    }
    @Test
    public void differentstartingIdxForDataBaseAndArray() throws IOException {

        int[] actual = pagination.maintainWindow(9, nextPage, fileName, new HashMap());
        int[] expected = new int[] {
            30, 0
        };

        Assert.assertArrayEquals(expected, actual);
    }
    @Test
    public void onepageWindow() throws IOException {

        pagination.noOfPages = noOfPages1;
        int[] actual = pagination.maintainWindow(9, nextPage, fileName, new HashMap());
        int[] expected = new int[] {
            30, 0
        };

        Assert.assertArrayEquals(expected, actual);
    }
    @Test
    public void onepageOneRecord() throws IOException {

        pagination.recordsPerPage = recordsPerPage1;
        int[] actual = pagination.maintainWindow(9, nextPage, fileName, new HashMap());
        int[] expected = new int[] {
            10, 0
        };

        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void userPressPrevButton() throws IOException {

        int[] actual = pagination.maintainWindow(9, prevPage, fileName, new HashMap());
        int[] expected = new int[] {
            18, 3
        };

        Assert.assertArrayEquals(expected, actual);
    }
    @Test
    public void pageToBeFetchedIsNegative() throws IOException {

        int[] actual = pagination.maintainWindow(2, prevPage, fileName, new HashMap());
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