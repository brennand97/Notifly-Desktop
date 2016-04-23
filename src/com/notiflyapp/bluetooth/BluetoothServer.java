package com.notiflyapp.bluetooth;

import javax.bluetooth.*;
import javax.microedition.io.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Brennan on 4/16/2016.
 *
 * Initializes system Bluetooth and starts a thread that accepts incoming connections, passing them off to
 * BluetoothClients that are keep track of by serverConn.
 */
public class BluetoothServer extends Thread{

    private final UUID uuid = new UUID("8502318923c1410ca7c729a91f49f764", false);                                       //Service UUID for bluetooth discovery and connection
    private final String name = "SMS Server";                                                                            //Service name
    private final String url = "btspp://localhost:" + uuid + ";name=" + name + ";authenticate=false;encrypt=false;";     //Service URL, for Bluetooth RFCOMM service

    private LocalDevice local = null;                                           //Holds local bluetooth device that will accept connections
    private StreamConnectionNotifier serverConn = null;                         //Socket that is listened on to accept connections
    private ArrayList<BluetoothClient> currentClients = new ArrayList<>();      //List of current connected and active clients

    private boolean running = false;                                                        //Server's main loop is active
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");         //Server's log time format


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
                currentClients.add(bc);     //Adds the create BluetoothClient to serverConn's list of active clients
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
    void removeClient(BluetoothClient client) { currentClients.remove(client); }


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
        if(local.getDiscoverable() != DiscoveryAgent.NOT_DISCOVERABLE) {    //Check to see if the local bluetooth device is still discoverable
            try {
                local.setDiscoverable(DiscoveryAgent.NOT_DISCOVERABLE);     //Set local bluetooth device to 'Not Discoverable'
                serverOut("Turned off Discovery");  //Announce change to server log
            } catch (BluetoothStateException e) {
                e.printStackTrace();
            }
        }
        currentClients.forEach(BluetoothClient::close); //Close all connected BluetoothClients by calling their respective close() method
        currentClients.clear();     //Clear client least due to no active clients
        serverOut("Server stopped.");   //Announce final BluetoothServer state to server log
    }


    /**
     * Initializes the BluetoothServer, called by default constructor.
     * Finds local bluetooth device and initializes the bluetooth server connection, advertises presence, and makes discoverable.
     *
     * @throws IOException
     */
    private void init() throws IOException {
        if(local == null) {     //Checks to see if local bluetooth device already defined
            local = LocalDevice.getLocalDevice();   //Finds local bluetooth device
            if(local == null) { //Checks to make sure a device was found
                serverOut("No local bluetooth found, cannot initialize server.");   //If no device found, no bluetooth available
                System.exit(1);     //Exits application
            } else {
                serverOut("Found local bluetooth"); //Announces bluetooth discovery to server log
            }
        }
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
        serverConn = (StreamConnectionNotifier) Connector.open(url);    //Opens the bluetooth socket being listened to with RFCOMM bluetooth URL
        serverOut("Advertising service");   //Announces server change to server log
    }


    /**
     *
     * @return if server's main loop is running, if it is active
     */
    public boolean isRunning() { return running; }

    /**
     * Send message to server log
     *
     * @param out String to be printed to server log with
     */
    private void serverOut(String out) {
        serverOut("BluetoothServer", out);
    }


    /**
     * Prints the messages to the server log with a location, sender, ID TAG
     *
     * @param location  String identifying where message is coming from
     * @param out   String to be displayed in server log
     */
    void serverOut(String location, String out) {
        Date now = new Date();  //Gets current time message is being sent
        String time = sdf.format(now);  //Formats current time according to predefined format
        System.out.println("[" + time + "] [" + location + "]    " + out);  //Format of time server log out
    }

}
