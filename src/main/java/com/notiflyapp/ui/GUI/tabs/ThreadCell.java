package com.notiflyapp.ui.GUI.tabs;

import com.notiflyapp.data.*;
import com.notiflyapp.data.requestframework.Request;
import com.notiflyapp.data.requestframework.RequestHandler;
import com.notiflyapp.database.Database;
import com.notiflyapp.database.DatabaseFactory;
import com.notiflyapp.database.NullResultSetException;
import com.notiflyapp.database.UnequalArraysException;
import com.notiflyapp.servers.bluetooth.BluetoothClient;
import com.sun.glass.ui.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Brennan on 6/17/2016.
 */
public class ThreadCell {

    public static final String THREAD_TYPE_SINGLE = "single";
    public static final String THREAD_TYPE_MULTIPLE = "multiple";

    private Node node;
    private Label label;
    private ImageView imageView;

    private BluetoothClient client;
    private BDeviceTab house;
    private int threadId;
    private String name;
    private ConversationThread thread;

    private ArrayList<DataObject> messages = new ArrayList<>();

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

    public void newMessage(DataObject dataObject) {
        switch (dataObject.getType()) {
            case DataObject.Type.SMS:
                messages.add(dataObject);
                break;
            case DataObject.Type.MMS:
                messages.add(dataObject);
                break;
        }
    }

    public DataObject[] getMessages() {
        if(messages.size() == 0) {
            DataObject[] msgs = retrieveMessages();
            for(DataObject msg: msgs) {
                messages.add(msg);
            }
            return msgs;
        } else {
            DataObject[] msgs = new DataObject[messages.size()];
            return messages.toArray(msgs);
        }
    }

    private DataObject[] retrieveMessages() {
        try {
            return DatabaseFactory.getMessageDatabase(client.getDeviceMac()).getMessages(threadId);
        } catch (SQLException | NullResultSetException | UnequalArraysException e) {
            e.printStackTrace();
        }
        return null;
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

    public String getThreadType() {
        if(getAddress().contains(";")) {
            return THREAD_TYPE_MULTIPLE;
        } else {
            return THREAD_TYPE_SINGLE;
        }
    }

    public String getAddress() {
        return formatOutAddress(thread.getContacts());
    }

    String formatOutAddress(Contact[] contacts) {
        StringBuilder b = new StringBuilder();
        for(int i = 0; i < contacts.length; i++) {
            String raw = contacts[i].getExtra().replace(" ", "").replace("(", "").replace(")", "").replace("-", "").replace("+", "");
            if(raw.length() == 10) {
                b.append("+1");
                b.append(raw);
            } else {
                b.append("+");
                b.append(raw);
            }
            if(i < contacts.length - 1) {
                b.append(";");
            }
        }
        return b.toString();
    }

}