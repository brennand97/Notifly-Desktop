package com.notiflyapp.ui.GUI.tabs;

import com.notiflyapp.data.DeviceInfo;
import com.notiflyapp.servers.bluetooth.BluetoothClient;
import com.notiflyapp.ui.Houston;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;

import java.io.IOException;

/**
 * Created by Brennan on 6/8/2016.
 */
public class BDeviceTab extends TabHouse {

    private BluetoothClient client;
    private DeviceInfo deviceInfo;

    public BDeviceTab(Tab tab, BluetoothClient client) {
        super(tab, client.getDeviceName() == null ? client.getDeviceMac() == null ? "Device 1" : client.getDeviceMac() : client.getDeviceName());
        this.client = client;
        deviceInfo = client.getDeviceInfo();
        tab.setOnClosed(event -> {
            close();
        });

        try {
            tab.setContent(FXMLLoader.load(getClass().getResource("/com/notiflyapp/ui/GUI/view/device_tab.fxml")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void refresh() {
        setTitle(client.getDeviceName() == null ? client.getDeviceMac() == null ? "Device 1" : client.getDeviceMac() : client.getDeviceName());
    }

    public BluetoothClient getBluetoothClient() {
        return client;
    }

    public void close() {
        Houston.getHandler().send(() -> Houston.getInstance().closeBluetoothDevice(client));
    }

}
