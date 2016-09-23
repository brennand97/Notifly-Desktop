/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.database;

/**
 * Created by Brennan on 6/29/2016.
 */
public abstract class MacDatabase extends Database {

    protected String macAddress;

    protected String TABLE_NAME;
    protected String CREATE_TABLE;

    public MacDatabase(String mac) {
        macAddress = formatMac(mac);
        this.TABLE_NAME = getTableNamePrefix() + formatMac(macAddress);
        this.CREATE_TABLE = getCreateTableString();
        super.initialize();
    }

    public String getMacAddress() {
        return macAddress;
    }

    protected abstract String getTableNamePrefix();

    static String formatMac(String mac) {
        String[] split = mac.split(":");
        if(split.length == 6) {
            StringBuilder newMac = new StringBuilder();
            for(String part: split) {
                newMac.append(part);
            }
            return newMac.toString().toUpperCase();
        }
        return mac.toUpperCase();
    }

}
