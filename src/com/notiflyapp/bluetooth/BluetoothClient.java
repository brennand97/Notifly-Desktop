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

    private ClientThread thread;
    private BluetoothServer server;

    private ArrayList<DataObject> recieved = new ArrayList<>();
    private ArrayList<DataObject> sent = new ArrayList<>();

    private String deviceName;
    private String deviceMac;
    private String deviceType;

    public BluetoothClient(BluetoothServer server, StreamConnection conn) {
        thread = new ClientThread(this, conn);
        thread.start();
        this.server = server;
    }

    public void close() {
        thread.close();
        server.removeClient(this);
        serverOut("Client disconnected.");
    }

    private void setDataAuto(DeviceInfo di) {
        //TODO Pull all device info from initial info burst
    }

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
                setDataAuto((DeviceInfo) msg);
                break;
        }
        recieved.add(msg);
    }

    public ArrayList<DataObject> getRecieved() {
        return recieved;
    }

    public void sendMsg(DataObject msg) {
        try {
            thread.send(msg);
        } catch (IOException e) {
            if(msg.getExtra() != null) {
                serverOut("Failed to send message: " + msg.getType() + ": " + msg.getBody() + ": " + msg.getExtra().getPath());
            } else {
                serverOut("Failed to send message: " + msg.getType() + ": " + msg.getBody());
            }
            e.printStackTrace();
        }
    }

    protected void serverOut(String out) {
        server.serverOut("BluetoothClient: " + deviceName, out);
    }

    public ArrayList<DataObject> getSent() {
        return sent;
    }

}
