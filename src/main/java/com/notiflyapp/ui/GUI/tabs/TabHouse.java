/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.ui.GUI.tabs;

import javafx.scene.control.Tab;

/**
 * Created by Brennan on 6/8/2016.
 */
public abstract class TabHouse {

    private Tab tab;
    private String title;

    public TabHouse(Tab tab, String title) {
        tab.setText(title);
        this.tab = tab;
        this.title = title;
    }

    public abstract void refresh();

    public abstract void handleResize(double newSceneWidth);

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        tab.setText(title);
        this.title = title;
    }

    public Tab getTab() {
        return tab;
    }

}
