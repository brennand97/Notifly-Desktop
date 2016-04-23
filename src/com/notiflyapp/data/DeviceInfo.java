package com.notiflyapp.data;

import java.io.File;

/**
 * Created by Brennan on 4/17/2016.
 *
 * Sent by BluetoothClient device to give the server information on the device like its name, mac address, and type (Ex. Phone, Tablet, Laptop)
 */
public class DeviceInfo extends DataObject {

    private static final long serialVersionUID = 3349238414148539469L;  //Defining UID so this object can be sent over bluetooth then be decoded again

    private String deviceName, deviceMac, deviceType;   //Holds the information as it is sent to the other device


    /**
     * Initializes a new DeviceInfo DataObject to be written to and sent to another device
     */
    public DeviceInfo() {
        super();
        type = Type.DEVICEINFO;
    }

    /**
     * @return The body of the DataObject as a String
     */
    @Deprecated
    @Override
    public String getBody() {
        return null;
        //Not needed for this instance
    }


    /**
     *
     * @return Stored device name
     */
    public String getDeviceName() { return deviceName; } //Returns the stored deviceName


    /**
     *
     * @param deviceName Set device name to be stored
     */
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }   //Sets the device name to later be sent to other device


    /**
     *
     * @return  Stored device mac address
     */
    public String getDeviceMac() { return deviceMac; }  //Returns the stored deviceMac


    /**
     *
     * @param deviceMac Set device mac address to be stored
     */
    public void setDeviceMac(String deviceMac) { this.deviceMac = deviceMac; }  //Sets the device mac address to later be sent to other device


    /**
     *
     * @return Stored device type (Phone, Tablet, Laptop, etc.)
     */
    public String getDeviceType() { return deviceType; }    //Returns the stored deviceType


    /**
     *
     * @param deviceType Set device type to be stored
     */
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }  //Sets the device type to later be sent to other device

    /**
     * @param body The body of the message being sent as a String
     */
    @Deprecated
    @Override
    public void putBody(String body) {
        //Not needed for this instance
    }

    /**
     * @return Extra data stored in the message as a File
     */
    @Deprecated
    @Override
    public File getExtra() {
        return null;
        //Not need for this instance
    }

    /**
     * @param file Extra data that goes along with the body as a File
     */
    @Deprecated
    @Override
    public void putExtra(File file) {
        //Not needed for this instance
    }


}
