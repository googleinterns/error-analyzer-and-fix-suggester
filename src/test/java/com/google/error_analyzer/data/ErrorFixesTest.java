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
package com.google.error_analyzer.data;

import com.google.error_analyzer.data.ErrorFixes;
import com.google.gson.Gson;
import java.io.*;
import java.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
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
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.BDDMockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;	

@RunWith(MockitoJUnitRunner.class)
public final class ErrorFixesTest {
    
    ErrorFixes errorFixes;

    @Before
    public void setUp() {
        errorFixes = new ErrorFixes();
    }

    // test resultString
    @Test
    public void resultString() throws Exception {
        String actual = (String) getPrivateResultString("here is a fix");
        String expected = " <a href = \"here is a fix\" > FIX </a>";
        Assert.assertEquals(expected, actual);
    }

    private Object getPrivateResultString(String fix) 	
    throws Exception {	
        Method method = ErrorFixes.class.getDeclaredMethod(	
        "resultString", new Class[] {String.class});	
        method.setAccessible(true);	
        return method.invoke(errorFixes, fix);	
    }
}