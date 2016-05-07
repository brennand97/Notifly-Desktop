package com.notiflyapp.data;

import java.io.File;

/**
 * Created by Brennan on 5/7/2016.
 */
public class Request extends DataObject<Class, File> {

    /**
     * @return The body of the DataObject as a String
     */
    @Override
    public Class getBody() {
        return null;
    }

    /**
     * @param body The body of the message being sent as a String
     */
    @Override
    public void putBody(Class body) {

    }

    /**
     * @return Extra data stored in the message as a File
     */
    @Override
    public File getExtra() {
        return null;
    }

    /**
     * @param file Extra data that goes along with the body as a File
     */
    @Override
    public void putExtra(File file) {

    }

}
