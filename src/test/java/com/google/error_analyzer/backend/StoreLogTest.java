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
import com.google.error_analyzer.backend.MockLogDao;
import com.google.error_analyzer.backend.StoreLogs;
import java.io.IOException;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)

/*this class contains tests for the methods used for 
storing logs to the database*/
public final class StoreLogTest {
    private StoreLogs storeLogs;

    @Before
    public void setUp() {
        storeLogs = new StoreLogs();
        storeLogs.logDao = new MockLogDao();
    }

    //store the log into the database when index with name same as the
    //file name does not exist in the database and then trying to
    //store another file with same fileName
    @Test
    public void checkAndStoreLogInAlreadyExistingFileTest()
    throws IOException {
        String log = "error2";
        String fileName = "samplefile1";
        String expected = storeLogs.FILE_STORED_RESPONSE;
        String actual = storeLogs.checkAndStoreLog(fileName, log);
        assertEquals(expected, actual);
        expected = storeLogs.FILE_ALREADY_EXISTS_RESPONSE;
        actual = storeLogs.checkAndStoreLog(fileName, log);
        assertEquals(expected, actual);
    }

}