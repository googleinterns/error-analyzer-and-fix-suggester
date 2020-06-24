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

import com.google.error_analyzer.backend.StoreLogHelper;
import java.io.IOException;
import java.util.ArrayList;

/*The document class has attributes id, logLineNumber, logText,
 and jsonString. Id represents the document id in the database.
 Data will be stored as json strings in the documents*/
 public class Document {
    private String id;
    private int logLineNumber;
    private String logText;
    private String jsonString;
    private static StoreLogHelper storeLogHelper = new StoreLogHelper();

    public Document(String id, String jsonString) {
        this.id = id;
        this.jsonString = jsonString;
    }

    public Document(String id, int logLineNumber, String logText)
     throws IOException {
        this.id = id;
        this.logLineNumber = logLineNumber;
        this.logText = logText;
        this.jsonString = storeLogHelper
            .convertToJsonString(logText, logLineNumber);
    }

    public String getJsonString() {
        return jsonString;
    }

    public String getID() {
        return id;
    }

}
