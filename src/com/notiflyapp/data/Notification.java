package com.notiflyapp.data;

import java.io.File;

/**
 * Created by Brennan on 4/17/2016.
 */
public class Notification extends DataObject{

    private static final long serialVersionUID = 3349238414148539470L;

    public Notification() {
        super();
        type = Type.NOTIFICATION;
    }

    @Override
    public String getBody() {
        return null;
    }

    @Override
    public File getExtra() {
        return null;
    }

    @Override
    public byte[] getBytes() {
        return new byte[0];
    }

}
