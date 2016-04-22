package com.notiflyapp.bluetooth;

import javax.bluetooth.*;
import javax.microedition.io.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Brennan on 4/16/2016.
 */
public class BluetoothServer extends Thread{

    public final UUID uuid = new UUID("8502318923c1410ca7c729a91f49f764", false);
    public final String name = "SMS Server";
    public final String url = "btspp://localhost:" + uuid + ";name=" + name + ";authenticate=false;encrypt=false;";

    private LocalDevice local = null;
    private StreamConnectionNotifier server = null;
    private StreamConnection conn = null;
    private ArrayList<BluetoothClient> currentClients = new ArrayList<>();

    private boolean running = false;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public BluetoothServer() {
        try {
            init();
            running = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        serverOut("Accepting connections");
        while(running) {
            conn = null;
            try {
                conn = server.acceptAndOpen();
                BluetoothClient bc = new BluetoothClient(this, conn);
                currentClients.add(bc);
            } catch (IOException e) {
                serverOut("Connection failed");
                e.printStackTrace();
            }
        }
    }

    public void removeClient(BluetoothClient client) {
        currentClients.remove(currentClients.indexOf(client));
    }

    public void close() {
        running = false;
        currentClients.forEach(BluetoothClient::close);
        serverOut("Server stopped.");
    }

    private void init() throws IOException {
        serverOut("Turning on Discovery");
        local = LocalDevice.getLocalDevice();
        local.setDiscoverable(DiscoveryAgent.GIAC);
        serverOut("Advertising service");
        server = (StreamConnectionNotifier) Connector.open(url);
    }

    public void serverOut(String out) {
        serverOut("BluetoothServer", out);
    }

    public void serverOut(String location, String out) {
        Date now = new Date();
        String time = sdf.format(now);
        System.out.println("[" + time + "] [" + location + "]    " + out);
    }

}
