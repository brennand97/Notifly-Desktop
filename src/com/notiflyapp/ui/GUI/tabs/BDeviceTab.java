package com.notiflyapp.ui.GUI.tabs;

import com.notiflyapp.data.DataObject;
import com.notiflyapp.data.DeviceInfo;
import com.notiflyapp.data.MMS;
import com.notiflyapp.data.SMS;
import com.notiflyapp.database.DatabaseFactory;
import com.notiflyapp.database.NullResultSetException;
import com.notiflyapp.servers.bluetooth.BluetoothClient;
import com.notiflyapp.controlcenter.Houston;
import com.notiflyapp.ui.GUI.ThreadCell;
import com.sun.glass.ui.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Brennan on 6/8/2016.
 */
public class BDeviceTab extends TabHouse {

    private BluetoothClient client;
    private DeviceInfo deviceInfo;

    private Tab tab;
    private ListView<Node> threadView;
    private ArrayList<ThreadCell> threadCells = new ArrayList<>();
    private ListView<Node> messageView;
    private Label nameLabel;

    private ThreadCell current;
    private double smsMaxWidth;

    private static final long gracePeriod = 5000;
    private static final String defaultName = "Bluetooth Device";

    public BDeviceTab(Tab tab, BluetoothClient client) {
        super(tab, client.getDeviceName() == null ? client.getDeviceMac() == null ? defaultName : client.getDeviceMac() : client.getDeviceName());
        this.tab = tab;
        this.client = client;
        tab.setOnClosed(event -> close() );

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/notiflyapp/ui/GUI/view/device_tab.fxml"));
            Node node = loader.load();
            tab.setContent(node);

            Application.invokeLater(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                initialize();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initialize() {
        threadView = (ListView<Node>) tab.getContent().lookup("#thread_list_view");
        threadView.setOnMouseClicked(event -> selectThread(threadView.getSelectionModel().getSelectedIndex()));
        messageView = (ListView<Node>) tab.getContent().lookup("#active_conversation_message_list_view");
        smsMaxWidth = (messageView.getWidth() * 0.75);
        System.out.println("smsMaxWidth : " + smsMaxWidth);
        /*
        messageView.setOnMouseClicked(event -> {
            double tmpSmsMaxWidth = (messageView.getWidth() * 0.75);
            if(tmpSmsMaxWidth != smsMaxWidth) {
                smsMaxWidth = tmpSmsMaxWidth;
                System.out.println("smsMaxWidth : " + smsMaxWidth);
                for(Node node: messageView.getItems()) {
                    //TODO correct for double sided conversation with "gravity"
                    node.prefWidth(smsMaxWidth);
                    node.lookup("#message_text").prefWidth(smsMaxWidth);
                }
            }
        });
        */
        nameLabel = (Label) tab.getContent().lookup("#active_conversation_title_bar_title");
        try {
            int[] threadIds = DatabaseFactory.getMessageDatabase(client.getDeviceMac()).getThreadIds();
            for(int i: threadIds) {
                addThreadCell(i);
            }
            if(threadIds.length > 0) {
                selectThread(0);
            }
        } catch (SQLException | NullResultSetException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void addThreadCell(int threadId) {
        for(ThreadCell cell: threadCells) {
            if(cell.getThreadId() == threadId) {
                return;
            }
        }
        ThreadCell cell = new ThreadCell(deviceInfo.getDeviceMac(), threadId);
        threadCells.add(cell);
        threadView.getItems().add(cell.getNode());
    }

    private void selectThread(int index) {
        //TODO change threadCells to a RearrangeableArrayList (Needs to be created) so that the index list always makes that of the listView
        ThreadCell tmpCurrent = threadCells.get(index);
        if(!tmpCurrent.equals(current)) {
            current = tmpCurrent;
            clearMessageListView();
            nameLabel.setText(current.getName());
            DataObject[] messages = current.getMessages();
            for(DataObject msg: messages) {
                switch (msg.getType()) {
                    case SMS:
                        newMessage((SMS) msg);
                        break;
                    case MMS:
                        newMessage((MMS) msg);
                        break;
                }
            }
        }
    }

    private void clearMessageListView() {
        messageView.getItems().clear();
    }

    @Override
    public void refresh() {
        deviceInfo = client.getDeviceInfo();
        setTitle(client.getDeviceName() == null ? client.getDeviceMac() == null ? defaultName : client.getDeviceMac() : client.getDeviceName());
        if(!client.isConnected()) {
            Houston.getHandler().send(() -> {
                try {
                    Thread.sleep(gracePeriod);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(!this.getBluetoothClient().isConnected()) {
                    Houston.getInstance().removeTab(this);
                }
            });
        }
    }

    public void setBluetoothClient(BluetoothClient client) {
        this.client = client;
    }

    public BluetoothClient getBluetoothClient() {
        return client;
    }

    public void close() {
        Houston.getHandler().send(() -> {
            Houston.getInstance().closeBluetoothDevice(client);
            Houston.getInstance().removeTab(this);
        });
    }

    public void handleNewMessage(DataObject object) {
        switch (object.getType()) {
            case SMS:
                SMS sms = (SMS) object;
                if(sms.getThreadId() == current.getThreadId()) {
                    newMessage(sms);
                } else {
                    addThreadCell(sms.getThreadId());
                    //TODO check to see if newest message and then send thread_cell to top of the list
                }
                break;
            case MMS:

                break;
        }
    }

    public void newMessage(SMS sms) {
        try {
            Node node = FXMLLoader.load(getClass().getResource("/com/notiflyapp/ui/GUI/view/sms_cell.fxml"));
            node.prefWidth(smsMaxWidth);
            TextFlow textFlow = (TextFlow) node.lookup("#message_text");
            textFlow.prefWidthProperty().set(smsMaxWidth);
            textFlow.setTextAlignment(TextAlignment.LEFT);
            Text text = new Text();
            text.prefWidth(smsMaxWidth);
            text.setText(sms.getBody());
            text.setTextAlignment(TextAlignment.LEFT);
            textFlow.getChildren().add(text);
            messageView.getItems().add(node);
            messageView.scrollTo(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void newMessage(MMS mms) {

    }

}
