/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.database;

import com.notiflyapp.data.DeviceInfo;
import org.bluez.v4.Device;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by Brennan on 6/8/2016.
 */
public class DeviceDatabase extends Database {

    public static final String MAC = "mac";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String CONNECT = "connect";
    public static final String SMS = "sms";
    public static final String NOTIFICATION = "notification";

    public static String TABLE_NAME = "bt_devices";
    protected static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            MAC + " CHARACTER(17), " + NAME + " TEXT, " + TYPE + " INTEGER, " + CONNECT + " INTEGER, " + SMS + " INTEGER, " + NOTIFICATION + " INTEGER);";

    public DeviceDatabase() {
        initialize();
    }

    @Override
    protected String getCreateTableString() {
        return CREATE_TABLE;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    public void insert(DeviceInfo di) throws SQLException {
        StringBuilder call = new StringBuilder();
        call.append("INSERT INTO ").append(TABLE_NAME).append("(").append(MAC).append(", ").append(NAME).append(", ")
                .append(TYPE).append(", ").append(CONNECT).append(", ").append(SMS).append(", ").append(NOTIFICATION).append(") VALUES(");
        call.append("\"").append(di.getDeviceMac()).append("\"").append(", ");
        call.append("\"").append(di.getDeviceName()).append("\"").append(", ");
        call.append(di.getDeviceType()).append(", ");
        call.append(di.getOptionConnect() ? 1 : 0).append(", ");
        call.append(di.getOptionSMS() ? 1 : 0).append(", ");
        call.append(di.getOptionNotification() ? 1 : 0).append(");");
        stmt.executeUpdate(call.toString());
    }

    public void update(DeviceInfo di) throws UnequalArraysException, SQLException {
        StringBuilder call = new StringBuilder();
        call.append("INSERT OR REPLACE INTO ").append(TABLE_NAME).append("(").append(ID).append(", ").append(MAC).append(", ").append(NAME).append(", ")
                .append(TYPE).append(", ").append(CONNECT).append(", ").append(SMS).append(", ").append(NOTIFICATION).append(") VALUES(");
        call.append(getId(di)).append(", ");
        call.append("\"").append(di.getDeviceMac()).append("\"").append(", ");
        call.append("\"").append(di.getDeviceName()).append("\"").append(", ");
        call.append(di.getDeviceType()).append(", ");
        call.append(di.getOptionConnect() ? 1 : 0).append(", ");
        call.append(di.getOptionSMS() ? 1 : 0).append(", ");
        call.append(di.getOptionNotification() ? 1 : 0).append(");");
        stmt.executeUpdate(call.toString());
    }

    public void nonDuplicateInsert(DeviceInfo di) throws UnequalArraysException, NullResultSetException, SQLException {
        if(has(di)) {
            if(hasIdentical(di)) {
                return;
            } else {
                update(di);
            }
        } else {
            insert(di);
        }
    }

    public DeviceInfo[] queryDeviceInfo(String[] selection, String[] value) throws SQLException, NullResultSetException, UnequalArraysException {
        ResultSet rs = query(TABLE_NAME, null, selection, value, null, null);
        if(rs != null) {
            ArrayList<DeviceInfo> infoArray = new ArrayList<>();
            while (rs.next()) {
                infoArray.add(makeDeviceInfo(rs));
            }
            DeviceInfo[] infos = new DeviceInfo[infoArray.size()];
            for(int i =  0; i < infoArray.size(); i++) {
                infos[i] = infoArray.get(i);
            }
            rs.close();
            return infos;
        } else {
            rs.close();
            throw NullResultSetException.makeException(TABLE_NAME);
        }
    }

    public DeviceInfo[] getAllDeviceInfos() throws SQLException, NullResultSetException {
        ResultSet rs = getAll(TABLE_NAME);
        if(rs != null) {
            ArrayList<DeviceInfo> infoArray = new ArrayList<>();
            while (rs.next()) {
                infoArray.add(makeDeviceInfo(rs));
            }
            DeviceInfo[] infos = new DeviceInfo[infoArray.size()];
            for(int i =  0; i < infoArray.size(); i++) {
                infos[i] = infoArray.get(i);
            }
            rs.close();
            return infos;
        } else {
            throw NullResultSetException.makeException(TABLE_NAME);
        }
    }

    public int getId(DeviceInfo info) throws SQLException, UnequalArraysException {
        ResultSet rs = query(TABLE_NAME, null, new String[]{MAC}, new String[]{info.getDeviceMac()}, null, null);
        if(rs != null) {
            rs.next();
            int id = rs.getInt(ID);
            rs.close();
            return id;
        }
        rs.close();
        return -1;
    }

    public boolean has(DeviceInfo info) throws UnequalArraysException, NullResultSetException, SQLException {
        DeviceInfo[] infos = queryDeviceInfo(new String[]{MAC}, new String[]{info.getDeviceMac()});
        if(infos.length > 0) {
            return true;
        }
        return false;
    }

    public boolean hasIdentical(DeviceInfo info) throws UnequalArraysException, NullResultSetException, SQLException {
        DeviceInfo[] infos = queryDeviceInfo(new String[]{MAC, NAME, TYPE, CONNECT, SMS, NOTIFICATION},
                new String[]{info.getDeviceMac(), info.getDeviceName(), String.valueOf(info.getDeviceType()),
                                String.valueOf(info.getOptionConnect() ? 1 : 0),String.valueOf(info.getOptionSMS() ? 1 : 0), String.valueOf(info.getOptionNotification() ? 1 : 0)});
        if(infos.length > 0) {
            return true;
        }
        return false;
    }

    private DeviceInfo makeDeviceInfo(ResultSet rs) throws SQLException {
        DeviceInfo info = new DeviceInfo();
        info.setDeviceMac(rs.getString(MAC));
        info.setDeviceName(rs.getString(NAME));
        info.setDeviceType(rs.getInt(TYPE));
        info.setOptionConnect(rs.getInt(CONNECT) == 1);
        info.setOptionSMS(rs.getInt(SMS) == 1);
        info.setOptionNotification(rs.getInt(NOTIFICATION) == 1);
        return info;
    }

}
