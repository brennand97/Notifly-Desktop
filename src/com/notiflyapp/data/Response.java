package com.notiflyapp.data;

import java.io.File;

/**
 * Created by Brennan on 5/7/2016.
 */
public class Response extends DataObject<String, String> {

    private static final long serialVersionUID = 3349238414148539472L;

    private String requestString;
    private String UUID;

    public Response() {
        super();
        type = Type.REQUEST;
    }

    /**
     * @return The body of the DataObject as a String
     */
    @Override
    public String getBody() {
        return requestString;
    }

    /**
     * @param body The body of the message being sent as a String
     */
    @Override
    public void putBody(String body) {
        requestString = body;
    }

    /**
     * @return Extra data stored in the message as a File
     */
    @Override
    public String getExtra() {
        return UUID;
    }

    /**
     * @param extra Extra data that goes along with the body as a File
     */
    @Override
    public void putExtra(String extra) {
        UUID = extra;
    }

}
