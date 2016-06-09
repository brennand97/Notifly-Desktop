package com.notiflyapp.ui.GUI.controller;

import com.notiflyapp.ui.Houston;
import javafx.fxml.FXML;

public class MainController {

    @FXML public void initialize() {
        System.out.println("MainController Started");
        Houston.setMainController(this);
    }

}
