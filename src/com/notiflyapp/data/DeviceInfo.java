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

    public String getDeviceName() { return deviceName; } //Returns the stored deviceName

    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }   //Sets the device name to later be sent to other device

    public String getDeviceMac() { return deviceMac; }  //Returns the stored deviceMac

    public void setDeviceMac(String deviceMac) { this.deviceMac = deviceMac; }  //Sets the device mac address to later be sent to other device

    public String getDeviceType() { return deviceType; }    //Returns teh stored deviceType

    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }  //Sets the device type to later be sent to other device

    @Override   //Not needed for this DataObject as their are three separate pieces of information
    public String getBody() {
        return null;
    }

    @Override   //Not needed for this DataObject as their are three separate pieces of information
    public File getExtra() {
        return null;
    }


}
