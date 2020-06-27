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

import com.google.error_analyzer.backend.FileLogs;
import com.google.error_analyzer.backend.StoreLogs;
import com.google.error_analyzer.data.constant.FileConstants;
import com.google.error_analyzer.data.constant.LogFields;
import com.google.error_analyzer.data.constant.PageConstants;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@WebServlet("/uploadFile")
@MultipartConfig
/*This class is for storing the logs provided by the user 
as files to the database*/
public class FileServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(FileServlet.class);
    public static final FileLogs fileLogs = new FileLogs();

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setContentType(FileConstants.TEXT_HTML_CONTENT_TYPE);
            Part filePart = request.getPart(LogFields.FILE);
            InputStream fileContent = filePart.getInputStream();
            String fileName = request.getParameter(LogFields.FILE_NAME);
            request.getSession();
            String status =
                fileLogs.checkAndStoreFileLog(request, fileName, fileContent);
            response.getWriter().println(status);
            RequestDispatcher requestDispatcher =
                request.getRequestDispatcher(PageConstants.LANDING_PAGE);
            requestDispatcher.include(request, response);
        } catch (Exception e) {
            logger.error("Could not store file", e);
        }
    }
}