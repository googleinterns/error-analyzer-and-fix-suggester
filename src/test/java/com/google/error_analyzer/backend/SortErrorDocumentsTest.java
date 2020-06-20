// /**Copyright 2019 Google LLC
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//     https://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.*/

// package com.google.error_analyzer;

// import com.google.error_analyzer.backend.BooleanQuery;
// import java.util.ArrayList;
// import org.elasticsearch.search.SearchHit;
// import org.elasticsearch.search.SearchHits;
// import org.junit.Assert;
// import org.junit.Before;
// import org.junit.Rule;
// import org.junit.runner.RunWith;
// import org.junit.runners.JUnit4;
// import org.junit.Test;
// import org.mockito.junit.MockitoJUnit;
// import org.mockito.junit.MockitoRule;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;
// import org.mockito.runners.MockitoJUnitRunner;
// import static org.mockito.Mockito.times;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// @RunWith(MockitoJUnitRunner.class)
// public final class SortErrorDocumentsTest {

//     private final BooleanQuery boolQuery = new BooleanQuery();
//     private final String sourceString1 = "{\"logText\" : \"warn: KeystoreRemoteException in java class\",\"logLineNumber\" : \"1\"}";
//     private final String sourceString2 = "{\"logText\" : \"ERROR FulltextSearchQuery could not complete query request\",\"logLineNumber\" : \"2\"}";
//     private final String sourceString3 = "{\"logText\" : \"info: query completed\",\"logLineNumber\" : \"3\"}";

//     @Mock 
//     SearchHit hit1;
//     @Mock 
//     SearchHit hit2;
//     @Mock 
//     SearchHit hit3;

//     @Rule
//     public MockitoRule rule = MockitoJUnit.rule();

//     @Test
//     public void searchHitsIsEmpty() {
//         SearchHit[] hits = new SearchHit[0];
//         ArrayList<String> actual = boolQuery.sortErrorDocuments(hits);
//         ArrayList<String> expected = new ArrayList();
//         Assert.assertEquals(expected, actual);
//     }
    
//     @Test
//     public void sortErrorDocumentsTest() {
//         when(hit1.getId()).thenReturn("2");
//         when(hit1.getSourceAsString()).thenReturn(sourceString2);
//         when(hit2.getId()).thenReturn("1");
//         when(hit2.getSourceAsString()).thenReturn(sourceString1);
//         when(hit3.getId()).thenReturn("3");
//         when(hit3.getSourceAsString()).thenReturn(sourceString3);
//         SearchHit[] hits = new SearchHit[]{hit1, hit2, hit3};
//         ArrayList<String> actual = boolQuery.sortErrorDocuments(hits);
//         ArrayList<String> expected = new ArrayList();
//         expected.add(sourceString1);
//         expected.add(sourceString2);
//         expected.add(sourceString3);
//         Assert.assertEquals(expected, actual);
//     }

//     @Test
//     public void documentIdParseError() {
//         when(hit1.getId()).thenReturn("3");
//         when(hit1.getSourceAsString()).thenReturn(sourceString3);
//         when(hit2.getId()).thenReturn("");
//         when(hit3.getId()).thenReturn("2");
//         when(hit3.getSourceAsString()).thenReturn(sourceString2);
//         SearchHit[] hits = new SearchHit[]{hit1, hit2, hit3};
//         ArrayList<String> actual = boolQuery.sortErrorDocuments(hits);
//         ArrayList<String> expected = new ArrayList();
//         expected.add(sourceString2);
//         expected.add(sourceString3);
//         Assert.assertEquals(expected, actual);
//     }
// }