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

import com.google.error_analyzer.data.Document;
import java.util.ArrayList;

/*this class contains all the methods related to the indices of the mock 
database. The Index class has attributes indexName and documentList.
documentList is an Arraylist of objects of Document class.*/
public class Index {
    private String indexName;
    private ArrayList < Document > documentList = new ArrayList < Document > ();

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public void addDocument(Document doc) {
        documentList.add(doc);
    }

    public String getIndexName() {
        return indexName;
    }

    public ArrayList < Document > getDocumentList() {
        return documentList;
    }
}