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

package com.google.sps.servlets;

import com.google.gson.Gson;
import java.util.*;
import java.lang.*;
import com.google.sps.data.ErrorsLogs;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



@
WebServlet("/pagination")
public class Pagination extends HttpServlet {

    private final int recordsPerPage = 3;
    private final int noOfPages = 5;
    private final int extraPageInFrontAndBack = 2;
    private ArrayList < String > data = new ArrayList();

    // java object need to be returned 
    private class ReturnObj {
        ArrayList < String > logsOrErrors;
        int totalPages;
        public ReturnObj(ArrayList < String > logsOrErrors, int totalPages) {
            this.logsOrErrors = logsOrErrors;
            this.totalPages = totalPages;
        }
    }

    @
    Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int page = Integer.parseInt(request.getParameter("requestedPage"));
        String fileType = request.getParameter("fileType");
        String fileName = request.getParameter("fileName");
        String next = request.getParameter("next");

        ErrorsLogs errLogObj = new ErrorsLogs();
        ArrayList < String > display = new ArrayList();
        int totalPages = 1;

        // fill display with the data of file asked by user 
        if (fileType.equals("logs")) {
            String storedFileName = errLogObj.getLogFileName();
            if (storedFileName.equals(fileName)) {
                display = errLogObj.getLogs();
                totalPages = totalNoOfPages(display.size());
            } else {}
            //   fetch from db

        } else {
            String storedFileName = errLogObj.getErrorFileName();
            if (storedFileName.equals(fileName)) {
                display = errLogObj.getErrors();
                totalPages = totalNoOfPages(display.size());
            } else {}
            // fetch from db
        }

        HashSet < String > search = errLogObj.getSearchedErrors();

        //  if user is asking for 1st page return that page and make a window of fully finished and ready to use data for that file
        // if user is not on 1st page but someother page just maintain the already created window of pages
        if (page == 1) {
            String json = returnResponse(display, 0, Math.min(recordsPerPage, display.size()) - 1, search, totalPages);
            response.setContentType("application/json");
            response.getWriter().println(json);
            int offset = 0;
            int total = recordsPerPage * Math.min(noOfPages, totalPages);
            // create window
            addToData(offset, display, offset, total - 1);
        } else {
            int start = recordsPerPage * ((page - 1) % noOfPages);
            int stop = start + recordsPerPage;
            // if the page user is asking for is the last page then it can contain less data than the standard amount of data that we were showing
            if (page == totalPages && display.size() % recordsPerPage != 0)
                stop = start + (display.size() % recordsPerPage);
            String json = returnResponse(data, start, stop - 1, search, totalPages);
            response.setContentType("application/json");
            response.getWriter().println(json);

            // maintain window
            if (next.equals("true") && page + extraPageInFrontAndBack <= totalPages) {
                int offset = recordsPerPage * (page + extraPageInFrontAndBack - 1);
                int total = (recordsPerPage * (page + extraPageInFrontAndBack)) - 1;
                int startidx = recordsPerPage * ((page + extraPageInFrontAndBack - 1) % noOfPages);
                addToData(startidx, display, offset, total);
            } else if (next.equals("false") && page - extraPageInFrontAndBack > 0) {
                int offset = recordsPerPage * (page - extraPageInFrontAndBack - 1);
                int total = (recordsPerPage * (page - extraPageInFrontAndBack)) - 1;
                int startidx = recordsPerPage * ((page - extraPageInFrontAndBack - 1) % noOfPages);
                addToData(startidx, display, offset, total);
            }
        }
    }
    // picks content from maintained window on the basis of required page and convert it into json format
    private String returnResponse(ArrayList < String > sourceList, int startIdx, int stopIdx, HashSet < String > search, int totalPages) {
        ArrayList < String > currentPage = new ArrayList();
        for (int i = startIdx; i <= stopIdx; i++) {
            String element = sourceList.get(i);
            if (search.contains(element)) {
                currentPage.add(element);
            } else {
                currentPage.add(element);
            }
        }
        ReturnObj returnObj = new ReturnObj(currentPage, totalPages);
        return convertToJson(returnObj);
    }
    // put/change content of data for maintaining continuous window of pages(here window of 5 pages)
    private void addToData(int startIdxData, ArrayList < String > sourceList, int startIdxSource, int stopIdxSource) {
        for (int i = startIdxData, j = startIdxSource; j <= stopIdxSource && j < sourceList.size(); i++, j++) {
            if (i >= data.size())
                data.add(sourceList.get(j));
            else
                data.set(i, sourceList.get(j));
        }
    }
    // return json for java object
    private String convertToJson(ReturnObj returnObj) {
        Gson gson = new Gson();
        String json = gson.toJson(returnObj);
        return json;
    }
    // returns total pages for a file 
    private int totalNoOfPages(int len) {
        if (len % recordsPerPage == 0)
            return len / recordsPerPage;
        else
            return (len / recordsPerPage) + 1;
    }

}