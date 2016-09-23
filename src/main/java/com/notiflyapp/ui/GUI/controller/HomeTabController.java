/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.ui.GUI.controller;

import com.notiflyapp.controlcenter.Houston;
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
            discoverBtn.getStyleClass().clear();
            discoverBtn.getStyleClass().add("home-menu-button");
            discoverBtn.getStyleClass().add("button");
            Houston.getInstance().stopBluetoothDiscovery();
            discovering = false;
        } else {
            discoverBtn.getStyleClass().clear();
            discoverBtn.getStyleClass().add("home-menu-button-toggled");
            Houston.getInstance().startBluetoothDiscovery();
            discovering = true;
        }
    }

    @FXML private void clearMessages(ActionEvent event) {
        System.out.println("Clear messages");
        Houston.getHandler().send(() -> Houston.getInstance().clearMessages());
    }

}
