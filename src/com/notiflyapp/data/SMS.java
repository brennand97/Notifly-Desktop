package com.notiflyapp.data;

import java.io.File;

/**
 * Created by Brennan on 4/17/2016.
 */
public class SMS extends DataObject {

    private static final long serialVersionUID = 3349238414148539467L;

    private String body;

    public SMS() {
        super();
        type = Type.SMS;
    }

    public SMS(String sender, String body) {
        super();
        type = Type.SMS;
        this.sender = sender;
        this.body = body;
    }

    @Override
    public String getBody() {
        return body;
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
