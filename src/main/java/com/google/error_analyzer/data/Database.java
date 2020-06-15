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

import com.google.error_analyzer.data.StoreLogs;
import java.io.IOException;
import org.elasticsearch.ElasticsearchException;;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.RestClient;
import org.apache.http.HttpHost;
import  org.elasticsearch.action.get.GetResponse;
import  org.elasticsearch.action.get.GetRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Database{
    private static final String HOSTNAME ="localhost";
    private static final int PORT = 9200;
    private static final String SCHEME = "http";
    private RestHighLevelClient client = new RestHighLevelClient(
            RestClient.builder(new HttpHost(HOSTNAME, PORT, SCHEME)));
    private static final Logger logger = LogManager.getLogger(Database.class);
             

    //checks whether index with name fileName already exists in the database;
    public boolean FileExists(String fileName) throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest();
        getIndexRequest.indices(fileName);
        boolean indexExists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        return indexExists;
    }


    //Stores the jsonString at index with name filename and returns the logText of the document stored
    public String storeLogLine(String Filename, String jsonString,String Id) throws IOException {
        IndexRequest indexRequest = new IndexRequest(Filename);
        indexRequest.id(Id); 
        indexRequest.source(jsonString, XContentType.JSON);
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        GetRequest getRequest = new GetRequest(Filename, Id); 
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        return getResponse.getSourceAsString();
    }

    //Stores the log into the database if an index with name fileName does not exist in the database and returns a string that contains the status of the log string whether the log string was stored in the database or not.
    public String checkAndStoreLog(String fileName, String log) throws IOException {
        if (FileExists(fileName) == true) {
            logger.info("File already exists");
            return ("\t\t\t<h2> Sorry! the file already exists</h2>");
        } 
        else {
            String splitString = "\\r?\\n";
            int LogLineNumber = 1;
            String logLines[] = log.split(splitString);
            for (String logLine: logLines) {
                if(!(logLine.equals(""))){
                String logLineNumber = Integer.toString(LogLineNumber);
                StoreLogs storelog = new StoreLogs();
                String jsonString = storelog.convertToJsonString(logLine, logLineNumber);
                storeLogLine(fileName, jsonString, logLineNumber);
                LogLineNumber++;}
            }
            logger.info("File Stored");
            return ("\t\t\t<h2> File Stored</h2>");

        }


    }
    }