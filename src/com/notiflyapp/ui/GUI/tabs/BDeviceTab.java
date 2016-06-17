package com.notiflyapp.ui.GUI.tabs;

import com.notiflyapp.data.DeviceInfo;
import com.notiflyapp.data.SMS;
import com.notiflyapp.servers.bluetooth.BluetoothClient;
import com.notiflyapp.controlcenter.Houston;
import com.notiflyapp.ui.GUI.ThreadCell;
import com.sun.glass.ui.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Brennan on 6/8/2016.
 */
public class BDeviceTab extends TabHouse {

    private BluetoothClient client;
    private DeviceInfo deviceInfo;

    private Tab tab;
    private ListView<Node> threadView;
    private ArrayList<ThreadCell> threadCells = new ArrayList<>();

    private static final long gracePeriod = 5000;
    private static final String defaultName = "Bluetooth Device";

    public BDeviceTab(Tab tab, BluetoothClient client) {
        super(tab, client.getDeviceName() == null ? client.getDeviceMac() == null ? defaultName : client.getDeviceMac() : client.getDeviceName());
        this.tab = tab;
        this.client = client;
        deviceInfo = client.getDeviceInfo();
        tab.setOnClosed(event -> close() );

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/notiflyapp/ui/GUI/view/device_tab.fxml"));
            Node node = loader.load();
            tab.setContent(node);

            Application.invokeLater(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                initialize();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialize() {
        threadView = (ListView<Node>) tab.getContent().lookup("#thread_list_view");
        System.out.println(threadView == null);
        for(int i = 0; i < 25; i++) {
            addThreadCell(i);
        }
    }

    public void addThreadCell(int threadId) {
        for(ThreadCell cell: threadCells) {
            if(cell.getThreadId() == threadId) {
                return;
            }
        }
        ThreadCell cell = new ThreadCell(threadId);
        threadCells.add(cell);
        threadView.getItems().add(cell.getNode());
    }

    @Override
    public void refresh() {
        setTitle(client.getDeviceName() == null ? client.getDeviceMac() == null ? defaultName : client.getDeviceMac() : client.getDeviceName());
        if(!client.isConnected()) {
            Houston.getHandler().send(() -> {
                try {
                    Thread.sleep(gracePeriod);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(!this.getBluetoothClient().isConnected()) {
                    Houston.getInstance().removeTab(this);
                }
            });
        }
    }

    public void setBluetoothClient(BluetoothClient client) {
        this.client = client;
    }

    public BluetoothClient getBluetoothClient() {
        return client;
    }

    public void close() {
        Houston.getHandler().send(() -> {
            Houston.getInstance().closeBluetoothDevice(client);
            Houston.getInstance().removeTab(this);
        });
    }

    public void newMessage(SMS sms) {

    }

}
