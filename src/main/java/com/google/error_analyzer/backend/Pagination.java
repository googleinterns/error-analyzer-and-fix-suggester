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
public class Pagination extends HttpServlet {

    private String field = "name";
    private static String ERROR = "errors";
    private static final Logger LOG =
        LogManager.getLogger(Pagination.class);
    // keep noOfPages a odd no so that there are equal pages in front 
    // and back 
    private int noOfPages = 5;
    private LogDao database = new LogDao();
    private LogDaoHelper databaseHelper = new LogDaoHelper();

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int page = Integer.parseInt(request.getParameter("requestedPage"));
        String fileName = request.getParameter("fileName");
        String fileType = request.getParameter("fileType");
        int recordsPerPage =
            Integer.parseInt(request.getParameter("recordsPerPage"));

        response.setContentType("application/json");
        if (fileName.length() == 0) {
            String json = returnEmptyObject();
            response.getWriter().println(json);
            return;
        }
        String json =
            fetchAndReturnResponse(page, fileName, fileType, recordsPerPage);
        response.getWriter().println(json);
    }

    // fetches logs from database and return json for the same
    private String fetchAndReturnResponse(int page, String fileName,
        String fileType, int recordsPerPage) {
        int start = (page - 1) * recordsPerPage;
        int size = recordsPerPage;
        if (page == 1) {
            size = noOfPages * recordsPerPage;
        }
        if (fileType.equals(ERROR)) {
            fileName += "error";
        }
        return fetchData(start, size, fileName, fileType);
    }

    // interact with database using dao functions
    private String fetchData(int start, int size, String fileName,
        String fileType) {
        try {
            SearchHit[] searchHits =
                database.getAll(fileName, start, size);
            ImmutableList < String > hitIds =
                databaseHelper.hitId(searchHits);
            ImmutableList < String > hitFieldContent =
                databaseHelper.hitFieldContent(searchHits, field);
            if (hitIds == null) {
                return convertToJson(new ArrayList());
            }
            return addFetchResultToData(fileType, hitIds, hitFieldContent);
        } catch (IOException exception) {
            LOG.error(exception);
        }
        return convertToJson(new ArrayList());
    }

    // add errorfixes and text highlightes 
    private String addFetchResultToData(String fileType,
        ImmutableList < String > hitIds,
        ImmutableList < String > hitFieldContent) {
        ArrayList < String > data = new ArrayList();
        SearchErrors searchErrors = new SearchErrors();
        HashMap < String, String > search =
            searchErrors.getSearchedErrors();
        ErrorFixes errorFix = new ErrorFixes();
        for (int idx = 0; idx < hitIds.size(); idx++) {
            String id = hitIds.get(idx);
            String resultString = hitFieldContent.get(idx);
            String fix = new String();

            if (fileType.equals(ERROR)) {
                fix = errorFix.findFixes(resultString);
            }
            if (search.containsKey(id)) {
                resultString = search.get(id);
            }
            resultString += fix;
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
    private String returnEmptyObject() {
        String json = convertToJson(new ArrayList());
        return json;
    }

}