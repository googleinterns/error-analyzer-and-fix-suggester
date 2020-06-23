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
import com.google.error_analyzer.backend.FileLogs;
import com.google.error_analyzer.backend.StoreLogs;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.RequestOptions;


@WebServlet("/uploadFile")
@MultipartConfig
/*This class is for storing the logs provided by the user as files to the database*/
public class FileServlet extends HttpServlet {
    private final String file = "file";
    private final String filename = "filename";
    private final StoreLogs storeLogs = new StoreLogs();

    @Override
    public void doPost(HttpServletRequest request,
         HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("text/html;");
        Part filePart = request.getPart(file);
        InputStream fileContent = filePart.getInputStream();
        FileLogs fileLogs = new FileLogs();
        String log = fileLogs.logsFromfile(fileContent);
        String fileName = request.getParameter(filename);
        String status = storeLogs.checkAndStoreLog(fileName, log);
        response.getWriter().println(status);
        RequestDispatcher requestDispatcher = request.getRequestDispatcher("/index.html");
        requestDispatcher.include(request, response);

    }
}