package com.notiflyapp.database;

import com.notiflyapp.data.DeviceInfo;

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
            if(md.formatMac(md.getMacAddress()).equals(md.formatMac(mac))) {
                return md;
            }
        }
        MessageDatabase md = new MessageDatabase(mac);
        getInstance().messages.add(md);
        return md;
    }

    public static MessageDatabase getMessageDatabase(DeviceInfo di) throws NullPointerException {
        String mac = di.getDeviceMac();
        if(mac == null) {
            throw new NullPointerException();
        }
        for(MessageDatabase md: getInstance().messages) {
            if(md.formatMac(md.getMacAddress()).equals(md.formatMac(mac))) {
                return md;
            }
        }
        MessageDatabase md = new MessageDatabase(mac);
        getInstance().messages.add(md);
        return md;
    }

}
