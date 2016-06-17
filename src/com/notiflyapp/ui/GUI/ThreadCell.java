package com.notiflyapp.ui.GUI;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.io.IOException;

/**
 * Created by Brennan on 6/17/2016.
 */
public class ThreadCell {

    private Node node;
    private Label label;
    private ImageView imageView;

    private int threadId;
    private String name;

    public ThreadCell(int threadId) {
        this.threadId = threadId;
        try {
            createNode();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createNode() throws IOException {
        node = FXMLLoader.load(getClass().getResource("/com/notiflyapp/ui/GUI/view/thread_cell.fxml"));
        label = (Label) node.lookup("#name_label");
        imageView = (ImageView) node.lookup("#image_icon");

        label.setText(getName());
    }

    public Node getNode() {
        return node;
    }

    public ThreadCell setThreadId(int threadId) {
        this.threadId = threadId;
        return this;
    }

    public int getThreadId() {
        return threadId;
    }

    public String getName() {
        //TODO retrieve name from future threadId database received from device
        return String.valueOf(threadId);
    }

}
