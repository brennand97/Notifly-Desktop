/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.ui.GUI.tabs.device;

import com.notiflyapp.data.DataObject;
import com.notiflyapp.data.MMS;
import com.notiflyapp.data.Message;
import com.notiflyapp.data.SMS;

import java.util.Comparator;

/**
 * Created by Brennan on 7/10/2016.
 *
 * This Comparator is used to compare Message objects in the DeviceTab Message ArrayList which is
 * then used to update the Message VBox
 */
class MessageComparator implements Comparator<Message> {
    @Override
    public int compare(Message o1, Message o2) {
        if(o1.getDate() < o2.getDate()) {
            return -1;
        } else {
            return 1;
        }
    }
}
