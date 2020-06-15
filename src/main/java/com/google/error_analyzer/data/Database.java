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
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.GetRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*This class contains the methods that are making a call to elasticsearch 
to store the logs to elasticsearch*/


public class Database {

    private static final String HOSTNAME = "35.194.181.238";
    private static final int PORT = 9200;
    private static final String SCHEME = "http";
    private RestHighLevelClient client = new RestHighLevelClient(
        RestClient.builder(new HttpHost(HOSTNAME, PORT, SCHEME)));


    //checks whether index with name fileName already exists in the database;
    public boolean FileExists(String fileName) throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest();
        getIndexRequest.indices(fileName);
        boolean indexExists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        return indexExists;
    }


    //Stores the jsonString at index with name filename and returns the stored string
    public String storeLogLine(String filename, String jsonString, String Id) throws IOException {
        IndexRequest indexRequest = new IndexRequest(filename);
        indexRequest.id(Id);
        indexRequest.source(jsonString, XContentType.JSON);
        client.index(indexRequest, RequestOptions.DEFAULT);
        GetRequest getRequest = new GetRequest(filename, Id);
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        return getResponse.getSourceAsString();
    }


}