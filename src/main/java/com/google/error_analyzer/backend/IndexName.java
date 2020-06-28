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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/*this class contains the methods to convert fileName to indexName and 
indexName to fileName*/
public class IndexName {
    public static final String SESSIONID = "JSESSIONID";
    private static final Logger logger = LogManager.getLogger(IndexName.class);

    //returns index name for a given file name
    public static String getIndexName(HttpServletRequest request,
        String fileName) throws NullPointerException {
        String sessionId = getSessionId(request);
        String indexName = sessionId.concat(fileName);
        return indexName;
    }

    //returns file name for a given index name
    public static String getFileName(HttpServletRequest request,
        String indexName) throws NullPointerException {
        String sessionId = getSessionId(request);
        int indexOfFileName = indexName.indexOf(sessionId) +
            sessionId.length();
        String fileName = indexName
            .substring(indexOfFileName, indexName.length());
        return fileName;
    }

    //get sessionID of the user
    private static String getSessionId(HttpServletRequest request) throws NullPointerException {
        String sessionID = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie: cookies) {
                if (cookie.getName().equals(SESSIONID)) {
                    sessionID = cookie.getValue();
                }
            }
        } 
        if(sessionID == null) {
            logger.fatal("Session ID is null");
        }
        return sessionID.toLowerCase();
    }

}