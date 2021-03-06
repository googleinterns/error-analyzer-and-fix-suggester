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

import org.apache.commons.codec.DecoderException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.error_analyzer.backend.IndexName;
import com.google.error_analyzer.backend.LogDao;
import com.google.error_analyzer.backend.StoreLogHelper;
import com.google.error_analyzer.data.constant.LogFields;
import com.google.error_analyzer.data.Document;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.json.simple.JSONObject;
import java.util.concurrent.TimeUnit;

//This class contains the methods used for storing logs
//to the database.
public class StoreLogs {
    private static final Logger logger = LogManager.getLogger(StoreLogs.class); 
    private static final String LINE_BREAK = "\\r?\\n";
    private static final int OFFSET_FOR_PLAIN_TEXT = 0;
    private StoreLogHelper storeLogHelper = new StoreLogHelper();
    public static int MaxLogLines = 10000;
    public String ERROR_TEMPLATE_RESPONSE =
        "\t\t\t<h2> Could not store file %1$s</h2>";
    public static final String FILE_STORED_TEMPLATE_RESPONSE =
        "\t\t\t<h2> File %1$s Stored</h2>";
    public static final String FILE_EMPTY_TEMPLATE_RESPONSE =
        "\t\t\t<h2> Sorry! the file %1$s is empty</h2>";
    public DaoInterface logDao = new LogDao();

    //stores the logs into database with appropriate filename
    public String checkAndStoreLog(HttpServletRequest request,
        String fileName, InputStream fileContent, boolean isUrl) {
        try {
            String indexName = IndexName.getIndexName(request, fileName);
            indexName = getUniqueIndexName(indexName);
            final String response = storeLogsInBatches(
                request, indexName, fileContent, isUrl);
            findAndStoreErrorsInIndex(indexName);
            return response;
        } catch (Exception e) {
            final String errorResponse =
                String.format(ERROR_TEMPLATE_RESPONSE, e);
            logger.error(String.format("Could not store file %1$s %2$s",
                fileName, e));
            return errorResponse;
        }
    }

    // stores maximum 10000 log lines in a single API  call
    public String storeLogsInBatches(HttpServletRequest request, 
        String fileName, InputStream fileContent, boolean isUrl) 
        throws IOException, DecoderException, UnsupportedEncodingException  {
        InputStreamReader isReader = new InputStreamReader(fileContent);
        BufferedReader reader = new BufferedReader(isReader);
        String logLine;
        int offset = 0;
        String response = String.format(FILE_EMPTY_TEMPLATE_RESPONSE,
            IndexName.getFileName(request, fileName));
        String log = "";
        int lineCount = 0;
        while ((logLine = reader.readLine()) != null) {
            log = log + logLine + "\n";
            lineCount++;
            if (lineCount >= MaxLogLines) {
                response =
                    storeLogs(request, fileName, log, offset, isUrl);
                log = "";
                lineCount = 0;
                offset = offset + MaxLogLines;
            }
        }
        if (!log.isEmpty()) {
            response =
               storeLogs(request, fileName, log, offset, isUrl);
        }
        return response;
    }

    /*remove html tags if the logs are from url and then stores the log 
    into the database*/
    private String storeLogs(HttpServletRequest request,  String fileName,
     String log, int offset, boolean isUrl) throws IOException ,
     DecoderException, UnsupportedEncodingException  {
        if(isUrl) {
            log = UrlLogs.removeHtmlTags(log);
        }
        String response = storeLog(request, fileName, log, offset);
        return response;
    }

    //Stores the log in an index with name fileName
    public String storeLog(HttpServletRequest request, String fileName, 
     String log, int offset) throws IOException, NullPointerException, 
      DecoderException, UnsupportedEncodingException{
        Builder < Document > documentList = ImmutableList
            . < Document > builder();
        int logLineNumber = offset + 1;
        String logLines[] = log.split(LINE_BREAK);
        for (String logLine: logLines) {
            String cleanedLogLine = storeLogHelper.cleanLogText(logLine);
            if (!(logLine.isEmpty() || logLine.equals(" "))) {
                String logLineNumberString = Integer.toString(logLineNumber);
                Document document = new Document(
                    logLineNumberString, logLineNumber, cleanedLogLine);
                documentList.add(document);
                logLineNumber++;
            }
        }

        if ((documentList.build()).isEmpty()) {
            fileName = IndexName.getFileName(request, fileName);
            String response = String.format(FILE_EMPTY_TEMPLATE_RESPONSE, fileName);
            logger.info(String.format("File %s is empty", fileName));
            return response;
        }
        logDao.bulkStoreLog(fileName, documentList.build());
        fileName = IndexName.getFileName(request, fileName);
        String response = String.format(FILE_STORED_TEMPLATE_RESPONSE, fileName);
        logger.info(String.format("File %s stored", fileName));
        return response;
    }

    //find the name of the index in which the logs can be stored
    public String getUniqueIndexName(String indexName) throws IOException {
        String nextIndexName = indexName;
        int indexSuffix = 1;
        while (logDao.fileExists(nextIndexName)) {
            String encodedSuffix = IndexName
                .encodeFromStringToHex(String.format("(%s)",indexSuffix));
            nextIndexName = String.format(
                "%1$s%2$s", indexName, encodedSuffix);
            indexSuffix++;
        }
        return nextIndexName;
    }

    public void findAndStoreErrorsInIndex(String indexName) {
        try {
            TimeUnit.SECONDS.sleep(1);
            logDao.findAndStoreErrors(indexName);
        } catch (Exception e) {
            logger.error("Error while running findAndStoreErrors function "
            .concat(e.toString()));
        }
    }
}