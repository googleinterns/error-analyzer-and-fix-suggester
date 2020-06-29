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
    private static final String STUFF_STRING = "i";
    private static final Logger logger = LogManager.getLogger(IndexName.class);

    //returns index name for a given file name
    public static String getIndexName(HttpServletRequest request,
        String fileName) throws NullPointerException {
        String sessionId = getSessionId(request);
        String indexName = sessionId.concat(fileName);
        indexName = encodeIndexName(indexName);
        return indexName;
    }

    //returns file name for a given index name
    public static String getFileName(HttpServletRequest request,
        String indexName) throws NullPointerException {
        indexName = decodeIndexName (indexName);
        String sessionId = getSessionId(request);
        int indexOfFileName = indexName.indexOf(sessionId) +
            sessionId.length();
        String fileName = indexName
            .substring(indexOfFileName, indexName.length());
        return fileName;
    }

    /*returns encoded index name*/
    public static String encodeIndexName(String indexName) {
        String encodedIndexName = "";
        for(int stringIndex = 0; stringIndex < indexName.length(); stringIndex++){ 
            int asciiValue = (int) indexName.charAt(stringIndex);
            String hex = Integer.toHexString(asciiValue);
            encodedIndexName = encodedIndexName.concat(STUFF_STRING);
            encodedIndexName = encodedIndexName.concat(hex);
        }
        return encodedIndexName;
    }

    //get sessionID of the user
    public static String getSessionId(HttpServletRequest request) throws NullPointerException {
        String sessionID = "";
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie: cookies) {
                if (cookie.getName().equals(SESSIONID)) {
                    sessionID = cookie.getValue();
                }
            }
        } 
        if(sessionID == "") {
            logger.fatal("Invalid Session");
        }
        return sessionID;
    }

    /*returns decoded index name*/
    private static String decodeIndexName(String indexName) throws NumberFormatException {
        String decodedIndexName = "";
        String decimalStrings[] = indexName.split(STUFF_STRING);
        for (String decimalString: decimalStrings) {
            if(!decimalString.isEmpty()){
            int decimalValue = Integer.parseInt(decimalString,16);
            char character = (char)decimalValue;
            String characterString = Character.toString(character);
            decodedIndexName = decodedIndexName.concat(characterString);
            }
        }
        return decodedIndexName;    
    }
}