/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.database;

/**
 * Created by Brennan on 6/8/2016.
 */
public class UnequalArraysException extends Exception {
    public UnequalArraysException(String s) { super(s); }
    public UnequalArraysException(Exception e) { super(e); }
    public static synchronized UnequalArraysException makeException() {
        return new UnequalArraysException("Provided arrays not equal in length");
    }
}
