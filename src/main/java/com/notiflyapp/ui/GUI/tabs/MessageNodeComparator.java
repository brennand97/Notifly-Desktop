package com.notiflyapp.ui.GUI.tabs;

import javafx.scene.Node;
import javafx.scene.control.Label;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by Brennan on 7/10/2016.
 */
public class MessageNodeComparator implements Comparator<Node> {
    @Override
    public int compare(Node o1, Node o2) {
        Date date1;
        Date date2;
        try {
            if(ThreadCell.MILITARY_TIME) {
                date1 = (new SimpleDateFormat(ThreadCell.DATE_FORMAT_24)).parse(((Label) o1.lookup("#message_date")).getText());
            } else {
                date1 = (new SimpleDateFormat(ThreadCell.DATE_FORMAT_12)).parse(((Label) o1.lookup("#message_date")).getText());
            }
        } catch (ParseException e) {
            return 0;
        }
        try {
            if(ThreadCell.MILITARY_TIME) {
                date2 = (new SimpleDateFormat(ThreadCell.DATE_FORMAT_24)).parse(((Label) o2.lookup("#message_date")).getText());
            } else {
                date2 = (new SimpleDateFormat(ThreadCell.DATE_FORMAT_12)).parse(((Label) o2.lookup("#message_date")).getText());
            }
        } catch (ParseException e) {
            return 0;
        }

        if(date1.getTime() == date2.getTime()) {
            return 0;
        } else if(date1.getTime() < date2.getTime()) {
            return 1;
        } else {
            return -1;
        }
    }
}
