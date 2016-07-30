package com.notiflyapp.ui.GUI.tabs.device;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Brennan on 6/17/2016.
 */
public class ThreadCell {

    public static final String THREAD_TYPE_SINGLE = "single";
    public static final String THREAD_TYPE_MULTIPLE = "multiple";
    protected static final String DATE_FORMAT_12 = "MM/dd/yyyy, hh:mm:ss a";
    protected static final String DATE_FORMAT_24 = "MM/dd/yyyy, HH:mm:ss";

    protected static boolean MILITARY_TIME = false;

    private Node node;
    private Label nameLabel;
    private Label dateLabel;
    private ImageView imageView;

    private BluetoothClient client;
    private BDeviceTab house;
    private int threadId;
    private String name;
    private ConversationThread thread;
    private double scrollPoint = 1.0;
    private double maxWidth;

    private ArrayList<DataObject> messages = new ArrayList<>();
    private long mostRecent = 0L;

    public ThreadCell(BluetoothClient client, BDeviceTab house, int threadId, double maxWidth) {
        this.client = client;
        this.house = house;
        this.threadId = threadId;
        this.maxWidth = maxWidth;
        retrieveContact();
        try {
            createNode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getMessages();
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
        if(messages.size() != 0) {
            DataObject[] msgs = new DataObject[messages.size()];
            return messages.toArray(msgs);
        }
        try {
            DataObject[] output = DatabaseFactory.getMessageDatabase(client.getDeviceMac()).getMessages(threadId);
            for(DataObject o: output) {
                addMessage(o);
            }
            return output;
        } catch (SQLException | NullResultSetException | UnequalArraysException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addMessage(DataObject msg) {
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

    public long getMostRecent() {
        return mostRecent;
    }

    public Node getNode() {
        if(node == null) {
            try {
                createNode();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        try {
            thread = DatabaseFactory.getThreadDatabase(client.getDeviceMac()).queryThread(threadId);
            if(thread != null) {
                handleContact(thread);
                return;
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
            name = b.toString();
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
        if(thread != null) {
            return formatOutAddress(thread.getContacts());
        }
        return null;
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

    public double getScrollPoint() {
        return scrollPoint;
    }

    public void setScrollPoint(double scrollPoint) {
        this.scrollPoint = scrollPoint;
    }

    public void updateMaxWidth(double newMaxWidth) {
        this.maxWidth = newMaxWidth;
        node.maxWidth(maxWidth);
        System.out.println(maxWidth);
    }

}