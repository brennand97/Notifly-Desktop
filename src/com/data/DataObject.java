package com.data;

import java.io.*;

/**
 * Created by Brennan on 4/17/2016.
 */
public abstract class DataObject implements Serializable {

    private static final long serialVersionUID = 3349238414148539466L;

    public enum Type { SMS, MMS, NOTIFICATION, DEVICEINFO }

    protected Type type;
    protected String sender;

    public DataObject() {}

    public Type getType() {
        return  type;
    }

    public String getSender() {
        return sender;
    }

    public abstract String getBody();
    public abstract File getExtra();
    public abstract byte[] getBytes();

    public byte[] serializeThis(Object obj) throws IOException {
        return this.serialize(this);
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public byte[] serialize() throws IOException {
        return Serial.serialize(this);
    }

    public static DataObject deserialize(byte[] data) throws  IOException, ClassNotFoundException {
        return (DataObject) Serial.deserialize(data);
    }

}
