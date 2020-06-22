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
import java.io.IOException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//This class contains the methods used for processing logs and storing them to the database.
//This includes removal of special characters, conversion into json string.
public class StoreLogs {

    private static final Logger logger = LogManager.getLogger(StoreLogs.class);
    public DaoInterface logDao = new LogDao();

    //convert the logText and logLineNumber to a json string
    public String convertToJsonString(String logText, int logLineNumber) {
        final String removeSpecialCharactersString = "[^a-zA-Z0-9:\"]";
        final String space = " ";
        logText = logText.replaceAll(removeSpecialCharactersString, space);
        //remove special characters
        final String doubleInvertedComma = "\"";
        logText = logText.replaceAll(doubleInvertedComma, "\\\\\"");
        logText = logText.replaceAll("\\s+", " ");
        //remove extra white space
        String jsonString = String.format("{\"logLineNumber\":%1$s," +
            "\"logText\":\"%2$s\"}", logLineNumber, logText);
       // jsonString = JSON.stringify(jsonString);
        return jsonString;
    }

    //Calls the method StoreLog if an index with name fileName does not 
    //exist in db
    public String checkAndStoreLog(String fileName, String log) {
        try {
            if (logDao.fileExists(fileName) == true) {
                logger.error("File already exists");
                final String response =
                    "\t\t\t<h2> Sorry! the file already exists</h2>";
                return (response);
            } else {
                final String response = storeLog(fileName, log);
                logger.info("File Stored");
                return (response);
            }
        } catch (Exception e) {
            final String response = String.format("\t\t\t<h2> Could not store file %1$s</h2>", e);
            logger.error("Could not store file", e);
            return (response);
        }
    }

    //Stores the log in an index with name fileName
    public String storeLog(String fileName, String log) throws IOException {
        final String lineBreakString = "\\r?\\n";
        int logLineNumber = 1;
        String logLines[] = log.split(lineBreakString);
        for (String logLine: logLines) {
            if (!(logLine.equals(""))) {
                String logLineNum = Integer.toString(logLineNumber);
                String jsonString = convertToJsonString(logLine, logLineNumber);
                logDao.storeLogLine(fileName, jsonString, logLineNum);
                logLineNumber++;
            }
        }
        final String response = "\t\t\t<h2> File Stored</h2>";
        return response;
    }



}