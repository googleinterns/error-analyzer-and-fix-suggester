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

import com.google.common.collect.ImmutableList;
import com.google.error_analyzer.backend.DaoInterface;
import com.google.error_analyzer.backend.IndexName;
import com.google.error_analyzer.backend.LogDao;
import com.google.error_analyzer.backend.StoreLogs;
import com.google.error_analyzer.data.constant.FileConstants;
import com.google.error_analyzer.data.constant.LogFields;
import com.google.error_analyzer.data.constant.PageConstants;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@WebServlet("/deleteServlet")
public class DeleteServlet extends HttpServlet {
    private static final Logger logger =
        LogManager.getLogger(DeleteServlet.class);
    public static DaoInterface logDao = new LogDao();
    public static final String ERROR_RESPONSE_TEMPLATE =
        "Could not delete your Files %s";

    @Override
    public void doPost(HttpServletRequest request,
        HttpServletResponse response) throws IOException {
        response.setContentType(FileConstants.APPLICATION_JSON_CONTENT_TYPE);
        try {
            String sessionId = IndexName.getSessionId(request);
            String encodedSessionId = IndexName.encodeIndexName(sessionId);
            String status = logDao.deleteIndices(encodedSessionId);
            String json = new Gson().toJson(status);
            response.getWriter().println(json);

        } catch (Exception e) {
            String status = String.format(ERROR_RESPONSE_TEMPLATE, e);
            logger.error(status);
            String json = new Gson().toJson(status);
            response.getWriter().println(json);
        }
    }
}