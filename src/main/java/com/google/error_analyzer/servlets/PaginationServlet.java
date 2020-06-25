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

    private static final String ERROR = "errors";
    private static final Logger logger =
        LogManager.getLogger(PaginationServlet.class);
    private static int start;
    private static int size;
    private static String fileName;
    private LogDao logDao = new LogDao();
    private LogDaoHelper logDaoHelper = new LogDaoHelper();

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
    throws IOException {
        start = Integer.parseInt(request.getParameter("start"));
        size = Integer.parseInt(request.getParameter("size"));
        fileName = request.getParameter("fileName");
        String fileType = request.getParameter("fileType");
        String searchString = request.getParameter("searchString");
        if(fileType.equals(ERROR))
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
        ImmutableList < String > hitFieldContent = ImmutableList.
            <String>builder().build();
        try {
            if(!searchString.isEmpty()) {
                ImmutableList < SearchHit > searchHits = 
                    logDao.fullTextSearch(fileName, searchString, 
                    LogFields.LOG_TEXT, start, size);
                hitFieldContent= 
                    logDaoHelper.getHighLightedText(searchHits,
                         LogFields.LOG_TEXT);
            } else {
                ImmutableList < SearchHit > searchHits =
                    logDao.getAll(fileName, start, size);
                hitFieldContent =
                    logDaoHelper.hitFieldContent(searchHits, LogFields.LOG_TEXT);
            }
            if (hitFieldContent == null) {
                    return emptyObject();
            }
            return addErrorFixesAndBuildFinalResult(fileType, searchString,
            hitFieldContent);
        } catch (IOException exception) {
            logger.error(exception);
        }
        return emptyObject();
    }

    // add errorfixes 
    private String addErrorFixesAndBuildFinalResult(String fileType,
    String searchString,ImmutableList < String > hitFieldContent) {
        ArrayList < String > data = new ArrayList();
        int startIdx = 0;
        if(fileType.equals(ERROR) && searchString.isEmpty()) { 
            startIdx = hitFieldContent.size() -1 ;
        }
        for (int idx = startIdx; idx < hitFieldContent.size() && idx >= 0;) {
            String resultString = hitFieldContent.get(idx);
            if (fileType.equals(ERROR)) {
                // fix at this moment is a empty string but will be replaced
                // by actual fix while integrating this branch with fix-suggester 
                String fix= new String();
                resultString += " " + fix;
                idx--;
            } else {
                idx++;
            } 
            
            data.add(resultString);
        }
        return convertToJson(data);
    }

    // return json for java object
    private String convertToJson(ArrayList < String > data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return json;
    }

    // return empty json
    private String emptyObject() {
        return  convertToJson(new ArrayList());
    }

}