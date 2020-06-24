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
package com.google.error_analyzer.backend;

import com.google.common.collect.ImmutableList;
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

    //  LogField contains the name of index field which cotains the data 
    // we want to display   
    private static final String LOG_FIELD = LogFields.LOG_TEXT;
    private static final String ERROR = "errors";
    private static final Logger logger =
        LogManager.getLogger(PaginationServlet.class);
    private LogDao logDao = new LogDao();
    private LogDaoHelper logDaoHelper = new LogDaoHelper();

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
    throws IOException {
        int start = Integer.parseInt(request.getParameter("start"));
        int size = Integer.parseInt(request.getParameter("size"));
        String fileName = request.getParameter("fileName");
        String fileType = request.getParameter("fileType");
        String searchString = request.getParameter("searchString");
        if(fileType.equals(ERROR))
            fileName = logDaoHelper.getErrorIndexName(fileName);
        response.setContentType("application/json");
        if (fileName.isEmpty() || !logDao.fileExists(fileName)) {
            response.getWriter().println(emptyObject());
            return;
        }
        String json = 
            fetchPageFromDatabase(fileName, fileType, searchString, start, size);
        response.getWriter().println(json);
    }

    private String fetchPageFromDatabase(int start, int size, String fileName,
        String fileType, String searchString) {
        ImmutableList < String > hitFieldContent = ImmutableList.
            <String>builder().build();
        ImmutableList < String > hitIds = ImmutableList.
            <String>builder().build();
        try {
            if(!searchString.isEmpty()) {
                ImmutableList < SearchHit > searchHits = 
                    logDao.fullTextSearch(fileName, searchString, dataField, start, size);
                hitFieldContent= 
                    logDaoHelper.getHighLightedText(searchHits, dataField);
                hitIds=
                    logDaoHelper.hitId(searchHits);
            } else {
                ImmutableList < SearchHit > searchHits =
                    logDao.getAll(fileName, start, size);
                hitIds =
                    logDaoHelper.hitId(searchHits);
                hitFieldContent =
                    logDaoHelper.hitFieldContent(searchHits, dataField);
                if (hitIds == null) {
                    return convertToJson(new ArrayList());
                }
            }
            return addErrorFixesAndHighlights(fileType, hitIds, hitFieldContent, searchString);
        } catch (IOException exception) {
            logger.error(exception);
        }
        return convertToJson(new ArrayList());
    }

    // add errorfixes to error file and text highlights 
    private String addErrorFixesAndHighlights(String fileType,
        ImmutableList < String > hitIds,
        ImmutableList < String > hitFieldContent, String searchString) {
        ArrayList < String > data = new ArrayList();
        int startIdx = 0;
        if(fileType.equals(ERROR) && searchString.isEmpty())
            startIdx = hitIds.size() -1 ;
        for (int idx = startIdx; idx < hitIds.size() && idx >= 0;) {
            String id = hitIds.get(idx);
            String resultString = hitFieldContent.get(idx);
            String fix = new String();

            if (fileType.equals(ERROR)) {
                fix = errorFix.findFixes(resultString);
                idx--;
            } else {
                idx++;
            } 
            resultString += " " + fix;
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
        String json = convertToJson(new ArrayList());
        return json;
    }

}