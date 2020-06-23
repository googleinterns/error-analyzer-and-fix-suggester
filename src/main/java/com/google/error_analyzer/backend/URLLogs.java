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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.Charset;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;

public class URLLogs {

    private static final Logger logger = LogManager.getLogger(URLLogs.class);

    public String logsFromURL(String Url) {
        String log="";
        try {
            StringBuilder stringBuilder = new StringBuilder();
            URLConnection urlConn = null;
            InputStreamReader inputStreamReader = null;
            URL url = new URL(Url);
            urlConn = url.openConnection();
            if (urlConn != null && urlConn.getInputStream() != null) {
                inputStreamReader = new InputStreamReader(urlConn.getInputStream(), Charset.defaultCharset());
                BufferedReader bufferedReader = new BufferedReader( inputStreamReader );
                if (bufferedReader != null) {
                    int cp;
                    while ((cp = bufferedReader.read()) != -1) {
                        stringBuilder.append((char) cp);
                    }
                    bufferedReader.close();
                }
                String HtmlString = stringBuilder.toString();
                log= RemoveHTMLTags(HtmlString);


            } inputStreamReader.close();
        }
         catch (Exception e) {
            logger.error("Exception while calling URL:", e);
            throw new RuntimeException("Exception while calling URL:", e);
            
        }
        return log;
    }



    public static String RemoveHTMLTags(String html) {
        if (html == null) {
            return html;
        } 
        Document document = Jsoup.parse(html);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));
        document.select("br").append("\\n");
        document.select("p, h1, h2, h3, h4, h5, h6").prepend("\\n");
        String s = document.html().replaceAll("\\\\n", "\n");
        Whitelist whitelist = Whitelist.none();
        whitelist.addTags("a");
        whitelist.addAttributes("a", "href");
        return Jsoup.clean(s, "", whitelist, new Document.OutputSettings().prettyPrint(false));
    }
}