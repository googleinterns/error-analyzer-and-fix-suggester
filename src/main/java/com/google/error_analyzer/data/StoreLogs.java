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

package com.google.error_analyzer.data;

import java.io.IOException;
import org.elasticsearch.ElasticsearchException;;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.ServletException;

public class StoreLogs {

    public void checkAndStorePlainText(HttpServletRequest request, HttpServletResponse response,
     RestHighLevelClient client, String fileName, String log) throws IOException, ServletException {

        if (FileExists(fileName, client) == true) {
            response.getWriter().print("\t\t\t<h2> Sorry! the file already exists</h2>");
            RequestDispatcher rd = request.getRequestDispatcher("/index.html");
            rd.include(request, response);
        } 
        else {
            StorePlainText(client, fileName, log);
            response.getWriter().print("\t\t\t<h2> File Stored</h2>");
            RequestDispatcher rd = request.getRequestDispatcher("/index.html");
            rd.include(request, response);
        }
    }


    public void StorePlainText(RestHighLevelClient client, String fileName, String log) 
    throws IOException {

        String splitString = "\\r?\\n";
        int LogLineNumber = 1;
        String logLines[] = log.split(splitString);
        for (String logLine: logLines) {

            String logLineNumber = Integer.toString(LogLineNumber);

            String jsonString = convertToJsonString(logLine, logLineNumber);
            StoreLog(client, fileName, jsonString);
            LogLineNumber++;

        }

    }


    public String convertToJsonString(String logText, String logLineNumber) {

        String jsonString = String.format("{\"logLineNumber\":\"%1$s\"," +
            "\"logText\":\"%2$s\" }", logLineNumber, logText);
        return jsonString;
    }



    public void StoreLog(RestHighLevelClient client, String Filename, String jsonString) throws IOException {

        IndexRequest indexRequest = new IndexRequest(Filename);
        indexRequest.source(jsonString, XContentType.JSON);
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
    }


    public boolean FileExists(String fileName, RestHighLevelClient client) throws IOException {

        GetIndexRequest getIndexRequest = new GetIndexRequest();
        getIndexRequest.indices(fileName);
        boolean indexExists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        return indexExists;
    }


}