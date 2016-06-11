package com.notiflyapp.servers.bluetooth;

import com.notiflyapp.data.DataObject;
import com.notiflyapp.data.DeviceInfo;
import com.notiflyapp.data.SMS;
import com.notiflyapp.controlcenter.Houston;

import javax.bluetooth.RemoteDevice;
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
    private DeviceInfo deviceInfo;
    private RemoteDevice remoteDevice;

    /**
     *
     * @param server    BluetoothServer that the device connected to
     * @param conn      The uninitialized connection received by the server
     */
    BluetoothClient(BluetoothServer server, StreamConnection conn) {
        thread = new ClientThread(this, conn);
        thread.start();
        this.server = server;
        try {
            remoteDevice = RemoteDevice.getRemoteDevice(conn);
            deviceMac = formatMac(remoteDevice.getBluetoothAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Will disconnect client device from server and remove itself from connected devices list
     */
    void close() {
        Houston.getHandler().send(() -> Houston.getInstance().updateDevice(this));
        if(thread.isConnected()) {
            thread.close();     //Called receiving threads close() method to disconnect it from device
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
        if(deviceMac == null) {
            deviceMac = formatMac(di.getDeviceMac());
        }
        deviceType = di.getDeviceType();    //Retrieves the device Type provided by the device
        deviceInfo = di;
        if(deviceInfo.getDeviceMac() == null || deviceInfo.getDeviceMac().equals("00:00:00:00:00:20")) {
            deviceInfo.setDeviceMac(deviceMac);
        }
        Houston.getHandler().send(() -> Houston.getInstance().updateDevice(this));
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
                    serverOutNoLog("(" + msg.serialize().length + ") " + ((SMS) msg).getOriginatingAddress() + ": " + msg.getBody());
                    Houston.getHandler().send(() -> Houston.getInstance().incomingMessage(this, msg));
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
        thread.send(msg);
        sent.add(msg);
        serverOutNoLog("Sent message to " + ((SMS) msg).getAddress()  + ":   " + msg.getBody());
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
        server.serverOut("BluetoothClient: " + (deviceName == null ? deviceMac : deviceName), out, true);
    }

    protected void serverOutNoLog(String out) { server.serverOut("BluetoothClient: " + deviceName, out, false); }

    private String formatMac(String mac) {
        if(mac.split(":").length != 6) {
            StringBuilder newMac = new StringBuilder();
            for(int i = 0; i < mac.length(); i+=2) {
                newMac.append(mac.substring(i, i + 2));
                if(i < mac.length() - 2) {
                    newMac.append(":");
                }
            }
            return newMac.toString();
        }
        return mac;
    }

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

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public RemoteDevice getRemoteDevice() {
        return remoteDevice;
    }

    public boolean isConnected() {
        return thread.isConnected();
    }
}
