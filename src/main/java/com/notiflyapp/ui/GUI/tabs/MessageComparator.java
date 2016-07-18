package com.notiflyapp.ui.GUI.tabs;

import com.notiflyapp.data.DataObject;
import com.notiflyapp.data.MMS;
import com.notiflyapp.data.Message;
import com.notiflyapp.data.SMS;

import java.util.Comparator;

/**
 * Created by Brennan on 7/10/2016.
 */
public class MessageComparator implements Comparator<Message> {
    @Override
    public int compare(Message o1, Message o2) {
        if(o1.getDate() < o2.getDate()) {
            return -1;
        } else {
            return 1;
        }
    }
}
