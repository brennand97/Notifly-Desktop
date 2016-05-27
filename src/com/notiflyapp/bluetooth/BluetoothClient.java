package com.notiflyapp.bluetooth;

import com.notiflyapp.data.DataObject;
import com.notiflyapp.data.DeviceInfo;

import javax.microedition.io.StreamConnection;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Brennan on 4/17/2016.
 *
 * Holds connection thread for the bluetooth client and the information gathered on the client
 */
public class BluetoothClient {

    private ClientThread thread;    //Reference to ClientThread object that holds device's connection socket + in and out streams
    private BluetoothServer server; //Reference to the server that stores and created this client

    private ArrayList<DataObject> received = new ArrayList<>(); //ArrayList of all DataObjects received from the device
    private ArrayList<DataObject> sent = new ArrayList<>();     //ArrayList of all DataObjects sent to the device

    private String deviceName;  //Device's name obtained from received DataInfo DataObject on connect
    private String deviceMac;   //Device's Mac Address obtained from received DataInfo DataObject on connect
    private int deviceType;  //Device's Type obtained from received DataInfo DataObject on connect (Ex. Phone, Tablet, Laptop)

    public static class Type {
        public static final int MISC              = 0x0000;
        public static final int COMPUTER          = 0x0100;
        public static final int PHONE             = 0x0200;
        public static final int NETWORKING        = 0x0300;
        public static final int AUDIO_VIDEO       = 0x0400;
        public static final int PERIPHERAL        = 0x0500;
        public static final int IMAGING           = 0x0600;
        public static final int WEARABLE          = 0x0700;
        public static final int TOY               = 0x0800;
        public static final int HEALTH            = 0x0900;
        public static final int UNCATEGORIZED     = 0x1F00;
    }

    /**
     *
     * @param server    BluetoothServer that the device connected to
     * @param conn      The uninitialized connection received by the server
     */
    BluetoothClient(BluetoothServer server, StreamConnection conn) {
        thread = new ClientThread(this, conn);
        thread.start();
        this.server = server;
    }


    /**
     * Will disconnect client device from server and remove itself from connected devices list
     */
    void close() {
        if(thread.isConnected()) {
            thread.close();     //Called receiving threads close() method to disconnect it from device
            serverOut("Client disconnected.");
        }
        if(server.isRunning()) {    //Check to see if server is calling this close of if client is so there is no ConcurrentModificationExceptions
            server.removeClient(this);  //Remove client from BluetoothServer's active clients list
        }
    }


    /**
     *Will take a DeviceInfo DataObject and extract the device's information
     * from it.
     *
     * @param di DeviceInfo DataObject containing the device's Name, Mac Address, Type
     */
    private void setDeviceData(DeviceInfo di) {
        deviceName = di.getDeviceName();    //Retrieves the device name provided by the device
        deviceMac = di.getDeviceMac();      //Retrieves the device Mac Address provided by the device
        deviceType = di.getDeviceType();    //Retrieves the device Type provided by the device
        serverOut("Updated device information.");
    }


    /**
     * Handles the DataObjects received from the client device and
     * will pass the extracted data, or whole DataObject, to the
     * appropriate functions.
     *
     * @param msg DataObject received from client
     */
    protected void receivedMsg(DataObject msg) {
        switch (msg.getType()) {
            case SMS:
                try {
                    serverOutNoLog("(" + msg.serialize().length + ") " + msg.getSender() + ": " + msg.getBody());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case MMS:
                break;
            case NOTIFICATION:
                break;
            case DEVICEINFO:
                setDeviceData((DeviceInfo) msg);
                break;
        }
        received.add(msg);
    }


    /**
     *
     * @return Array of all DataObjects received from the client device
     */
    public ArrayList<DataObject> getReceived() {
        return received;
    }


    /**
     * Sends a DataObject to client device and stores the sent object
     * in the sent array.
     *
     * @param msg DataObject to be sent to client device
     */
    public void sendMsg(DataObject msg) {
        try {
            thread.send(msg);
            sent.add(msg);
        } catch (IOException e) {
            if(msg.getExtra() != null) {
                serverOut("Failed to send message: " + msg.getType() + ": " + msg.getBody() + ": ");
            } else {
                serverOut("Failed to send message: " + msg.getType() + ": " + msg.getBody());
            }
            e.printStackTrace();
        }
    }


    /**
     *
     * @return Array of all DataObjects sent to the client device
     */
    public ArrayList<DataObject> getSent() {
        return sent;
    }


    /**
     * Prints a string to the server log with identifying TAG of "BluetoothClient: 'deviceName'"
     * and time at which it was sent.
     *
     * @param out String to print to log
     */
    protected void serverOut(String out) {
        server.serverOut("BluetoothClient: " + deviceName, out, true);
    }

    protected void serverOutNoLog(String out) { server.serverOut("BluetoothClient: " + deviceName, out, false); }


    /**
     * @return Connected bluetooth device's name
     */
    public String getDeviceName() {
        return deviceName;
    }


    /**
     * @param deviceName Set the connected bluetooth device's name
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }


    /**
     * @return Connected bluetooth device's Mac Address
     */
    public String getDeviceMac() {
        return deviceMac;
    }


    /**
     * @param deviceMac Set the connected bluetooth device's name
     */
    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }


    /**
     * @return Connected bluetooth device's Type
     */
    public int getDeviceType() {
        return deviceType;
    }


    /**
     * @param deviceType Set the connected bluetooth device's Type
     */
    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }
}
