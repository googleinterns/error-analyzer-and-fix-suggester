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

package com.google.error_analyzer.servlets;

import com.google.common.collect.ImmutableList;
import com.google.error_analyzer.data.constant.LogFields;
import com.google.error_analyzer.backend.StackTrace;
import com.google.gson.Gson;
import java.lang.*;
import java.util.*;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Servlet to request for stack trace following an error 
*/
@WebServlet("/stackTrace")
public class StackTraceServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(StackTraceServlet.class);
    public StackTrace stackTrace = new StackTrace();
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        try {
            Integer errorLineNumber = Integer.parseInt(request.getParameter(LogFields.LOG_LINE_NUMBER));
            String indexName = request.getParameter(LogFields.FILE_NAME);
            ImmutableList < String > stackList = stackTrace.findStack(errorLineNumber,indexName);
            Gson gson = new Gson();
            String json = gson.toJson(stackList);
            response.getWriter().println(json);
        } catch (NumberFormatException numberFormatException) {
            String errorMsg = "Could not parse logLineNumber ".concat(numberFormatException.toString());
            logger.error(errorMsg);
            response.getWriter().println(errorMsg);
        } catch (IOException ioException) {
            String errorMsg = "Could not connect to database";
            logger.error(errorMsg);
            response.getWriter().println(errorMsg);
        } catch (NullPointerException nullPointerException) {
            String errorMsg = "Could not complete request".concat(nullPointerException.toString());
            logger.error(errorMsg);
            response.getWriter().println(errorMsg);
        }
    }
}