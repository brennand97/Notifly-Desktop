/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.ui.GUI.tabs.device;

import java.util.Comparator;

/**
 * Created by Brennan on 7/10/2016.
 *
 * This Comparator is used to compare ThreadCell objects in the DeviceTab Conversation Thread ListView
 */
class ThreadComparator implements Comparator<ThreadCell> {
    @Override
    public int compare(ThreadCell o1, ThreadCell o2) {
        if(o1.getMostRecent() < o2.getMostRecent()) {
            return 1;
        } else {
            return -1;
        }
    }
}
