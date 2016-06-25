package com.notiflyapp.database;

/**
 * Created by Brennan on 6/1/2016.
 */
public class NullResultSetException extends Exception {
    public NullResultSetException(String s) { super(s); }
    public NullResultSetException(Exception e) { super(e); }
    public static synchronized NullResultSetException makeException(String table) {
        return new NullResultSetException("ResultSet called from : " + table + "returned null");
    }
}
