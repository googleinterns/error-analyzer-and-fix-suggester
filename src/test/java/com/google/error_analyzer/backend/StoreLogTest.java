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
import com.google.error_analyzer.backend.StoreLogs;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.io.IOException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.InjectMocks;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)

public final class StoreLogTest {
    private MockDatabase mockDatabase = null;

    @Mock
    Database database;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @InjectMocks
    StoreLogs storeLog;

    @Before
    public void setUp() {
        mockDatabase = new MockDatabase();
        storeLog = new StoreLogs();
        storeLog.database = database;
    }

    @Test
    public void checkAndStoreLogTest() throws IOException {
        String log = "error1";
        String fileName = "samplefile";
        boolean fileExists = mockDatabase.FileExists(fileName);
        String  StoredLog = mockDatabase.storeLogLine(fileName, log, "1");
        when(database.FileExists(fileName)).thenReturn(fileExists);
        when(database.storeLogLine(fileName, log, "1")).thenReturn(StoredLog);
        String expected = "\t\t\t<h2> File Stored</h2>";
        String actual = storeLog.checkAndStoreLog(fileName, log);
        assertEquals(expected, actual);
    
    }

    @Test
    public void checkAndStoreLogInAlreadyExistingFileTest() throws IOException {
        String log = "error2";
        String fileName = "samplefile1";
        boolean fileExists = mockDatabase.FileExists(fileName);
        String  StoredLog = mockDatabase.storeLogLine(fileName, log, "1");
        when(database.FileExists(fileName)).thenReturn(fileExists);
        when(database.storeLogLine(fileName, log, "1")).thenReturn(StoredLog);
        storeLog.checkAndStoreLog(fileName, log);
        boolean FileExists = mockDatabase.FileExists(fileName);
        String  Storedlog = mockDatabase.storeLogLine(fileName, log, "1");
        when(database.FileExists(fileName)).thenReturn(FileExists);
        when(database.storeLogLine(fileName, log, "1")).thenReturn(Storedlog);
        String expected = "\t\t\t<h2> Sorry! the file already exists</h2>";
        String actual = storeLog.checkAndStoreLog(fileName, log);
        assertEquals(expected, actual);
    
    }

    @Test
    public void storeLogLineTest(){
        String log="error2";
        String fileName = "samplefile2";
        String actual = mockDatabase.storeLogLine(fileName, log, "1");
        String expected = "error2";
        assertEquals(expected, actual);
    }

    @Test
    public void FileExistsTestwhenFileDoesNotExists(){
        String log = "error2";
        String fileName = "samplefile3";
        boolean actual = mockDatabase.FileExists(fileName);
        boolean expected = false;
        assertEquals(expected, actual);
    }

    @Test
    public void FileExistsTestwhenFileAlreadyExists() throws IOException{
        String log = "error2";
        String fileName = "samplefile4";
        boolean fileExists = mockDatabase.FileExists(fileName);
        String  StoredLog = mockDatabase.storeLogLine(fileName, log, "1");
        when(database.FileExists(fileName)).thenReturn(fileExists);
        when(database.storeLogLine(fileName, log, "1")).thenReturn(StoredLog);
        storeLog.checkAndStoreLog(fileName, log);
        boolean actual = mockDatabase.FileExists(fileName);
        boolean expected = true;
        assertEquals(expected, actual);
    }
}