package com.notiflyapp.database;

import com.notiflyapp.data.DeviceInfo;
import com.notiflyapp.data.SMS;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by Brennan on 6/8/2016.
 */
public class MessageDatabase extends Database {

    private final static String TABLE_NAME_PREFIX = "messages_";
    private String macAddress;

    private String TABLE_NAME;
    private String CREATE_TABLE;

    private static final String ADDRESS = "address";
    private static final String ORIGINATING_ADDRESS = "originating_address";
    private static final String BODY = "body";
    private static final String CREATOR = "creator";
    private static final String DATE = "date";
    private static final String DATE_SENT = "date_sent";
    private static final String PERSON = "person";
    private static final String READ = "read";
    private static final String SUBSCRIPTION_ID = "subscription_id";
    private static final String THREAD_ID = "thread_id";
    private static final String TYPE = "type";

    private static final String TYPE_SMS = "sms";
    private static final String TYPE_MMS = "mms";

    public MessageDatabase(String mac) throws NullPointerException {
        macAddress = formatMac(mac);
        if(macAddress == null) {
            throw new NullPointerException();
        }
        TABLE_NAME = TABLE_NAME_PREFIX + formatMac(macAddress);
        CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" + ID + "INTEGER PRIMARY KEY, " +
                ADDRESS + " TEXT, " + ORIGINATING_ADDRESS + " TEXT, " + BODY + " TEXT, " + CREATOR + " TEXT, " +
                DATE + " INTEGER, " + DATE_SENT + " INTEGER, " + PERSON + " TEXT, " + READ + " INTEGER, " + SUBSCRIPTION_ID + " INTEGER, " +
                THREAD_ID + "INTEGER, " + TYPE + "STRING);";
        initialize();
    }

    @Override
    protected void createTable() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(CREATE_TABLE);
    }

    public void insert(SMS sms) throws SQLException {
        StringBuilder call = new StringBuilder();
        call.append("INSERT INTO ").append(TABLE_NAME).append("(").append(ADDRESS).append(", ").append(ORIGINATING_ADDRESS).append(", ")
                .append(BODY).append(", ").append(CREATOR).append(", ").append(DATE).append(", ").append(DATE_SENT).append(", ").append(PERSON).append(", ")
                .append(READ).append(", ").append(SUBSCRIPTION_ID).append(", ").append(THREAD_ID).append(", ").append(TYPE).append(") VALUES(");
        call.append("\"").append(sms.getAddress()).append("\"").append(", ");
        call.append("\"").append(sms.getOriginatingAddress()).append("\"").append(", ");
        call.append("\"").append(sms.getBody()).append("\"").append(", ");
        call.append("\"").append(sms.getCreator()).append("\"").append(", ");
        call.append(sms.getDate()).append(", ");
        call.append(sms.getDateSent()).append(", ");
        call.append("\"").append(sms.getPerson()).append("\"").append(", ");
        call.append(sms.isRead() ? 1 : 0).append(", ");
        call.append(sms.getSubscriptionId()).append(", ");
        call.append(sms.getThreadId()).append(", ");
        call.append(TYPE_SMS).append(");");
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(call.toString());
    }

    public SMS[] querySms(String[] column, String[] value) throws UnequalArraysException, SQLException, NullResultSetException {
        ResultSet rs = query(TABLE_NAME, column, value);
        if(rs != null) {
            ArrayList<SMS> smsArray = new ArrayList<>();
            while (rs.next()) {
                smsArray.add(makeSms(rs));
            }
            SMS[] smses = new SMS[smsArray.size()];
            for(int i =  0; i < smsArray.size(); i++) {
                smses[i] = smsArray.get(i);
            }
            return smses;
        } else {
            throw NullResultSetException.makeException(TABLE_NAME);
        }
    }

    public SMS[] getAllSms() throws SQLException, NullResultSetException {
        ResultSet rs = getAll(TABLE_NAME);
        if(rs != null) {
            ArrayList<SMS> smsArray = new ArrayList<>();
            while (rs.next()) {
                smsArray.add(makeSms(rs));
            }
            SMS[] smses = new SMS[smsArray.size()];
            for(int i =  0; i < smsArray.size(); i++) {
                smses[i] = smsArray.get(i);
            }
            return smses;
        } else {
            throw NullResultSetException.makeException(TABLE_NAME);
        }
    }

    private SMS makeSms(ResultSet rs) throws SQLException {
        SMS sms = new SMS();
        sms.setAddress(rs.getString(ADDRESS));
        sms.setOriginatingAddress(rs.getString(ORIGINATING_ADDRESS));
        sms.setBody(rs.getString(BODY));
        sms.setCreator(rs.getString(CREATOR));
        sms.setDate(rs.getLong(DATE));
        sms.setDateSent(rs.getLong(DATE_SENT));
        sms.setPerson(rs.getString(PERSON));
        sms.setRead(rs.getInt(READ) == 1);
        sms.setSubscriptionId(rs.getLong(SUBSCRIPTION_ID));
        sms.setThreadId(rs.getInt(THREAD_ID));
        return sms;
    }

    String formatMac(String mac) {
        if(mac.split(":").length != 6) {
            StringBuilder newMac = new StringBuilder();
            for(int i = 0; i < mac.length(); i+=2) {
                newMac.append(mac.substring(i, i + 2));
                if(i < mac.length() - 2) {
                    newMac.append(":");
                }
            }
            return newMac.toString();
        }
        return mac;
    }

    public String getMacAddress() {
        return macAddress;
    }

}
