package com.notiflyapp.ui.GUI;

import com.notiflyapp.data.*;
import com.notiflyapp.data.requestframework.Request;
import com.notiflyapp.data.requestframework.RequestHandler;
import com.notiflyapp.database.DatabaseFactory;
import com.notiflyapp.database.NullResultSetException;
import com.notiflyapp.database.UnequalArraysException;
import com.notiflyapp.servers.bluetooth.BluetoothClient;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by Brennan on 6/17/2016.
 */
public class ThreadCell {

    private Node node;
    private Label label;
    private ImageView imageView;

    private BluetoothClient client;
    private int threadId;
    private String name;

    public ThreadCell(BluetoothClient client, int threadId) {
        this.client = client;
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
            return DatabaseFactory.getMessageDatabase(client.getDeviceMac()).getMessages(threadId);
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

    private void retrieveContact() {
        //TODO retrieve name from future threadId database received from device
        Request request = new Request();
        request.putBody(RequestHandler.RequestCode.CONTACT_BY_THREAD_ID);
        request.putExtra(UUID.randomUUID());
        RequestHandler.RequestCallback callback = response -> {

        };
        RequestHandler.getInstance().sendRequest(client, request, callback);
    }

    public String getName() {
        if (name == null) {
            return String.valueOf(threadId);
        } else {
            return name;
        }
    }

}