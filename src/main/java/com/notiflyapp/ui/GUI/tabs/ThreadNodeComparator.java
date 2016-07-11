package com.notiflyapp.ui.GUI.tabs;

import javafx.scene.Node;
import javafx.scene.control.Label;

import java.util.Comparator;

/**
 * Created by Brennan on 7/10/2016.
 */
public class ThreadNodeComparator implements Comparator<Node> {
    @Override
    public int compare(Node o1, Node o2) {
        long date1;
        long date2;
        try {
            date1 = Long.parseLong(((Label) o1.lookup("#date_label")).getText());
        } catch (NumberFormatException e) {
            date1 = 0;
        }
        try {
            date2 = Long.parseLong(((Label) o2.lookup("#date_label")).getText());
        } catch (NumberFormatException e) {
            date2 = 0;
        }
        if(date1 < date2) {
            return 1;
        } else {
            return -1;
        }
    }
}
