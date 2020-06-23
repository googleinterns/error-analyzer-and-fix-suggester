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
import java.io.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.json.simple.JSONObject;

//This class contains the methods used for processing logs and storing them to the database.
//This includes removal of special characters, conversion into json string.
public class StoreLogs {

    private static final Logger logger = LogManager.getLogger(StoreLogs.class);
    private static final String specialCharacters = "[^a-zA-Z0-9:\"]";
    private static final String space = " ";
    private static final String doubleInvertedComma = "\"";
    public static final String fileStoredResponse =
                        "\t\t\t<h2> File Stored</h2>"; 
    public static final String fileNotStoredResponse = 
                        "\t\t\t<h2> Sorry! the file already exists"+
                        "Please try with a different file name</h2>";
    public DaoInterface logDao = new LogDao();

    //clean log text
    public String cleanLogText(String logText) {
        //remove special characters
        logText = logText.replaceAll(specialCharacters, space);
        logText = logText.replaceAll(doubleInvertedComma, "\\\\\"");
        //remove extra white space
        logText = logText.replaceAll("\\s+", space);
        return logText;
        
    }

    //convert the logText and logLineNumber to a json string
    public String convertToJsonString (String logText, int logLineNumber) throws IOException {
        
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("logLineNumber", logLineNumber);
        jsonObject.put("logText", logText);
        StringWriter stringWriter = new StringWriter();
        jsonObject.writeJSONString(stringWriter);
        String jsonString = stringWriter.toString();
        return jsonString;
    }

    //Calls the method StoreLog if an index with name fileName does not 
    //exist in db
    public String checkAndStoreLog(String fileName, String log) {
        try {
            if (logDao.fileExists(fileName)) {
                logger.error("File already exists");
                return fileNotStoredResponse;
            } else {
                String response = storeLog(fileName, log);
                String findErrorsResponse = findAndStoreErrors(fileName);
                response = response.concat(findErrorsResponse);
                logger.info("File Stored");
                return response;
            }
        } catch (Exception e) {
            final String errorResponse = String.format("\t\t\t<h2> Could not store file %1$s</h2>", e);
            logger.error("Could not store file", e);
            return errorResponse;
        }
    }

    //Stores the log in an index with name fileName
    public String storeLog(String fileName, String log) throws IOException {
        final String lineBreakString = "\\r?\\n";
        int logLineNumber = 1;
        String logLines[] = log.split(lineBreakString);
        for (String logLine: logLines) {
            if (!((logLine.equals("")))) {
                String logLineNum = Integer.toString(logLineNumber);
                logLine = cleanLogText(logLine);
                String jsonString = convertToJsonString(logLine, logLineNumber);
                logDao.storeLogLine(fileName, jsonString, logLineNum);
                logLineNumber++;
            }
        }
        return fileStoredResponse;
    }

    private String findAndStoreErrors(String fileName) {
        try {
            logDao.findAndStoreErrors(fileName);
            return "<h2>Processed successfuly</h2>";
        } catch (IOException e) {
            return "<h2>Could not process file</h2>";
        }
    }

}