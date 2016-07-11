package com.notiflyapp.ui.GUI.tabs;

import com.notiflyapp.data.SMS;

import java.util.Comparator;

/**
 * Created by Brennan on 7/10/2016.
 */
public class SmsComparator implements Comparator<SMS> {
    @Override
    public int compare(SMS o1, SMS o2) {
        if(o1.getDate() < o2.getDate()) {
            return 1;
        } else {
            return -1;
        }
    }
}
