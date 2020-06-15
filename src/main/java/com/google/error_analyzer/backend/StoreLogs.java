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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;

public class StoreLogs {
    
    public static Database database = new Database();

    public String convertToJsonString(String logText, String logLineNumber) {
        logText=logText.replaceAll("[^a-zA-Z0-9:\"]", " ");
        logText=logText.replaceAll("\"","\'");
        String jsonString = String.format("{\"logLineNumber\":\"%1$s\"," +
            "\"logText\":\"%2$s\"}", logLineNumber, logText);
        return jsonString;
    }

    //Stores the log into the database if an index with name fileName does not exist in the database and returns a string that contains the status of the log string whether the log string was stored in the database or not.
    public String checkAndStoreLog(String fileName, String log) throws IOException {
        if (database.FileExists(fileName) == true) {
            return ("\t\t\t<h2> Sorry! the file already exists</h2>");
        } 
        else {
            String splitString = "\\r?\\n";
            int LogLineNumber = 1;
            String logLines[] = log.split(splitString);
            for (String logLine: logLines) {
                String logLineNumber = Integer.toString(LogLineNumber);
                String jsonString = convertToJsonString(logLine, logLineNumber);
                database.storeLogLine(fileName, jsonString, logLineNumber);
                LogLineNumber++;
            }
            return ("\t\t\t<h2> File Stored</h2>");

        }
    }
    
}