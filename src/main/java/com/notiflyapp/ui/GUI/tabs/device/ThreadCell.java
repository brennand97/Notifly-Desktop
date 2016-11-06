/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.ui.GUI.tabs.device;

import com.notiflyapp.data.*;
import com.notiflyapp.data.requestframework.Request;
import com.notiflyapp.data.requestframework.RequestHandler;
import com.notiflyapp.servers.bluetooth.BluetoothClient;
import com.notiflyapp.tools.CompletionCallback;
import com.sun.glass.ui.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Brennan on 6/17/2016.
 */
public class ThreadCell {

    static final String THREAD_TYPE_SINGLE = "single";
    static final String THREAD_TYPE_MULTIPLE = "multiple";
    static final String DATE_FORMAT_12 = "MM/dd/yyyy, hh:mm:ss a";
    static final String DATE_FORMAT_24 = "MM/dd/yyyy, HH:mm:ss";

    static boolean MILITARY_TIME = false;

    private Node node;
    private Label nameLabel;
    private Label dateLabel;
    private ImageView imageView;

    private BluetoothClient client;
    private DeviceTab house;
    private int threadId;
    private String name;
    private ConversationThread thread;
    private double scrollPoint = 1.0;
    private double maxWidth;

    private ArrayList<DataObject> messages = new ArrayList<>();
    private long mostRecent = 0L;

    ThreadCell(BluetoothClient client, DeviceTab house, int threadId, double maxWidth, CompletionCallback<ThreadCell> callback) {
        this.client = client;
        this.house = house;
        this.threadId = threadId;
        this.maxWidth = maxWidth;
        try {
            createNode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        retrieveContact(callback);

        /*
        try {
            DataObject[] output = DatabaseFactory.getMessageDatabase(client.getDeviceMac()).getMessages(threadId);
            for(DataObject o: output) {
                addMessage(o);
                //System.out.println(o);
            }
        } catch (SQLException | NullResultSetException | UnequalArraysException e) {
            e.printStackTrace();
        }
        */
    }

    private void createNode() throws IOException {
        node = FXMLLoader.load(getClass().getResource("/com/notiflyapp/ui/GUI/fxml/thread_cell.fxml"));
        nameLabel = (Label) node.lookup("#name_label");
        imageView = (ImageView) node.lookup("#image_icon");
        dateLabel = (Label) node.lookup("#date_label");

        nameLabel.setText(getName());
        if(mostRecent != 0) {
            if(MILITARY_TIME) {
                dateLabel.setText((new SimpleDateFormat(DATE_FORMAT_24)).format(new Date(mostRecent)));
            } else {
                dateLabel.setText((new SimpleDateFormat(DATE_FORMAT_12)).format(new Date(mostRecent)));
            }
        } else {
            dateLabel.setText("");
        }
        node.maxWidth(maxWidth);

    }

    public DataObject[] getMessages() {
        DataObject[] msgs = new DataObject[messages.size()];
        return messages.toArray(msgs);
    }

    public void callForMessages(long time, int count) {
        Request request = new Request();
        request.putBody(RequestHandler.RequestCode.RETRIEVE_PREVIOUS_SMS);
        request.putItem(RequestHandler.RequestCode.EXTRA_THREAD_ID, new DataString(String.valueOf(this.threadId)));
        request.putItem(RequestHandler.RequestCode.EXTRA_RETRIEVE_PREVIOUS_SMS_START_TIME, new DataString(String.valueOf(time)));
        request.putItem(RequestHandler.RequestCode.EXTRA_RETRIEVE_PREVIOUS_SMS_MESSAGE_COUNT, new DataString(String.valueOf(count)));
        RequestHandler.getInstance().sendRequest(client, request, null);
    }

    void addMessage(DataObject msg) {
        if(!messages.contains(msg)) {
            messages.add(msg);
            long date = 0;
            switch (msg.getType()) {
                case DataObject.Type.SMS:
                    date = ((SMS) msg).getDate();
                    break;
                case DataObject.Type.MMS:
                    date = ((MMS) msg).getDate();
                    break;
            }
            if(date > mostRecent) {
                mostRecent = date;
                if(MILITARY_TIME) {
                    dateLabel.setText((new SimpleDateFormat(DATE_FORMAT_24)).format(new Date(mostRecent)));
                } else {
                    dateLabel.setText((new SimpleDateFormat(DATE_FORMAT_12)).format(new Date(mostRecent)));
                }
            }
        }
    }

    void clearMessages() {
        messages.clear();
    }

    long getMostRecent() {
        return mostRecent;
    }

    Node getNode() {
        if(node == null) {
            try {
                createNode();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return node;
    }

    int getThreadId() {
        return threadId;
    }

    private void retrieveContact(CompletionCallback<ThreadCell> compCall) {
        Request request = new Request();
        request.putBody(RequestHandler.RequestCode.CONTACT_BY_THREAD_ID);
        request.putExtra(UUID.randomUUID());
        request.putRequestValue(String.valueOf(threadId));
        RequestHandler.ResponseCallback callback = (request1, response) -> {
            thread = (ConversationThread) response.getItem(RequestHandler.RequestCode.EXTRA_CONTACT_BY_THREAD_ID_THREAD);
            this.updateContact();
            if(compCall != null) {
                compCall.complete(ThreadCell.this);
            }
        };
        RequestHandler.getInstance().sendRequest(client, request, callback);
    }

    private void updateContact() {
        Contact[] contacts = thread.getContacts();
        if(contacts.length > 1) {
            StringBuilder b = new StringBuilder();
            for(int i = 0; i < contacts.length; i++) {
                if(contacts[i].getBody() == null) {
                    b.append(contacts[i].getExtra());
                } else {
                    b.append(contacts[i].getBody());
                }
                if(i < contacts.length - 1) {
                    b.append(", ");
                }
            }
            name = b.toString();
        } else if(contacts.length == 1){
            if(contacts[0].getBody() == null) {
                name = contacts[0].getExtra();
            } else {
                name = contacts[0].getBody();
            }
        } else {
            name = String.valueOf(threadId);
        }
        if(nameLabel == null) {
            try {
                createNode();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Application.invokeLater(() -> {
            nameLabel.setText(getName());
            house.updateCurrentNameLabel();
        });
    }

    String contactAddresses(Contact[] contacts) {
        if(contacts.length > 1) {
            StringBuilder b = new StringBuilder();
            for(int i = 0; i < contacts.length; i++) {
                b.append(contacts[i].getExtra());
                if(i < contacts.length - 1) {
                    b.append(";");
                }
            }
            return b.toString();
        } else if(contacts.length == 1){
            return contacts[0].getExtra();
        } else {
            return String.valueOf(threadId);
        }
    }

    String getName() {
        if (name == null) {
            return String.valueOf(threadId);
        } else {
            return name;
        }
    }

    String getThreadType() {
        if(getAddress().contains(";")) {
            return THREAD_TYPE_MULTIPLE;
        } else {
            return THREAD_TYPE_SINGLE;
        }
    }

    String getAddress() {
        if(thread != null) {
            return formatOutAddress(thread.getContacts());
        }
        return null;
    }

    Contact[] getContacts() {
        if(thread != null) {
            return thread.getContacts();
        }
        return null;
    }

    private String formatOutAddress(Contact[] contacts) {
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

    double getScrollPoint() {
        return scrollPoint;
    }

    void setScrollPoint(double scrollPoint) {
        this.scrollPoint = scrollPoint;
    }

    void updateMaxWidth(double newMaxWidth) {
        this.maxWidth = newMaxWidth;
        node.maxWidth(maxWidth);
        //System.out.println(maxWidth);
    }

}