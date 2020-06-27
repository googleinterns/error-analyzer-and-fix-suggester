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

import com.google.error_analyzer.backend.StoreLogs;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//this class contains all the file related methods
public class FileLogs {
    private static final Logger logger = LogManager.getLogger(FileLogs.class);
    private static final int MAX_LOG_LINES = 10000;
    public StoreLogs storeLogs = new StoreLogs();

    //stores the logs into database with appropriate filename
    public String checkAndStoreFileLog(HttpServletRequest request,
        String fileName, InputStream fileContent) {
        try {
            String indexName = IndexName.getIndexName(request, fileName);
            indexName = storeLogs.getUniqueIndexName(indexName);
            final String response = storeFileLogs(request, indexName, fileContent);
            return response;
        } catch (Exception e) {
            final String errorResponse =
                String.format(storeLogs.ERROR_TEMPLATE_RESPONSE, e);
            logger.error(String.format("Could not store file %1$s %2$s",
                fileName, e));
            return errorResponse;
        }
    }

    // stores maximum 10000 log lines in a single API  call
    public String storeFileLogs(HttpServletRequest request, String fileName,
        InputStream fileContent) throws IOException {
        InputStreamReader isReader = new InputStreamReader(fileContent);
        BufferedReader reader = new BufferedReader(isReader);
        String logLine;
        int offset = 0;
        String response = String.format(storeLogs.FILE_EMPTY_TEMPLATE_RESPONSE,
            IndexName.getFileName(request, fileName));
        String log = "";
        int LineCount = 0;
        while ((logLine = reader.readLine()) != null) {
            log = log + logLine + "\n";
            LineCount++;
            if (LineCount >= MAX_LOG_LINES) {
                response =
                    storeLogs.storeLog(request, fileName, log, offset);
                log = "";
                LineCount = 0;
                offset = offset + MAX_LOG_LINES;
            }
        }
        if (!log.isEmpty()) {
            response =
                storeLogs.storeLog(request, fileName, log, offset);
        }
        return response;
    }

}