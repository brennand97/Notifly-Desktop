/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.servers.bluetooth;

import com.notiflyapp.data.DeviceInfo;
import com.notiflyapp.data.SMS;
import com.notiflyapp.servers.Server;

import javax.bluetooth.*;
import javax.microedition.io.*;

import java.io.*;

/**
 * Created by Brennan on 4/16/2016.
 *
 * Initializes system Bluetooth and starts a thread that accepts incoming connections, passing them off to
 * BluetoothClients that are keep track of by serverConn.
 */
public class BluetoothServer extends Server<BluetoothClient>{

    private final UUID uuid = new UUID("8502318923c1410ca7c729a91f49f764", false);                                       //Service UUID for bluetooth discovery and connection
    private final String name = "SMS Server";                                                                            //Service name
    private final String url = "btspp://localhost:" + uuid + ";name=" + name + ";authenticate=false;encrypt=false;";     //Service URL, for Bluetooth RFCOMM service

    private LocalDevice local = null;                                           //Holds local bluetooth device that will accept connections
    private StreamConnectionNotifier serverConn = null;                         //Socket that is listened on to accept connections


    /**
     * Initialize the BluetoothServer
     */
    public BluetoothServer() {
        try {
            init();     //Calls the BluetoothServer.init() method to prepare the server
            running = true;     //Sets main loop to be running for when run() is called by Thread.start()
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasDevice(DeviceInfo deviceInfo) {
        for(BluetoothClient client: connectedClients) {
            if(client.getDeviceName().equals(deviceInfo.getDeviceName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void sendMessage(SMS smsMessage, DeviceInfo deviceInfo) {
        connectedClients.stream().filter(client -> client.getDeviceName().equals(deviceInfo.getDeviceName())).forEach(client -> {
            client.sendMsg(smsMessage);
        });
    }


    /**
     * Thread start method
     *
     * Contains the main loop for the BluetoothServer, accepts connections and creates new BluetoothClients to
     * pass them off to.
     */
    public void run() {
        serverOut("Accepting connections");
        while(running) {
            StreamConnection conn;   //Temporary accepted connection holder until passed off to BluetoothClient
            try {
                conn = serverConn.acceptAndOpen();  //Accepts the incoming connection with the specified UUID
                BluetoothClient bc = new BluetoothClient(this, conn);   //Creates a new BluetoothClient passing in itself  and the uninitialized accepted connection
                serverOut("Client connected");
                connectedDevice(bc);     //Adds the create BluetoothClient to serverConn's list of active clients
            } catch (InterruptedIOException e1) {
                serverOut("Connection closed");
                //This is the 'conn' connection being closed, most likely do to the serverConn's close() method being called
            } catch (IOException e) {
                serverOut("Connection failed");
                e.printStackTrace();
            }
        }
    }


    /**
     * Allows a BluetoothClient to remove itself from the serverConn active client list on disconnect
     *
     * @param client BluetoothClient to be removed from the BluetoothServer active client list
     */
    void removeClient(BluetoothClient client) {
        connectedClients.remove(client);
    }

    public void disconnectClient(BluetoothClient client) {
        client.close();
        removeClient(client);
    }


    /**
     * Stops the BluetoothServer and all the connected BluetoothClients.
     * Closes all connections and streams in the process.
     */
    public void close() {
        running = false;    //Stop the main serverConn loop
        try {
            serverConn.close(); //Closes the connection socket that is listening for incoming connections, thus forcing an InterruptedIOException on the main thread making it stop
            this.join();        //Waits for main loop to cope with InterruptedIOException and come to a halt
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        stopDiscovery();
        connectedClients.forEach(BluetoothClient::close); //Close all connected BluetoothClients by calling their respective close() method
        connectedClients.clear();     //Clear client least due to no active clients
        serverOut("Server stopped.");   //Announce final BluetoothServer state to server log
    }


    /**
     * Initializes the BluetoothServer, called by default constructor.
     * Finds local bluetooth device and initializes the bluetooth server connection, advertises presence, and makes discoverable.
     *
     * @throws IOException
     */
    protected void init() throws IOException {
        if(local == null) {     //Checks to see if local bluetooth device already defined
            local = LocalDevice.getLocalDevice();   //Finds local bluetooth device
            if(local == null) { //Checks to make sure a device was found
                serverOut("No local bluetooth found, cannot initialize server.");   //If no device found, no bluetooth available
                System.exit(1);     //Exits application
            } else {
                serverOut("Found local bluetooth"); //Announces bluetooth discovery to server log
            }
        }
        serverConn = (StreamConnectionNotifier) Connector.open(url);    //Opens the bluetooth socket being listened to with RFCOMM bluetooth URL
    }

    public void startDiscovery() throws IOException {
        if(local.getDiscoverable() == DiscoveryAgent.NOT_DISCOVERABLE) {    //Checks to make sure local bluetooth's discovery isn't already enabled
            try {
                local.setDiscoverable(DiscoveryAgent.GIAC);     //Enables local device's bluetooth discovery
                serverOut("Turned on Discovery");   //Announces change to server log
            } catch (BluetoothStateException e) {
                //This may be called when the server has been stopped and restarted in one application session.
                //If such is the case the discovery, for some reason, will still be on.
                serverOut("Discovery could not be turned on");  //Announces failure to initialize bluetooth to server log
            }
        }
        serverOut("Advertising service");   //Announces server change to server log
        serverOut("Started Discovering");
    }

    public void stopDiscovery() {
        if(local.getDiscoverable() != DiscoveryAgent.NOT_DISCOVERABLE) {    //Check to see if the local bluetooth device is still discoverable
            try {
                local.setDiscoverable(DiscoveryAgent.NOT_DISCOVERABLE);     //Set local bluetooth device to 'Not Discoverable'
                serverOut("Turned off Discovery");  //Announce change to server log
            } catch (BluetoothStateException e) {
                e.printStackTrace();
            }
        }
        serverOut("Stopped Discovery");
    }

    /**
     * Send message to server log
     *
     * @param out String to be printed to server log with
     */
    protected void serverOut(String out) {
        serverOut("BluetoothServer", out, true);
    }

}
