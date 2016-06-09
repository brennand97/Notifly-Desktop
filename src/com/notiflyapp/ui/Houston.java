package com.notiflyapp.ui;

import com.notiflyapp.controlcenter.ServerHandler;
import com.notiflyapp.data.DataObject;
import com.notiflyapp.data.DeviceInfo;
import com.notiflyapp.data.Serial;
import com.notiflyapp.servers.bluetooth.BluetoothClient;
import com.notiflyapp.servers.bluetooth.BluetoothServer;
import com.notiflyapp.ui.GUI.controller.HomeTabController;
import com.notiflyapp.ui.GUI.controller.MainController;
import com.notiflyapp.ui.GUI.tabs.BDeviceTab;
import com.notiflyapp.ui.GUI.tabs.HomeTab;
import com.notiflyapp.ui.GUI.tabs.TabHouse;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Brennan on 6/7/2016.
 */
public class Houston {

    private static boolean GUI = true;
    private UIHandler handler = new UIHandler();

    private static ServerHandler serverHandler = new ServerHandler();
    private static BluetoothServer btServer;
    private static boolean serverActive = false;

    private MainController mainController;
    private HomeTabController homeTabController;

    private Stage primaryStage;
    private Scene primaryScene;

    private TabPane tabPane;
    private ArrayList<TabHouse> tabs = new ArrayList<>();

    private static Houston houston;

    public Houston() {

    }

    public static void initialize(Stage stage, Scene scene) {
        getInstance();
        getHandler().start();
        houston.primaryStage = stage;
        houston.primaryScene = scene;
        if(houston.tabPane == null) {
            houston.tabPane = (TabPane) houston.primaryScene.lookup("#main_tab_pane");
        }
        //Initialize homeTab
        Tab homeTab = new Tab();
        houston.tabPane.getTabs().add(homeTab);
        HomeTab home = new HomeTab(homeTab, "Home");
        houston.tabs.add(home);
    }

    public static Houston getInstance() {
        if(houston == null) {
            houston = new Houston();
        }
        return houston;
    }

    public static UIHandler getHandler() {
        return getInstance().handler;
    }

    public void close() {
        serverHandler.closeServers();
    }

    public void startBluetoothServer() {
        Runnable runnable = () -> {
            btServer = new BluetoothServer();
            btServer.start();
            serverActive = true;
            serverHandler.addServer(btServer);
        };
        (new Thread(runnable)).start();
    }

    public void startBluetoothDiscovery() {
        try {
            btServer.startDiscovery();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopBluetoothDiscovery() {
        btServer.stopDiscovery();
    }

    public static void setHomeTabController(HomeTabController htc) {
        getInstance().homeTabController = htc;
    }

    public static void setMainController(MainController mc) {
        getInstance().mainController = mc;
    }

    public void addDevice(Object object) {
        if(object instanceof BluetoothClient) {
            addBluetoothDevice((BluetoothClient) object);
        }
    }

    public void updateDevice(Object object) {
        if(object instanceof BluetoothClient) {
            updateBluetoothDevice((BluetoothClient) object);
        }
    }

    private void addBluetoothDevice(BluetoothClient client) {
        if(GUI) {
            Tab tab = new Tab();
            tabPane.getTabs().add(tab);
            BDeviceTab bdt = new BDeviceTab(tab, client);
            tabs.add(bdt);
        }
    }

    private void updateBluetoothDevice(BluetoothClient client) {
        for(TabHouse tabHouse: tabs) {
            if(tabHouse instanceof BDeviceTab) {
                if(((BDeviceTab) tabHouse).getBluetoothClient().equals(client)) {
                    tabHouse.refresh();
                }
            }
        }
    }

    public void closeBluetoothDevice(BluetoothClient client) {
        btServer.dissconnectClient(client);
    }

    public class UIHandler extends Thread {

        private ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1024);
        private boolean running = false;

        private final Object lock = new Object();

        protected UIHandler() {
        }

        public synchronized void send(Runnable runnable) {
            if(runnable != null) {
                queue.add(runnable);
                if(running) {
                    try {
                        synchronized (lock) {
                            lock.notify();
                        }
                    } catch (IllegalMonitorStateException e) {}
                }
            }
        }

        public void run() {
            System.out.println("UI Handler started");
            running = true;
            synchronized (lock) {
                while (running) {
                    if(queue.isEmpty()) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Runnable runnable = queue.poll();
                        Platform.runLater(runnable);
                    }
                }
            }
        }

    }

}
