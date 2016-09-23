/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.ui.GUI.tabs;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;

import java.io.IOException;

/**
 * Created by Brennan on 6/8/2016.
 */
public class HomeTab extends TabHouse {

    public HomeTab(Tab tab, String title) {
        super(tab, title);
        try {
            tab.setClosable(false);
            tab.setContent(FXMLLoader.load(getClass().getResource("/com/notiflyapp/ui/GUI/fxml/home_screen.fxml")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void refresh() {
        //Maybe do something at a latter date
    }

    @Override
    public void handleResize(double newSceneWidth) {

    }

}
