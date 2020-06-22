/**Copyright 2019 Google LLC
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    https://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.google.error_analyzer.backend;

import com.google.gson.Gson;
import java.io.IOException;
import java.lang.*;
import java.util.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


@WebServlet("/getCount")
public class DocCount extends HttpServlet {

    private static LogDao database = new LogDao();
    private static final Logger LOG =
        LogManager.getLogger(DocCount.class);

    // return no of document in given index
    @Override
    public void doPost(HttpServletRequest request, 
        HttpServletResponse response) throws IOException {
        String index = request.getParameter("index");
        long count = 0l;
        try {
            count = database.getDocCount(index);
        }  catch (Exception exception) {
            count = 0l;
            LOG.error(exception);
        } 
        response.setContentType("application/json");
        String json =convertToJson(count);
        response.getWriter().println(json);
    }

    // return json for java object
    private String convertToJson(long count) {
        Gson gson = new Gson();
        String json = gson.toJson(count);
        return json;
    }
}