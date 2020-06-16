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
import com.google.error_analyzer.backend.MockDatabase;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.io.IOException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public final class ErrorQueryTest {

    private final String filename = "file";
    private MockDatabase mockDatabase = null;

    @Mock
    Database database;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        mockDatabase = new MockDatabase();
    }
    // @After
    // public void validate() {
    //     validateMockitoUsage();
    // }
    @Test
    public void errorQueryTest() throws IOException {
        mockDatabase.errorQuery(filename);
        ArrayList<String> actual = mockDatabase.errorDatabase;
        when(database.errorQuery(filename)).thenReturn(true);
        database.errorQuery(filename);
        ArrayList<String> expected = new ArrayList<String>();
        expected.add("Error: nullPointerException");
        expected.add("Severe: Could not find index file");
        expected.add("warning: NullPointerException");
        verify(database, times(1)).errorQuery(filename);
        Assert.assertEquals(expected, actual);
    }
}