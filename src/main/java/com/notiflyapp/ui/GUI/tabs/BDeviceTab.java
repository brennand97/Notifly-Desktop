package com.notiflyapp.ui.GUI.tabs;

import com.notiflyapp.data.*;
import com.notiflyapp.data.requestframework.Request;
import com.notiflyapp.data.requestframework.RequestHandler;
import com.notiflyapp.database.DatabaseFactory;
import com.notiflyapp.database.NullResultSetException;
import com.notiflyapp.database.UnequalArraysException;
import com.notiflyapp.servers.bluetooth.BluetoothClient;
import com.notiflyapp.controlcenter.Houston;
import com.sun.glass.ui.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private ArrayList<DataObject> messages = new ArrayList<>();
    private Label nameLabel;
    private Button sendButton;
    private TextArea textArea;

    private ThreadCell current;
    private double smsMaxWidth;

    private static final long gracePeriod = 5000;
    private static final int messageFontSize = 16;
    private static final String defaultName = "Bluetooth Device";
    private static final String creatorName = "com.notiflyapp.ui.GUI.tabs.BDeviceTab";

    private final URL SMS_NODE_URL = getClass().getResource("/com/notiflyapp/ui/GUI/view/sms_cell.fxml");

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
        threadView.setOnMouseClicked(event ->  {
            int index = threadView.getSelectionModel().getSelectedIndex();
            if(index != -1) {
                selectThread(threadView.getSelectionModel().getSelectedIndex());
            }
        });
        messageView = (ListView<Node>) tab.getContent().lookup("#active_conversation_message_list_view");
        smsMaxWidth = (messageView.getWidth() * 0.75);
        nameLabel = (Label) tab.getContent().lookup("#active_conversation_title_bar_title");
        textArea = (TextArea) tab.getContent().lookup("#message_entry");
        textArea.setFont(new Font(messageFontSize));
        sendButton = (Button) tab.getContent().lookup("#send_button");
        sendButton.setOnAction(e -> {
            if(current.getThreadType().equals(ThreadCell.THREAD_TYPE_SINGLE)) {
                SMS sms = new SMS();
                sms.setBody(textArea.getText());
                sms.setAddress(current.getAddress());
                sms.setCreator(creatorName);
                sms.setDateSent(System.currentTimeMillis());
                sms.setRead(true);
                sms.setSubscriptionId(1);
                sms.setThreadId(current.getThreadId());
                textArea.clear();
                sendMessage(sms);
                handleNewMessage(sms);
            } else {
                //TODO handle creation of MMS message
            }
        });
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

    @Override
    public void handleResize(double newSceneWidth) {
        double tmpSmsMaxWidth = (messageView.getWidth() * 0.75);
        if(tmpSmsMaxWidth != smsMaxWidth) {
            smsMaxWidth = tmpSmsMaxWidth;
            for(Node node: messageView.getItems()) {
                //TODO correct for double sided conversation with "gravity"
                ((TextFlow) node.lookup("#message_text")).maxWidthProperty().set(smsMaxWidth);
            }
        }
    }

    private void addThreadCell(int threadId) {
        for(ThreadCell cell: threadCells) {
            if(cell.getThreadId() == threadId) {
                return;
            }
        }
        ThreadCell cell = new ThreadCell(client, this, threadId);
        threadCells.add(cell);
        threadView.getItems().add(cell.getNode());

        threadCells.sort(new ThreadComparator());
        threadView.getItems().sort(new ThreadNodeComparator());
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
                handleNewMessage(msg);
            }
        }
    }

    protected void updateName(ThreadCell cell) {
        if(current.equals(cell)) {
            nameLabel.setText(cell.getName());
        }
        for(int i = 0; i < threadCells.size(); i++) {
            if(threadCells.get(i).getThreadId() == cell.getThreadId()) {
                threadView.getItems().remove(i);
                threadView.getItems().add(i, cell.getNode());

            }
        }
    }

    private void clearMessageListView() {
        messages.clear();
        messageView.getItems().clear();
    }

    private void clearThreadListView() { threadView.getItems().clear(); }

    public void clearMessages() {
        clearMessageListView();
        clearThreadListView();
        nameLabel.setText("Start Conversation");
    }

    @Override
    public void refresh() {
        deviceInfo = client.getDeviceInfo();
        setTitle(client.getDeviceName() == null ? client.getDeviceMac() == null ? defaultName : client.getDeviceMac() : client.getDeviceName());
        if(!client.isConnected()) {
            Runnable runnable = () -> {
                try {
                    Thread.sleep(gracePeriod);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(!this.getBluetoothClient().isConnected()) {
                    Houston.getHandler().send(() -> Houston.getInstance().removeTab(this));
                }
            };
            (new Thread(runnable)).start();
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
            case DataObject.Type.SMS:
                SMS sms = (SMS) object;
                if(sms.getThreadId() == current.getThreadId()) {
                    for(DataObject s: messages) {
                        if(s.equals(sms)) {
                            break;
                        }
                    }
                    messages.add(sms);
                    current.addMessage(sms);
                    newMessage(sms);
                } else {
                    addThreadCell(sms.getThreadId());
                    for(ThreadCell cell: threadCells) {
                        if(cell.getThreadId() == sms.getThreadId()) {
                            cell.addMessage(sms);
                        }
                    }
                }

                break;
            case DataObject.Type.MMS:

                break;
        }

        if(ThreadCell.MILITARY_TIME) {
            ((Label) threadView.getItems().get(threadCells.indexOf(current)).lookup("#date_label")).setText((new SimpleDateFormat(ThreadCell.DATE_FORMAT_24)).format(new Date(current.getMostRecent())));
        } else {
            ((Label) threadView.getItems().get(threadCells.indexOf(current)).lookup("#date_label")).setText((new SimpleDateFormat(ThreadCell.DATE_FORMAT_12)).format(new Date(current.getMostRecent())));
        }

        threadCells.sort(new ThreadComparator());
        threadView.getItems().sort(new ThreadNodeComparator());
        threadView.refresh();

    }

    public void newMessage(SMS sms) {
        try {
            Node node = FXMLLoader.load(SMS_NODE_URL);
            if(sms.getAddress() != null) {
                if(sms.getAddress().equals(current.getAddress())) {
                    node.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                }
            } else {
                node.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            }
            TextFlow textFlow = (TextFlow) node.lookup("#message_text");
            textFlow.setTextAlignment(TextAlignment.LEFT);
            textFlow.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            textFlow.maxWidthProperty().set(smsMaxWidth);
            Text text = new Text();
            text.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            text.setTextAlignment(TextAlignment.LEFT);
            text.setFont(new Font(messageFontSize));
            text.setText(sms.getBody());
            textFlow.getChildren().add(text);
            messageView.getItems().add(node);
            messageView.scrollTo(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void newMessage(MMS mms) {

    }

    public void sendMessage(DataObject object) {
        Request request = new Request();
        switch (object.getType()) {
            case DataObject.Type.SMS:
                final int index;
                int index1;
                try {
                    index1 = DatabaseFactory.getMessageDatabase(client.getDeviceMac()).nonDuplicateInsert((SMS) object);
                } catch (UnequalArraysException | NullResultSetException | SQLException e) {
                    index1 = -1;
                    e.printStackTrace();
                }
                index = index1;
                request.putBody(RequestHandler.RequestCode.SEND_SMS);
                request.putItem(RequestHandler.RequestCode.EXTRA_SEND_SMS_SMSOBJECT, object);
                RequestHandler.getInstance().sendRequest(client, request, (request1, response) -> {
                    if(response.getRequestValue().equals(RequestHandler.RequestCode.CONFIRMATION_SEND_SMS_SENT)) {
                        try {
                            if(index != -1) {
                                DatabaseFactory.getMessageDatabase(client.getDeviceMac()).update(index, (SMS) response.getItem(RequestHandler.RequestCode.EXTRA_SEND_SMS_SMSOBJECT));
                            }
                        } catch (UnequalArraysException | SQLException e) {
                            e.printStackTrace();
                        }
                    } else if(response.getRequestValue().equals(RequestHandler.RequestCode.CONFIRMATION_SEND_SMS_FAILED)) {
                        //TODO display that message failed to send and try to resend on user request
                    }
                });
                break;
            case DataObject.Type.MMS:
                //TODO handle sending MMS message
                break;
        }
    }

}