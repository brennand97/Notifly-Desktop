/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.debug;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by Brennan on 4/25/2016.
 */
public class ServerLog {

    private static Logger logger;

    private static final String logLocation = "server.log";
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");         //Server's log time format

    public ServerLog() {
        logger = Logger.getLogger("MyLog");
        FileHandler fh;

        try {

            // This block configure the logger with handler and formatter
            fh = new FileHandler(logLocation);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            // Other time stamps are used
            logger.setUseParentHandlers(false);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prints the messages to the server log with a location, sender, ID TAG
     *
     * @param location  String identifying where message is coming from
     * @param out   String to be displayed in server log
     */
    public void out(String location, String out, boolean log) {
        Date now = new Date();  //Gets current time message is being sent
        String time = sdf.format(now);  //Formats current time according to predefined format
        String stringOut = "[" + time + "] [" + location + "]    " + out;   //Format of time server log out
        if(log) {
            logger.info(stringOut);  //Print to server log
        }
        System.out.println(stringOut);  //Print to command line
    }

}
