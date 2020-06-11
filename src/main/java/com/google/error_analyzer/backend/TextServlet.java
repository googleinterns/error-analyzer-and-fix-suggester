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
package com.google.error_analyzer.backend;

import com.google.error_analyzer.data.StoreLogs;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.action.ActionListener;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

@WebServlet("/textServlet")
public class TextServlet extends HttpServlet {
    private static final String HOSTNAME ="35.194.181.238";
    private static final int PORT = 9200;
    private static final String SCHEME = "http";

    private RestHighLevelClient client = new RestHighLevelClient(
        RestClient.builder(new HttpHost(HOSTNAME, PORT, SCHEME)));

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException ,ServletException{

        response.setContentType("text/html;");
        String logText = "Log";
        String log = request.getParameter(logText);
        String Filename="filename";
        String fileName= request.getParameter(Filename);
         StoreLogs storeLog=new StoreLogs();
         storeLog.checkAndStorePlainText(request,response,client,fileName,logText);
     }}