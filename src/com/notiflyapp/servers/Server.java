package com.notiflyapp.servers;

import com.notiflyapp.data.DeviceInfo;
import com.notiflyapp.data.SMS;
import com.notiflyapp.debug.ServerLog;
import com.notiflyapp.ui.Houston;
import javafx.concurrent.Task;

import java.util.ArrayList;

/**
 * Created by Brennan on 5/27/2016.
 */
public abstract class Server<T> extends Thread {

    protected ServerLog serverLog = new ServerLog();
    protected boolean running = false;

    protected ArrayList<T> connectedClients = new ArrayList<>();

    public abstract boolean hasDevice(DeviceInfo deviceInfo);

    public abstract void sendMessage(SMS smsMessage, DeviceInfo deviceInfo);

    /**
     * Send message to server log
     *
     * @param out String to be printed to server log with
     */
    protected abstract void serverOut(String out);

    /**
     * Prints the messages to the server log with a location, sender, ID TAG
     *
     * @param location  String identifying where message is coming from
     * @param out   String to be displayed in server log
     * @param log   Boolean to determine if written to log file
     */
    public void serverOut(String location, String out, boolean log) {
        serverLog.out(location, out, log);
    }

    public abstract void close();

    /**
     *
     * @return if server's main loop is running, if it is active
     */
    public boolean isRunning() { return running; }

    public void connectedDevice(T client) {
        connectedClients.add(client);
        Houston.getHandler().send(() -> Houston.getInstance().addDevice(client));
    }

}
