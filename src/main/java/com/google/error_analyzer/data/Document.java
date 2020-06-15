package com.google.error_analyzer.data;

import java.util.ArrayList;


public class  Document{
    String ID;
    String jsonString;

    public Document (String ID, String jsonString){
        this.ID=ID;
        this.jsonString=jsonString;
    }
    
    public String getJsonString () {
        return jsonString;
    }

    public String getID () {
        return ID;
    }
}