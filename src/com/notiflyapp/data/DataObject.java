package com.notiflyapp.data;

import java.io.*;

/**
 * Created by Brennan on 4/17/2016.
 *
 * Abstract class for all data to be sent between devices.  Specific instances of data should extend this class.
 */
public abstract class DataObject implements Serializable {

    private static final long serialVersionUID = 3349238414148539466L;


    /**
     * Enum Type provides each possible instance's type of DataObject
     */
    public enum Type { SMS, MMS, NOTIFICATION, DEVICEINFO }


    /**
     * Stores the instance's type
     */
    protected Type type;
    /**
     * Stores the sender of the DataObject (aka the device name or mac address)
     */
    protected String sender;


    /**
     * Default Conductor
     */
    public DataObject() {}


    /**
     *
     * @return DataObject.Type, the type of DataObject for the current instance
     */
    public Type getType() {
        return  type;
    }


    /**
     *
     * @return the name or mac address of the device that sent the DataObject
     */
    public String getSender() {
        return sender;
    }


    /**
     *
     * @return The body of the DataObject as a String
     */
    public abstract String getBody();

    /**
     *
     * @param body The body of the message being sent as a String
     */
    public abstract void putBody(String body);

    /**
     *
     * @return Extra data stored in the message as a File
     */
    public abstract File getExtra();

    /**
     *
     * @param file Extra data that goes along with the body as a File
     */
    public abstract void putExtra(File file);


    /**
     *
     * @return  The serialized DataObject in a byte array
     * @throws IOException
     */
    public byte[] serialize() throws IOException {
        return Serial.serialize(this);
    }


    /**
     *
     * @param data  Serialized for of a DataObject as a byte array
     * @return  The dematerialized DataObject
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static DataObject deserialize(byte[] data) throws  IOException, ClassNotFoundException {
        return (DataObject) Serial.deserialize(data);
    }

}
