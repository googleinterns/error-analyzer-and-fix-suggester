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

import java.nio.charset.Charset;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;

public class UrlLogs {
    private static final String PARA_AND_HEADING_TAGS = 
        "p, h1, h2, h3, h4, h5, h6";
    private static final String LINE_BREAK_TAG = "br";
    private static final String LINE_BREAK = "\n";
    private static final String HYPERLINK_TAG = "a";
    private static final String HYPERLINK_ATTRIBUTE = "href";

    public static String removeHtmlTags(String html) {
        if (html == null) {
            return html;
        } 
        Document document = Jsoup.parse(html);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));
        //add line break after line break, paragraph and heading tags.
        document.select(LINE_BREAK_TAG).append("\\n");
        document.select(PARA_AND_HEADING_TAGS).prepend("\\n");
        String htmlString = document.html().replaceAll("\\\\n", LINE_BREAK);
        //prevent rempval of hyperlinks tags
        Whitelist whitelist = Whitelist.none();
        whitelist.addTags(HYPERLINK_TAG);
        whitelist.addAttributes(HYPERLINK_TAG, HYPERLINK_ATTRIBUTE);
        return Jsoup.clean(htmlString, "", whitelist, 
                new Document.OutputSettings().prettyPrint(false));
    }

}