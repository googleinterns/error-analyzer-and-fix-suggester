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
package com.google.error_analyzer.servlets;

import com.google.common.collect.ImmutableList;
import com.google.error_analyzer.backend.LogDao;
import com.google.error_analyzer.backend.LogDaoHelper;
import com.google.error_analyzer.data.constant.FileConstants;
import com.google.error_analyzer.data.constant.LogFields;
import com.google.error_analyzer.data.Document;
import com.google.error_analyzer.data.ErrorFixes;
import com.google.gson.Gson;
import java.io.IOException;
import java.lang.*;
import java.util.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;


@WebServlet("/pagination")
public class PaginationServlet extends HttpServlet {
    private static final Logger logger =
        LogManager.getLogger(PaginationServlet.class);
    private static int start;
    private static int size;
    private static String fileName;
    private LogDao logDao = new LogDao();
    private LogDaoHelper logDaoHelper = new LogDaoHelper();
    private ErrorFixes errorFixes = new ErrorFixes();

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
    throws IOException {
        start = Integer.parseInt(request.getParameter(LogFields.START));
        size = Integer.parseInt(request.getParameter(LogFields.SIZE));
        fileName = request.getParameter(LogFields.FILE_NAME);
        String fileType = request.getParameter(LogFields.FILE_TYPE);
        String searchString = request.getParameter(LogFields.SEARCH_STRING);
        if(fileType.equals(LogFields.ERROR))
            fileName = logDaoHelper.getErrorIndexName(fileName);
        response.setContentType(FileConstants.APPLICATION_JSON_CONTENT_TYPE);
        if (fileName.isEmpty() || !logDao.fileExists(fileName)) {
            response.getWriter().println(emptyObject());
            return;
        }
        String json = 
            fetchPageFromDatabase(fileType, searchString);
        response.getWriter().println(json);
    }

    private String fetchPageFromDatabase(String fileType,
    String searchString) {
        ImmutableList < String > logTexts = ImmutableList.
            <String>builder().build();
        ImmutableList < String > logLineNumbers = ImmutableList.
            <String>builder().build();
        try {
            if(!searchString.isEmpty()) {
                ImmutableList < SearchHit > searchHits = 
                    logDao.fullTextSearch(fileName, searchString, 
                    LogFields.LOG_TEXT, start, size);
                logTexts= 
                    logDaoHelper.getHighLightedText(searchHits,
                    LogFields.LOG_TEXT);
                logLineNumbers= 
                    logDaoHelper.hitFieldContent(searchHits,
                    LogFields.LOG_LINE_NUMBER);
            } else {
                ImmutableList < SearchHit > searchHits =
                    logDao.getAll(fileName, start, size);
                logTexts =
                    logDaoHelper.hitFieldContent(searchHits,
                    LogFields.LOG_TEXT);
                logLineNumbers= 
                    logDaoHelper.hitFieldContent(searchHits,
                    LogFields.LOG_LINE_NUMBER);
            }
            if (logTexts == null) {
                    return emptyObject();
            }
            return addErrorFixesAndBuildFinalResult(fileType, searchString,
            logTexts, logLineNumbers);
        } catch (IOException exception) {
            logger.error(exception);
        }
        return emptyObject();
    }

    // add errorfixes 
    private String addErrorFixesAndBuildFinalResult(String fileType,
    String searchString,ImmutableList < String > logTexts, 
    ImmutableList < String > logLineNumbers) {
        ArrayList < Document > data = new ArrayList();
        int startIdx = 0;
        if(fileType.equals(LogFields.ERROR) && searchString.isEmpty()) { 
            startIdx = logTexts.size() -1 ;
        }
        for (int idx = startIdx; idx < logTexts.size() && idx >= 0;) {
            String logText = logTexts.get(idx);
            int logLineNo = Integer.parseInt(logLineNumbers.get(idx));
            if (fileType.equals(LogFields.ERROR)) {
                String fix= errorFixes.findFixes(logText);
                logText += " " + fix;
                idx--;
            } else {
                idx++;
            } 
            try{
                Document document = new Document("",logLineNo,logText);
                data.add(document);
            }catch(IOException exception) {
                logger.error(exception);
            }
        }
        return convertToJson(data);
    }

    // return json for java object
    private String convertToJson(ArrayList < Document > data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return json;
    }

    // return empty json
    private String emptyObject() {
        return  convertToJson(new ArrayList());
    }

}