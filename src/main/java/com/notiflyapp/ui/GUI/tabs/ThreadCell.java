package com.notiflyapp.ui.GUI.tabs;

import com.notiflyapp.data.*;
import com.notiflyapp.data.requestframework.Request;
import com.notiflyapp.data.requestframework.RequestHandler;
import com.notiflyapp.database.DatabaseFactory;
import com.notiflyapp.database.NullResultSetException;
import com.notiflyapp.database.UnequalArraysException;
import com.notiflyapp.servers.bluetooth.BluetoothClient;
import com.sun.glass.ui.Application;
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
    private BDeviceTab house;
    private int threadId;
    private String name;
    private ConversationThread thread;

    public ThreadCell(BluetoothClient client, BDeviceTab house, int threadId) {
        this.client = client;
        this.house = house;
        this.threadId = threadId;
        retrieveContact();
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
        try {
            createNode();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        try {
            thread = DatabaseFactory.getThreadDatabase(client.getDeviceMac()).queryThread(threadId);
            if(thread != null) {
                handleContact(thread);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(thread == null) {
            Request request = new Request();
            request.putBody(RequestHandler.RequestCode.CONTACT_BY_THREAD_ID);
            request.putExtra(UUID.randomUUID());
            request.putRequestValue(String.valueOf(threadId));
            RequestHandler.ResponseCallback callback = (request1, response) -> {
                thread = (ConversationThread) response.getItem(RequestHandler.RequestCode.EXTRA_CONTACT_BY_THREAD_ID_THREAD);
                try {
                    handleContact(thread);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            };
            RequestHandler.getInstance().sendRequest(client, request, callback);
        }

    }

    private void handleContact(ConversationThread thread) throws SQLException {
        this.thread = thread;
        DatabaseFactory.getThreadDatabase(client.getDeviceMac()).nonDuplicateInsert(thread);
        Contact[] contacts = thread.getContacts();
        if(contacts.length > 1) {
            StringBuilder b = new StringBuilder();
            for(int i = 0; i < contacts.length; i++) {
                if(contacts[i].getBody() != null) {
                    //TODO only insert if has actual contactId;
                    DatabaseFactory.getContactDatabase(client.getDeviceMac()).nonDuplicateInsert(contacts[i]);
                }
                if(contacts[i].getBody() == null) {
                    b.append(contacts[i].getExtra());
                } else {
                    b.append(contacts[i].getBody());
                }
                if(i < contacts.length - 1) {
                    b.append(", ");
                }
            }
        } else if(contacts.length != 0){
            if(contacts[0].getBody() != null) {
                //TODO only insert if has actual contactId;
                DatabaseFactory.getContactDatabase(client.getDeviceMac()).nonDuplicateInsert(contacts[0]);
            }
            if(contacts[0].getBody() == null) {
                name = contacts[0].getExtra();
            } else {
                name = contacts[0].getBody();
            }
        } else {
            name = String.valueOf(threadId);
        }
        Application.invokeLater(() -> house.updateName(this));
    }

    public String getName() {
        if (name == null) {
            return String.valueOf(threadId);
        } else {
            return name;
        }
    }

}