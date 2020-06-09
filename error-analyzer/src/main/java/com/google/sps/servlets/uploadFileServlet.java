
package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.action.ActionListener;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;


@WebServlet("/upload")
@MultipartConfig
public class uploadFileServlet extends HttpServlet {
    private RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        Part filePart = request.getPart("file");
        InputStream fileContent = filePart.getInputStream();
        InputStreamReader isReader = new InputStreamReader(fileContent);
        BufferedReader reader = new BufferedReader(isReader);
        StringBuffer sb = new StringBuffer();
        String str;
        while ((str = reader.readLine()) != null) {
            IndexRequest indexRequest = new IndexRequest("logs");
            String jsonString = "{" +
                "\"log\":\"" + str + "\"}";

            indexRequest.source(jsonString, XContentType.JSON);

            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        }

    }
}