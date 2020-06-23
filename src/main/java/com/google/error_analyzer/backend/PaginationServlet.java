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
import com.google.error_analyzer.data.ErrorFixes;
import com.google.error_analyzer.data.SearchErrors;
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

    // dataField contains the name of index field which cotains the data we want to display   
    private static final String dataField = LogFields.LOG_TEXT;
    private static final String ERROR = "errors";
    private static final Logger logger =
        LogManager.getLogger(PaginationServlet.class);
    // keep noOfPages a odd no so that there are equal pages in front 
    // and back 
    private LogDao database = new LogDao();
    private LogDaoHelper databaseHelper = new LogDaoHelper();
    private ErrorFixes errorFix = new ErrorFixes();

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int start = Integer.parseInt(request.getParameter("start"));
        int size = Integer.parseInt(request.getParameter("size"));
        String fileName = request.getParameter("fileName");
        String fileType = request.getParameter("fileType");

        response.setContentType("application/json");
        if (fileName.length() == 0 || !database.fileExists(fileName)) {
            response.getWriter().println(emptyObject());
            return;
        }
        String json = fetchPageFromDatabase(start, size, fileName, fileType);
        response.getWriter().println(json);
    }

    private String fetchPageFromDatabase(int start, int size, String fileName,
        String fileType) {
        try {
            SearchHit[] searchHits =
                database.getAll(fileName, start, size);
            ImmutableList < String > hitIds =
                databaseHelper.hitId(searchHits);
            ImmutableList < String > hitFieldContent =
                databaseHelper.hitFieldContent(searchHits, dataField);
            if (hitIds == null) {
                return convertToJson(new ArrayList());
            }
            return addErrorFixesAndHighlights(fileType, hitIds, hitFieldContent);
        } catch (IOException exception) {
            logger.error(exception);
        }
        return convertToJson(new ArrayList());
    }

    // add errorfixes and text highlights 
    private String addErrorFixesAndHighlights(String fileType,
        ImmutableList < String > hitIds,
        ImmutableList < String > hitFieldContent) {
        ArrayList < String > data = new ArrayList();
        SearchErrors searchErrors = new SearchErrors();
        HashMap < String, String > search =
            searchErrors.getSearchedErrors();
        int startIdx = 0;
        if(fileType.equals(ERROR))
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
            if (search.containsKey(id)) {
                resultString = search.get(id);
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
