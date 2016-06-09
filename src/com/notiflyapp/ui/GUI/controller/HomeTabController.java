package com.notiflyapp.ui.GUI.controller;

import com.notiflyapp.ui.Houston;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Created by Brennan on 6/7/2016.
 */
public class HomeTabController {

    @FXML private Button discoverBtn;
    @FXML private Button clearMsgBtn;

    private boolean discovering = false;

    @FXML public void initialize() {
        Houston.setHomeTabController(this);
    }

    @FXML private void toggleDiscovery(ActionEvent event) {
        System.out.println("Discovery button pushed");
        if(discovering) {
            Houston.getInstance().stopBluetoothDiscovery();
            discoverBtn.setText("Start Discovery");
            discovering = false;
        } else {
            Houston.getInstance().startBluetoothDiscovery();
            discoverBtn.setText("Stop Discovery");
            discovering = true;
        }
    }

    @FXML private void clearMessages(ActionEvent event) {
        System.out.println("Clear message button pushed");
    }

}
