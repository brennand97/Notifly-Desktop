package com.notiflyapp.database;

import com.notiflyapp.data.DeviceInfo;
import com.notiflyapp.ui.GUI.tabs.ThreadCell;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Brennan on 6/8/2016.
 */
public class DatabaseFactory {

    private static DatabaseFactory instance;

    private final DeviceDatabase device;
    private ArrayList<MessageDatabase> messages;

    private DatabaseFactory() {
        device = new DeviceDatabase();
        messages = new ArrayList<>();
    }

    public static DatabaseFactory getInstance() {
        if(instance == null) {
            instance = new DatabaseFactory();
        }
        return instance;
    }

    public static DeviceDatabase getDeviceDatabase() {
        return getInstance().device;
    }

    public static MessageDatabase getMessageDatabase(String mac) throws NullPointerException {
        if(mac == null) {
            throw new NullPointerException();
        }
        for(MessageDatabase md: getInstance().messages) {
            if(MacDatabase.formatMac(md.getMacAddress()).equals(md.formatMacOld(mac))) {
                return md;
            }
        }
        MessageDatabase md = new MessageDatabase(mac);
        getInstance().messages.add(md);
        return md;
    }

    public static ThreadDatabase getThreadDatabase(String mac) {
        MessageDatabase md = getMessageDatabase(mac);
        return md.getThreadDatabase();
    }

    public static ContactDatabase getContactDatabase(String mac) {
        MessageDatabase md = getMessageDatabase(mac);
        return md.getContactDatabase();
    }

    public void dropMessageTables() throws SQLException {
        for(MessageDatabase d: messages) {
            d.drop(d.TABLE_NAME);
        }
        messages.clear();
    }

}
