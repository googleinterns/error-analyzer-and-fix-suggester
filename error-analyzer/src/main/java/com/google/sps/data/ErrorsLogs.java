package com.google.sps.data;

// class contains data fetched from db and search results
import java.util.*;
public class ErrorsLogs {
    private HashSet < String > searchedErrors;
    private ArrayList < String > logs;
    private ArrayList < String > errors;
    private String errorFileName;
    private String logFileName;

    public ErrorsLogs() {
        searchedErrors = new HashSet();
        logs = new ArrayList();
        // dummy data
        logs.add("log 1");
        logs.add("log 2");
        logs.add("log 3");
        logs.add("log 4");
        logs.add("log 5");
        logs.add("log 6");
        logs.add("log 7");
        logs.add("log 8");
        logs.add("log 9");
        logs.add("log 10");
        logs.add("log 11");
        logs.add("log 12");
        logs.add("log 13");
        logs.add("log 14");
        logs.add("log 15");
        errors = new ArrayList();
        // dummy data
        errors.add("error 1");
        errors.add("error 2");
        errors.add("error 3");
        errors.add("error 4");
        errors.add("error 5");
        errors.add("error 6");
        errors.add("error 7");
        errors.add("error 8");
        errors.add("error 9");
        errors.add("error 10");
        errorFileName = new String("test");
        logFileName = new String("test");
    }

    public ArrayList < String > getErrors() {
        return errors;
    }
    public ArrayList < String > getLogs() {
        return logs;
    }
    public HashSet < String > getSearchedErrors() {
        return searchedErrors;
    }
    public String getErrorFileName() {
        return errorFileName;
    }
    public String getLogFileName() {
        return logFileName;
    }
    public void setSearchedErrors(HashSet < String > list) {
        searchedErrors = list;

    }
    public void setLogs(ArrayList < String > list) {
        logs = list;
    }
    public void setErrors(ArrayList < String > list) {
        errors = list;
    }
    public void setErrorFileName(String file) {
        errorFileName = file;
    }
    public void setLogFileName(String file) {
        logFileName = file;
    }
}