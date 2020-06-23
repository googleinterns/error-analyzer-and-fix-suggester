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
import com.google.error_analyzer.backend.URLLogs;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.Charset;
import javax.servlet.*;  
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*; 
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@WebServlet("/urlServlet")
public class UrlServlet extends HttpServlet {
    private final String url = "url";
    private final String filename = "filename";
    private final StoreLogs storeLogs = new StoreLogs();
    private static final Logger logger = LogManager.getLogger(UrlServlet.class);  

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
    throws IOException, ServletException {
        response.setContentType("text/html;");
        String Url=request.getParameter(url);
        String fileName= request.getParameter(filename);
        String log;
        try {
        URLLogs UrlLogs=new URLLogs();
        log=UrlLogs.logsFromURL(Url);
        }
        catch (Exception e) {
            response.getWriter().println("\t\t\t<h2>Exception while calling URL</h2>");
            RequestDispatcher requestDispatcher = request.getRequestDispatcher("/index.html");
            requestDispatcher.include(request, response);
            return;
        }
         String  status=storeLogs.checkAndStoreLog(fileName,log);
         response.getWriter().println(status);
         RequestDispatcher requestDispatcher = request.getRequestDispatcher("/index.html");
         requestDispatcher.include(request, response);

}}