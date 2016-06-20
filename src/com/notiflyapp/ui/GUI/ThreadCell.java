package com.notiflyapp.ui.GUI;

import com.notiflyapp.data.DataObject;
import com.notiflyapp.data.SMS;
import com.notiflyapp.database.DatabaseFactory;
import com.notiflyapp.database.NullResultSetException;
import com.notiflyapp.database.UnequalArraysException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by Brennan on 6/17/2016.
 */
public class ThreadCell {

    private Node node;
    private Label label;
    private ImageView imageView;

    private String mac;
    private int threadId;
    private String name;

    public ThreadCell(String mac, int threadId) {
        this.mac = mac;
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

    public DataObject[] getMessages() {
        try {
            return DatabaseFactory.getMessageDatabase(mac).getMessages(threadId);
        } catch (SQLException | NullResultSetException | UnequalArraysException e) {
            e.printStackTrace();
        }
        return new DataObject[0];
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
