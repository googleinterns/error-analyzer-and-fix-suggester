// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.error_analyzer.backend;

import com.google.error_analyzer.backend.LogDao;
import com.google.error_analyzer.data.constant.LogFields;
import java.io.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

//This class contains the methods used for processing logs and storing them 
//to the database.
//This includes removal of special characters, conversion into json string.

public class StoreLogHelper {
    private static final String SPECIAL_CHARACTERS = "[^a-zA-Z0-9:./\"]";
    private static final String SPACE = " ";
    private static final String WHITE_SPACE = "\\s+";

    //clean log text
    public String cleanLogText(String logText) {
        //remove special characters
        logText = logText.replaceAll(SPECIAL_CHARACTERS, SPACE);
        //remove extra white space
        logText = logText.replaceAll(WHITE_SPACE, SPACE);
        return logText;
    }

    //convert the cleaned logText and logLineNumber to a json string
    public String convertToJsonString(String logText, int logLineNumber)
    throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(LogFields.LOG_LINE_NUMBER, logLineNumber);
        jsonObject.put(LogFields.LOG_TEXT, logText);
        StringWriter stringWriter = new StringWriter();
        jsonObject.writeJSONString(stringWriter);
        String jsonString = stringWriter.toString();
        return jsonString;
    }

}