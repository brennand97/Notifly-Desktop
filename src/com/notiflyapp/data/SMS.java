package com.notiflyapp.data;

import java.io.File;

/**
 * Created by Brennan on 4/17/2016.
 *
 * Used for sending Short Message Service (SMS) messages between devices
 */
public class SMS extends DataObject {

    private static final long serialVersionUID = 3349238414148539467L;

    private String body;

    /**
     * Default constructor, degines DataObject.Type
     */
    public SMS() {
        super();
        type = Type.SMS;
    }

    /**
     * Constructor that sets sender and body on creation.
     *
     * @param sender The phone number of the sender of the SMS as a String.
     * @param body The body of the SMS message as a string.
     */
    public SMS(String sender, String body) {
        super();
        type = Type.SMS;
        this.sender = sender;
        this.body = body;
    }

    /**
     * @return The body of the DataObject as a String
     */
    @Override
    public String getBody() {
        return body;
    }

    /**
     * @param body The body of the message being sent as a String
     */
    @Override
    public void putBody(String body) {

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
