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
import java.io.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

//This class contains the methods used for processing logs and storing them 
//to the database.
//This includes removal of special characters, conversion into json string.
public class StoreLogs {
    private static final Logger logger = LogManager.getLogger(StoreLogs.class);
    private static final String SPECIAL_CHARACTERS = "[^a-zA-Z0-9:\"]";
    private static final String SPACE = " ";
    private static final String DOUBLE_INVERTED_COMMA = "\"";
    private static final String WHITE_SPACE = "\\s+";
    private static final String LINE_BREAK = "\\r?\\n";
    public static final String FILE_STORED_RESPONSE =
        "\t\t\t<h2> File Stored</h2>";
    public static final String FILE_NOT_STORED_RESPONSE =
        "\t\t\t<h2> Sorry! the file already exists. " +
        "Please try with a different file name</h2>";
    public DaoInterface logDao = new LogDao();

    //clean log text
    private String cleanLogText(String logText) {
        //remove special characters
        logText = logText.replaceAll(SPECIAL_CHARACTERS, SPACE);
        logText = logText.replaceAll(DOUBLE_INVERTED_COMMA, "\\\\\"");
        //remove extra white space
        logText = logText.replaceAll(WHITE_SPACE, SPACE);
        return logText;

    }

    //convert the cleaned logText and logLineNumber to a json string
    private String convertToJsonString(String logText, int logLineNumber)
    throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("logLineNumber", logLineNumber);
        jsonObject.put("logText", logText);
        StringWriter stringWriter = new StringWriter();
        jsonObject.writeJSONString(stringWriter);
        String jsonString = stringWriter.toString();
        return jsonString;
    }

    //Stores the log in an index with name fileName
    private String storeLog(String fileName, String log) throws IOException {
        int logLineNumber = 1;
        String logLines[] = log.split(LINE_BREAK);
        for (String logLine: logLines) {
            if (!(logLine.isEmpty())) {
                String logLineNumberString = Integer.toString(logLineNumber);
                logLine = cleanLogText(logLine);
                String jsonString = convertToJsonString(logLine, logLineNumber);
                logDao.storeLogLine(fileName, jsonString, logLineNumberString);
                logLineNumber++;
            }
        }
        return FILE_STORED_RESPONSE;
    }

    //Calls the method StoreLog if an index with name fileName does not 
    //exist in db
    public String checkAndStoreLog(String fileName, String log) {
        try {
            if (logDao.fileExists(fileName)) {
                logger.error(String.format("File %s already exists", fileName));
                return FILE_NOT_STORED_RESPONSE;
            } else {
                final String response = storeLog(fileName, log);
                logger.info(String.format("File %s stored", fileName));
                return response;
            }
        } catch (Exception e) {
            final String errorResponse =
                String.format("\t\t\t<h2> Could not store file %1$s</h2>", e);
            logger.error("Could not store file", e);
            return errorResponse;
        }
    }


}