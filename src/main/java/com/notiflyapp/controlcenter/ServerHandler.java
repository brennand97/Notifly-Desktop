/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.controlcenter;

import com.notiflyapp.data.DeviceInfo;
import com.notiflyapp.data.SMS;
import com.notiflyapp.servers.Server;

import java.util.ArrayList;

/**
 * Created by Brennan on 5/27/2016.
 */
public class ServerHandler {

    private ArrayList<Server> servers = new ArrayList<>();

    public ServerHandler(Server[] array) {
        for(Server server: array) {
            servers.add(server);
        }
    }

    public  ServerHandler() {}

    public void addServer(Server server) {
        servers.add(server);
    }

    public void closeServers() {
        servers.stream().forEach(server -> server.close());
        servers.clear();
    }

    public void sendSMSMessage(SMS message, DeviceInfo deviceInfo) {
        servers.stream().filter(server -> server.hasDevice(deviceInfo)).forEach(server -> {
            server.sendMessage(message, deviceInfo);
        });
    }

}
