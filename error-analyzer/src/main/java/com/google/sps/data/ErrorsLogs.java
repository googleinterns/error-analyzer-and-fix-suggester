package com.google.sps.data;

// class contains hits of latest full-text search
import java.util.*;
public class ErrorsLogs {
    private HashSet < String > searchedErrors;

    public ErrorsLogs() {
        searchedErrors = new HashSet();
    }
    public HashSet < String > getSearchedErrors() {
        return searchedErrors;
    }
    public void setSearchedErrors(HashSet < String > list) {
        searchedErrors = list;

    }
}