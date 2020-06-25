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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.error_analyzer.backend.LogDao;
import com.google.error_analyzer.backend.LogDaoHelper;
import com.google.error_analyzer.backend.StoreLogHelper;
import com.google.error_analyzer.data.constant.LogFields;
import com.google.error_analyzer.data.Document;
import java.io.*;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.json.simple.JSONObject;

//This class contains the methods used for storing logs
//to the database.
public class StoreLogs {
    private static final Logger logger = LogManager.getLogger(StoreLogs.class);
    private static final String LINE_BREAK = "\\r?\\n";
    private StoreLogHelper storeLogHelper = new StoreLogHelper();
    private String ERROR_TEMPLATE_RESPONSE =
        "\t\t\t<h2> Could not store file %1$s</h2>";
    public static final String FILE_STORED_TEMPLATE_RESPONSE =
        "\t\t\t<h2> File %1$s Stored</h2>";
    public static final String FILE_EMPTY_TEMPLATE_RESPONSE =
        "\t\t\t<h2> Sorry! the file %1$s is empty";
    public DaoInterface logDao = new LogDao();

    //stores the logs into database with appropriate filename
    public String checkAndStoreLog(HttpServletRequest request, String fileName,
        String log) {
        try {
            String indexName = LogDaoHelper.getIndexName(request, fileName);
            indexName = findFileName(indexName);
            final String response = storeLog(indexName, log);
            fileName = LogDaoHelper.getFileName(request, indexName);
            logger.info(String.format("File %s stored", fileName));
            return String.format(response, fileName);
        } catch (Exception e) {
            final String ERROR_RESPONSE =
                String.format(ERROR_TEMPLATE_RESPONSE, e);
            logger.error(String.format("Could not store file %1$s %2$s",
                fileName, e));
            return ERROR_RESPONSE;
        }
    }

    //Stores the log in an index with name fileName
    private String storeLog(String fileName, String log) throws IOException {
        Builder < Document > documentList = ImmutableList
            . < Document > builder();
        int logLineNumber = 1;
        String logLines[] = log.split(LINE_BREAK);
        for (String logLine: logLines) {
            String cleanedLogLine = storeLogHelper.cleanLogText(logLine);
            if (!(logLine.isEmpty())) {
                String logLineNumberString = Integer.toString(logLineNumber);
                Document document = new Document(
                    logLineNumberString, logLineNumber, cleanedLogLine);
                documentList.add(document);
                logLineNumber++;
            }
        }
        if ((documentList.build()).isEmpty()) {
            logger.error("File %1$s is empty", fileName);
            return FILE_EMPTY_TEMPLATE_RESPONSE;
        }
        logDao.bulkStoreLog(fileName, documentList.build());
        return FILE_STORED_TEMPLATE_RESPONSE;
    }

    //find the name of the index in which the logs can be stored
    public String findFileName(String fileName) throws IOException {
        if (logDao.fileExists(fileName)) {
            int fileSuffix = 1;
            String nextFileName = String.format(
                "%1$s(%2$s)", fileName, fileSuffix);
            while (logDao.fileExists(nextFileName)) {
                fileSuffix++;
                nextFileName = String.format(
                    "%1$s(%2$s)", fileName, fileSuffix);
            }
            fileName = nextFileName;
        }
        return fileName;
    }

}