package com.google.error_analyzer;

import com.google.error_analyzer.backend.UrlLogs;
import org.apache.http.HttpHost;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/* This class contains unit tests for the methods of UrlLogs*/
@RunWith(JUnit4.class)
public class UrlLogTest {
    private UrlLogs urlLogs;

    @Before
    public void setUp() throws Exception {
        urlLogs = new UrlLogs();
    }

    //remove html tags from HTML string
    @Test
    public void removeHtmlTags_removeTags() {
        String HtmlString = String.format("<!DOCTYPE html><html><head>" +
            "</head><body><h1>This is a Heading</h1></body></html>");
        String actual = urlLogs.removeHtmlTags(HtmlString);
        String expected = "\nThis is a Heading";
        assertEquals(actual, expected);
    }

    //preserve line breaks
    @Test
    public void removeHtmlTags_preserveLinebreaks() {
        String HtmlString = String.format("<!DOCTYPE html><html><head>" +
            "</head><body><h1>This is first line.\n" +
            "This is second line.<br/></h1></body></html>");
        String actual = urlLogs.removeHtmlTags(HtmlString);
        String expected = "\nThis is first line.\nThis is second line.\n";
        assertEquals(actual, expected);
    }

    //preserve links in HTML string
    @Test
    public void removeHtmlTags_preserveLinksInHTMLString() {
        String HtmlString = String.format("<!DOCTYPE html><html><head>" +
            "</head><body><h1>This is first line.\nThis is second line.</h1>" +
            "<a href=\"www.google.com\">link text</a></body></html>");
        String actual = urlLogs.removeHtmlTags(HtmlString);
        String expected = "\nThis is first line.\nThis is second line." +
            "<a href=\"www.google.com\">link text</a>";
        assertEquals(actual, expected);
    }

}