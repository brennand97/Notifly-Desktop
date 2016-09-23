/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.ui.GUI.controller;

import com.notiflyapp.controlcenter.Houston;
import javafx.fxml.FXML;

public class MainController {

    @FXML public void initialize() {
        System.out.println("MainController Started");
        Houston.setMainController(this);
    }

}
