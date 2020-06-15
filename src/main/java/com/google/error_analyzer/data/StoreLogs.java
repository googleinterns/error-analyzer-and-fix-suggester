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

package com.google.error_analyzer.data;

import com.google.error_analyzer.data.Database;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;

//This class contains the methods used for processing logs and storing them to the database.
//This includes removal of special characters, conversion into json string.


public class StoreLogs {

    private static final Logger logger = LogManager.getLogger(StoreLogs.class);
    private Database database = new Database();

    //convert the logText and logLineNumber to a json string
    public String convertToJsonString(String logText, String logLineNumber) {
        final String removeSpecialCharactersString = "[^a-zA-Z0-9:\"]";
        final String space = " ";
        logText = logText.replaceAll(removeSpecialCharactersString, space); //remove special characters
        final String doubleInvertedComma = "\"";
        final String singleInvertedComma = "\'";
        logText = logText.replaceAll(doubleInvertedComma, singleInvertedComma);
        String jsonString = String.format("{\"logLineNumber\":\"%1$s\"," +
            "\"logText\":\"%2$s\"}", logLineNumber, logText);
        return jsonString;
    }

    //Calls the method StoreLog if an index with name fileName does not exist in db
    public String checkAndStoreLog(String fileName, String log) throws IOException {
        if (database.FileExists(fileName) == true) {
            logger.info("File already exists");
            final String response = "\t\t\t<h2> Sorry! the file already exists</h2>";
            return (response);
        } else {
            StoreLog(fileName, log);
            logger.info("File Stored");
            final String response = "\t\t\t<h2> File Stored</h2>";
            return (response);

        }
    }

    //Stores the log in an index with name fileName
    public void StoreLog(String fileName, String log) throws IOException {
        final String lineBreakString = "\\r?\\n";
        int logLineNumber = 1;
        String logLines[] = log.split(lineBreakString);
        for (String logLine: logLines) {
            if (!(logLine.equals(""))) {
                String logLineNum = Integer.toString(logLineNumber);
                String jsonString = convertToJsonString(logLine, logLineNum);
                database.storeLogLine(fileName, jsonString, logLineNum);
                logLineNumber++;
            }
        }
    }



}