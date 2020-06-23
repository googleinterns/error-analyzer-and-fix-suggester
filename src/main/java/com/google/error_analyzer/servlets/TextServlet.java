// Copyright 2020 Google LLC
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// https://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.error_analyzer.servlets;

import com.google.error_analyzer.backend.StoreLogs;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

/*This class is for storing the logs entered by the user as 
plain text to the database*/
@WebServlet("/StorePlainTextLogServlet")
public class TextServlet extends HttpServlet {
    private static final String LOG_TEXT = "Log";
    private static final String FILE_NAME = "filename";
    public static final StoreLogs storeLog = new StoreLogs();

    @Override
    public void doPost(HttpServletRequest request,
     HttpServletResponse response)throws IOException, ServletException {
        response.setContentType("text/html");
        String log = request.getParameter(LOG_TEXT);
        String fileName = request.getParameter(FILE_NAME);
        String status = storeLog.checkAndStoreLog(fileName, log);
        response.getWriter().println(status);
        RequestDispatcher requestDispatcher = 
            request.getRequestDispatcher("/index.html");
        requestDispatcher.include(request, response);
    }
}