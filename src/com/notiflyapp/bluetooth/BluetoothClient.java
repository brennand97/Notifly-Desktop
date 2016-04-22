package com.notiflyapp.bluetooth;

import com.data.DataObject;
import com.data.DeviceInfo;

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
    private String deviceType;  //Device's Type obtained from received DataInfo DataObject on connect (Ex. Phone, Tablet, Laptop)


    /**
     *
     * @param server    BluetoothServer that the device connected to
     * @param conn      The uninitialized connection received by the server
     */
    public BluetoothClient(BluetoothServer server, StreamConnection conn) {
        thread = new ClientThread(this, conn);
        thread.start();
        this.server = server;
    }


    /**
     * Will disconnect client device from server
     */
    public void close() {
        if(thread.isConnected()) {
            thread.close();
            serverOut("Client disconnected.");
        }
    }


    /**
     *Will take a DeviceInfo DataObject and extract the device's information
     * from it.
     *
     * @param di DeviceInfo DataObject containing the device's Name, Mac Address, Type
     */
    private void setDeviceData(DeviceInfo di) {
        //TODO Pull all device info from initial info burst
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
                    serverOut("(" + msg.serialize().length + ") " + msg.getSender() + ": " + msg.getBody());
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
                serverOut("Failed to send message: " + msg.getType() + ": " + msg.getBody() + ": " + msg.getExtra().getPath());
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
        server.serverOut("BluetoothClient: " + deviceName, out);
    }

}
