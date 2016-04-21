package com.data;

import java.io.File;

/**
 * Created by Brennan on 4/17/2016.
 */
public class DeviceInfo extends DataObject {

    private static final long serialVersionUID = 3349238414148539469L;

    public DeviceInfo() {
        super();
        type = Type.DEVICEINFO;
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
