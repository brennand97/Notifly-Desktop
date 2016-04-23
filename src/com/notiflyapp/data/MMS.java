package com.notiflyapp.data;

import java.io.File;

/**
 * Created by Brennan on 4/17/2016.
 */
public class MMS extends DataObject{

    private static final long serialVersionUID = 3349238414148539468L;

    public  MMS() {
        super();
        type = Type.MMS;
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
