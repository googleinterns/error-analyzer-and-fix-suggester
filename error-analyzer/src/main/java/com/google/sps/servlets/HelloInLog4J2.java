package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
USE:
Import Logger and LogManager in all java files and create a logger object.
Use logger.*appropriate level*("log msg").

Log statements appear both in console and target/logs/app.log
If your log file reaches 5MB in size, it gets saved in YYYY-MM directory inside target/logs
The new log data is stored in app.log

The log data is appended (not overwritten) in app.log .
*/

@WebServlet("/logger")
public class HelloInLog4J2 extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(HelloInLog4J2.class);
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("------------------------------------------------");
        logger.debug("This is a debug message");
        logger.info("This is an info message");
        logger.warn("This is a warn message");
        logger.error("This is an error message");
        logger.fatal("This is a fatal message");
        logger.trace("This is a trace message");
        //logs location is target/logs
    }
}