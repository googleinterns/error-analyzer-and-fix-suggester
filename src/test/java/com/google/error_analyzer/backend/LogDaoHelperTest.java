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
import java.io.IOException;
import java.util.*;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public final class LogDaoHelperTest {
    private final String field = "name";
    private LogDaoHelper logDaoHelper;
    private SearchHit[] searchHits;
    @Mock 
    SearchHit hit1;

    @Mock 
    SearchHit hit2;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        logDaoHelper = new LogDaoHelper();
        searchHits=new SearchHit[]{hit1, hit2};
    }

    // hitId
    @Test
    public void getIdsOfGivenSearchHits() throws Exception {
        getHitId();
        ImmutableList <String> actual = logDaoHelper.hitId(searchHits);
        ImmutableList<String> expected = ImmutableList.<String>builder() 
                                          .add("1","2") 
                                          .build();  
        Assert.assertEquals(expected, actual);
    }
    
    // hitFieldContent
    @Test
    public void getFieldContentForEachHit() throws Exception {
        when(hit1.getSourceAsMap()).thenReturn(getSource("error1"));
        when(hit2.getSourceAsMap()).thenReturn(getSource("error2"));
        ImmutableList <String> actual = 
            logDaoHelper.hitFieldContent(searchHits,field);
        ImmutableList<String> expected = ImmutableList.<String>builder() 
                                          .add("error1","error2") 
                                          .build();  
        Assert.assertEquals(expected, actual);
    }

    // getHighLightedText
    @Test
    public void getHighLightedText() throws Exception {
        when(hit1.getHighlightFields()).thenReturn(highlight("error1"));
        when(hit2.getHighlightFields()).thenReturn(highlight("error2"));
        getHitId();
        ImmutableList<SearchHit> searchHits = ImmutableList
                                          .<SearchHit>builder() 
                                          .add(hit1, hit2) 
                                          .build(); 
        HashMap < String, String >actual = 
            logDaoHelper.getHighLightedText(searchHits, field);
        HashMap < String, String > expected =new HashMap(); 
        expected.put("1","error1");
        expected.put("2","error2");
        Assert.assertTrue(expected.equals(actual));
    }

    @Test
    public void errorFileNameAppend() {
        String fileName = "file";
        String actual = logDaoHelper.getErrorIndexName(fileName);
        Assert.assertEquals("fileerror", actual);
    }

    private void getHitId(){
        when(hit1.getId()).thenReturn("1");
        when(hit2.getId()).thenReturn("2");
    }

    private HashMap <String,Object> getSource(String content){
        HashMap <String,Object> fieldMap = new HashMap();
        fieldMap.put(field, content);
        return fieldMap;
    }

    private HashMap<String, HighlightField> highlight(String highlightText){
        Text [] text = new Text[]{new Text(highlightText)};
        HashMap <String,HighlightField> highlight = new HashMap();
        highlight.put(field, new HighlightField(field, text));
        return highlight;     
    }

}