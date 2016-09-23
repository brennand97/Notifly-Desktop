/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.ui.GUI.tabs.device;

import com.notiflyapp.data.*;
import com.notiflyapp.data.requestframework.Request;
import com.notiflyapp.data.requestframework.RequestHandler;
import com.notiflyapp.database.DatabaseFactory;
import com.notiflyapp.database.NullResultSetException;
import com.notiflyapp.database.UnequalArraysException;
import com.notiflyapp.servers.bluetooth.BluetoothClient;
import com.notiflyapp.controlcenter.Houston;
import com.notiflyapp.ui.GUI.tabs.TabHouse;
import com.sun.glass.ui.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
    private VBox messageView;
    private ScrollPane messageScroll;
    private ArrayList<Message> messages = new ArrayList<>();
    private Label nameLabel;
    private Button sendButton;
    private TextArea textArea;

    private ThreadCell current;
    private double smsMaxWidth;
    private double threadMaxWidth;

    private static final long gracePeriod = 5000;
    private static final int messageFontSize = 16;
    private static final String defaultName = "Bluetooth Device";
    private static final String creatorName = "com.notiflyapp.ui.GUI.tabs.device.BDeviceTab";

    private final URL SMS_NODE_URL = getClass().getResource("/com/notiflyapp/ui/GUI/fxml/sms_cell.fxml");

    public BDeviceTab(Tab tab, BluetoothClient client) {
        super(tab, client.getDeviceName() == null ? client.getDeviceMac() == null ? defaultName : client.getDeviceMac() : client.getDeviceName());
        this.tab = tab;
        this.client = client;
        tab.setOnClosed(event -> close() );

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/notiflyapp/ui/GUI/fxml/device_tab.fxml"));
            Node node = loader.load();
            tab.setContent(node);
            tab.getContent().getScene().getRoot().applyCss();

            initialize();

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
            if(event.getButton().equals(MouseButton.PRIMARY)) {
                if(event.getClickCount() == 2) {
                    Application.invokeLater(() -> messageScroll.setVvalue(1.0));
                }
            }
        });
        threadView.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> {
            Application.invokeLater(() -> {
                threadMaxWidth = newSceneWidth.doubleValue();
                for(ThreadCell cell : threadCells) {
                    cell.updateMaxWidth(threadMaxWidth);
                }
                for(Node node: threadView.getItems()) {
                    node.maxWidth(threadMaxWidth);
                }
            });
        });
        threadMaxWidth = 220;
        threadView.setPrefWidth(threadMaxWidth);
        messageView = (VBox) tab.getContent().lookup("#active_conversation_message_vbox");
        messageView.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> {
            Application.invokeLater(() -> handleResize(newSceneWidth.doubleValue()));
        });
        messageScroll = (ScrollPane) tab.getContent().lookup("#active_conversation_message_scroll_pane");
        messageScroll.addEventFilter(ScrollEvent.ANY, event -> {
            //System.out.println(messageScroll.getVvalue());
        });
        smsMaxWidth = (messageView.getWidth() * 0.75);
        nameLabel = (Label) tab.getContent().lookup("#active_conversation_title_bar_title");
        textArea = (TextArea) tab.getContent().lookup("#message_entry");
        textArea.setFont(new Font(messageFontSize));
        sendButton = (Button) tab.getContent().lookup("#send_button");
        sendButton.setOnAction(e -> {
            if(current.getThreadType().equals(ThreadCell.THREAD_TYPE_SINGLE)) {
                if(textArea.getText().trim().equals("")) {
                    return;
                }
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
                Application.invokeLater(() -> messageScroll.setVvalue(1.0));
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
            } else {
                current = null;
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
            for(Node node: messageView.getChildren()) {
                ((Label) node.lookup("#message_text")).maxWidthProperty().set(smsMaxWidth);
            }
        }
    }

    private void addThreadCell(int threadId) {
        for(ThreadCell cell: threadCells) {
            if(cell.getThreadId() == threadId) {
                return;
            }
        }
        ThreadCell cell = new ThreadCell(client, this, threadId, threadMaxWidth);
        Node node = cell.getNode();

        threadCells.add(cell);
        threadView.getItems().add(node);
        threadCells.sort(new ThreadComparator());
        threadView.getItems().sort(new ThreadNodeComparator());
    }

    private void selectThread(int index) {
        ThreadCell tmpCurrent = threadCells.get(index);
        if(!tmpCurrent.equals(current)) {
            threadView.getSelectionModel().select(index);
            if(messageScroll != null && current != null) {
                current.setScrollPoint(messageScroll.getVvalue());
            }
            current = tmpCurrent;
            clearMessageListView();
            nameLabel.setText(current.getName());
            DataObject[] messages = current.getMessages();
            for(DataObject msg: messages) {
                handleNewMessage(msg, false);
            }
            messageScroll.setVvalue(current.getScrollPoint());
            Application.invokeLater(() -> messageScroll.setVvalue(current.getScrollPoint()));
        }
    }

    protected void updateCurrentNameLabel() {
        if(current != null) {
            nameLabel.setText(current.getName());
        }
    }

    private void clearMessageListView() {
        messages.clear();
        messageView.getChildren().clear();
    }

    private void clearThreadListView() { threadView.getItems().clear(); }

    public void clearMessages() {
        clearMessageListView();
        clearThreadListView();
        nameLabel.setText("Start Conversation");
        current = null;
        messages.clear();
        threadCells.clear();
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

    public interface DateSet {
        void setDate(String date);
        void enableRetry();
    }

    public DateSet handleNewMessage(DataObject object, boolean sending) {
        DateSet output = null;
        //System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()).replace(",","\n  "));
        switch (object.getType()) {
            case DataObject.Type.SMS:
                SMS sms = (SMS) object;
                boolean isPartOfCurrent = false;
                if(current != null) {
                    if(sms.getThreadId() == current.getThreadId()) {
                        isPartOfCurrent = true;
                    }
                } else {
                    if(threadCells.size() == 0) {
                        addThreadCell(sms.getThreadId());
                        selectThread(0);
                    }
                }
                if(isPartOfCurrent) {
                    if(!messages.contains(sms)) {
                        messages.add(sms);
                        current.addMessage(sms);
                        messages.sort(new MessageComparator());
                        output = newMessage(sms, sending);
                    }
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
        /*
        if(threadView.getItems().size() == threadCells.size()) {
            if(ThreadCell.MILITARY_TIME) {
                ((Label) ((Node) threadView.getItems().get(threadCells.indexOf(current))).lookup("#date_label")).setText((new SimpleDateFormat(ThreadCell.DATE_FORMAT_24)).format(new Date(current.getMostRecent())));
            } else {
                ((Label) ((Node) threadView.getItems().get(threadCells.indexOf(current))).lookup("#date_label")).setText((new SimpleDateFormat(ThreadCell.DATE_FORMAT_12)).format(new Date(current.getMostRecent())));
            }
        }
        */
        threadCells.sort(new ThreadComparator());
        threadView.getItems().sort(new ThreadNodeComparator());
        threadView.refresh();

        return output;
    }

    public DateSet newMessage(SMS sms, boolean sending) {
        boolean scrollAtBottom = false;
        if(messageScroll.getVvalue() == 1.0) {
            scrollAtBottom = true;
        }
        try {
            final Node node = FXMLLoader.load(SMS_NODE_URL);
            Label label = (Label) node.lookup("#message_text");
            if(sms.getAddress() != null) {
                if(sms.getAddress().equals(current.getAddress())) {
                    node.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                    label.getStyleClass().clear();
                    label.getStyleClass().add("right-message");
                } else {
                    node.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                    label.getStyleClass().clear();
                    label.getStyleClass().add("left-message");
                }
            } else {
                node.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                label.getStyleClass().clear();
                label.getStyleClass().add("left-message");
            }
            if(messageView.getChildren().size() == 0) {
                VBox.setMargin(label, new Insets(10, 10, 0, 10));
            }
            label.setTextAlignment(TextAlignment.LEFT);
            label.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            label.maxWidthProperty().set(smsMaxWidth);
            label.setText(sms.getBody());
            final Label dateLabel = (Label) node.lookup("#message_date");
            DateSet output = null;
            if(!sending) {
                if(ThreadCell.MILITARY_TIME) {
                    dateLabel.setText((new SimpleDateFormat(ThreadCell.DATE_FORMAT_24)).format(sms.getDate()));
                } else {
                    dateLabel.setText((new SimpleDateFormat(ThreadCell.DATE_FORMAT_12)).format(sms.getDate()));
                }
            } else {
                dateLabel.setText("Sending...");
                output = new DateSet() {
                    @Override
                    public void setDate(String date) {
                        dateLabel.setText(date);
                    }

                    @Override
                    public void enableRetry() {
                        node.setOnMouseClicked(event -> {
                            if(event.getButton().equals(MouseButton.PRIMARY)) {
                                if(event.getClickCount() == 2) {
                                    messageView.getChildren().remove(node);
                                    messages.remove(sms);
                                    sendMessage(sms);
                                }
                            }
                        });
                    }
                };
            }

            int index = messages.indexOf(sms);
            if(index > -1) {
                messageView.getChildren().add(index, node);
            } else {
                messageView.getChildren().add(node);
            }

            if(scrollAtBottom) {
                Application.invokeLater(() -> {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    messageScroll.setVvalue(1.0);
                });
            }

            return output;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void newMessage(MMS mms, boolean sending) {

    }

    public void sendMessage(DataObject object) {
        final DateSet dateSet = handleNewMessage(object, true);
        Request request = new Request();
        switch (object.getType()) {
            case DataObject.Type.SMS:
                request.putBody(RequestHandler.RequestCode.SEND_SMS);
                request.putItem(RequestHandler.RequestCode.EXTRA_SEND_SMS_SMSOBJECT, object);
                RequestHandler.getInstance().sendRequest(client, request, (request1, response) -> {
                    if(response.getRequestValue().equals(RequestHandler.RequestCode.CONFIRMATION_SEND_SMS_SENT)) {
                        try {
                            SMS sms = (SMS) response.getItem(RequestHandler.RequestCode.EXTRA_SEND_SMS_SMSOBJECT);
                            DatabaseFactory.getMessageDatabase(client.getDeviceMac()).nonDuplicateInsert(sms);
                            if(dateSet != null) {
                                if(ThreadCell.MILITARY_TIME) {
                                    Application.invokeLater(() -> dateSet.setDate((new SimpleDateFormat(ThreadCell.DATE_FORMAT_24)).format(((SMS) object).getDateSent())));
                                } else {
                                    Application.invokeLater(() -> dateSet.setDate((new SimpleDateFormat(ThreadCell.DATE_FORMAT_12)).format(((SMS) object).getDateSent())));
                                }
                            }

                        } catch (UnequalArraysException | SQLException | NullResultSetException e) {
                            e.printStackTrace();
                        }
                    } else if(response.getRequestValue().equals(RequestHandler.RequestCode.CONFIRMATION_SEND_SMS_FAILED)) {
                        //Message failed
                        if(dateSet != null) {
                            Application.invokeLater(() -> {
                                dateSet.setDate("Message failed to send. Double click to retry.");
                                dateSet.enableRetry();
                            });
                        }
                    }
                });
                break;
            case DataObject.Type.MMS:
                //TODO handle sending MMS message
                break;
        }
    }

}