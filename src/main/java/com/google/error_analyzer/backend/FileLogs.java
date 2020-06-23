package com.google.error_analyzer.backend;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//this class contains all the file related methods
public class FileLogs {

    // returns a string containing the content of the file 
    public String logsFromfile(InputStream fileContent) throws IOException {
        InputStreamReader isReader = new InputStreamReader(fileContent);
        BufferedReader reader = new BufferedReader(isReader);
        String str;
        String log = "";
        while ((str = reader.readLine()) != null) {
            log = log + str + "\n";
        }
        return log;

    }
}