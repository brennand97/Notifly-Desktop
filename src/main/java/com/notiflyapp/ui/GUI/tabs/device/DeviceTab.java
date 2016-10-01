/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.ui.GUI.tabs.device;

import com.notiflyapp.data.*;
import com.notiflyapp.data.requestframework.Request;
import com.notiflyapp.data.requestframework.RequestHandler;
import com.notiflyapp.database.DatabaseFactory;
import com.notiflyapp.database.MessageDatabase;
import com.notiflyapp.database.NullResultSetException;
import com.notiflyapp.database.UnequalArraysException;
import com.notiflyapp.servers.bluetooth.BluetoothClient;
import com.notiflyapp.controlcenter.Houston;
import com.notiflyapp.ui.GUI.tabs.TabHouse;
import com.sun.glass.ui.*;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Brennan on 6/8/2016.
 *
 * Tab for connected Bluetooth Device, displays conversation threads and their respective messages.
 * Also allows for sending of outgoing SMS.
 */
public class DeviceTab extends TabHouse {

    private BluetoothClient client;
    private DeviceInfo deviceInfo;

    private Tab tab;
    private ListView<ThreadCell> threadView;
    private VBox messageView;
    private ScrollPane messageScroll;
    private ArrayList<Message> messages = new ArrayList<>();
    private Label nameLabel;
    private Button sendButton;
    private TextArea textArea;
    private VBox optionButton;
    private Circle[] optionDots = new Circle[3];

    private ThreadCell current;
    private double smsMaxWidth;
    private double threadMaxWidth;

    private static final long gracePeriod = 5000;
    private static final int messageFontSize = 16;
    private static final String defaultName = "Bluetooth Device";
    private static final String creatorName = "com.notiflyapp.ui.GUI.tabs.device.DeviceTab";

    private final URL SMS_NODE_URL = getClass().getResource("/com/notiflyapp/ui/GUI/fxml/sms_cell.fxml");

    public DeviceTab(Tab tab, BluetoothClient client) {
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

        //Apply Stylesheet

        tab.getContent().getScene().getStylesheets().add(getClass().getResource("/com/notiflyapp/ui/GUI/style/Notifly.css").toExternalForm());

        //Lookup FXML objects

        threadView = (ListView<ThreadCell>) tab.getContent().lookup("#thread_list_view");
        messageView = (VBox) tab.getContent().lookup("#active_conversation_message_vbox");
        messageScroll = (ScrollPane) tab.getContent().lookup("#active_conversation_message_scroll_pane");
        nameLabel = (Label) tab.getContent().lookup("#active_conversation_title_bar_title");

        optionButton = (VBox) tab.getContent().lookup("#option_button");
            optionDots[0] = (Circle) optionButton.lookup("#option_dot_1");
            optionDots[1] = (Circle) optionButton.lookup("#option_dot_2");
            optionDots[2] = (Circle) optionButton.lookup("#option_dot_3");

        textArea = (TextArea) tab.getContent().lookup("#message_entry");
        sendButton = (Button) tab.getContent().lookup("#send_button");

        //Define initial SMS width
        smsMaxWidth = (messageView.getWidth() * 0.75);



        //Thread View (Conversations ListView)

        threadView.setCellFactory(new Callback<ListView<ThreadCell>, ListCell<ThreadCell>>() {
            @Override
            public ListCell<ThreadCell> call(ListView<ThreadCell> param) {
                return new ListCell<ThreadCell>() {

                    @Override
                    protected void updateItem(ThreadCell item, boolean empty) {
                        super.updateItem(item, empty);

                        if (!empty && item != null) {
                            Node node = item.getNode();
                            setGraphic(node);
                        }

                    }

                };
            }
        });
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
        threadView.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> Application.invokeLater(() -> {
            threadMaxWidth = newSceneWidth.doubleValue();
            for(ThreadCell cell : threadView.getItems()) {
                cell.updateMaxWidth(threadMaxWidth);
            }
            threadView.refresh();
        }));
        threadMaxWidth = 220;
        threadView.setPrefWidth(threadMaxWidth);



        //Message View (Message VBox)

        messageView.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> {
            Application.invokeLater(() -> handleResize(newSceneWidth.doubleValue()));
        });



        //Option Button/Dots (VBox in top right containing 3 circles)

        for(Circle dot: optionDots) {
            //Initialize the style of the circles
            dot.setFill(Paint.valueOf("#808080"));
            dot.setStroke(dot.getFill());
        }
        final ContextMenu menu = new ContextMenu();
        optionButton.setOnMouseClicked(event -> {
            //TODO react to option dots being pushed
            menu.hide();
            menu.getItems().clear();

            MenuItem item1 = new MenuItem();
            item1.setText("Clear Messages");
            //item1.setStyle("-fx-font-size: 14px; -fx-background-color: #5A5A5A; -fx-border-color: #5A5A5A;");
            item1.setOnAction(event1 -> {
                MessageDatabase database = DatabaseFactory.getMessageDatabase(deviceInfo.getDeviceMac());
                try {
                    database.drop(database.getTableName());
                    if(threadView.getItems().size() > 1) {
                        clearMessages();
                    } else if(threadView.getItems().size() > 0) {
                        int index = threadView.getItems().indexOf(current);
                        System.out.println(index);
                        if(index == 0) {
                            selectThread(1);
                        } else {
                            selectThread(0);
                        }
                        threadView.getItems().remove(index);
                    }
                    threadView.refresh();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            menu.getItems().add(item1);

            menu.show(optionButton, 0, 0);
            menu.setX((optionButton.getLayoutX() + optionButton.getWidth() - menu.getWidth()) + (event.getScreenX() - event.getSceneX()));
            menu.setY((optionButton.getLayoutY() + (optionButton.getHeight() * 0.75)) + (event.getScreenY() - event.getSceneY()));
        });



        //Text Area (Input Text Area for outgoing messages)

        textArea.setFont(new Font(messageFontSize));



        //Send Button (Button in bottom left to send outgoing messages

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



        //Initialize Conversation Threads from SQLite Database for device if available

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
        for(ThreadCell cell: threadView.getItems()) {
            if(cell.getThreadId() == threadId) {
                return;
            }
        }
        ThreadCell cell = new ThreadCell(client, this, threadId, threadMaxWidth);

        threadView.getItems().add(cell);
        threadView.getItems().sort(new ThreadComparator());
    }

    private void selectThread(int index) {
        ThreadCell tmpCurrent = threadView.getItems().get(index);
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

    void updateCurrentNameLabel() {
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

    interface DateSet {
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
                    if(threadView.getItems().size() == 0) {
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
                    for(ThreadCell cell: threadView.getItems()) {
                        if(cell.getThreadId() == sms.getThreadId()) {
                            cell.addMessage(sms);
                        }
                    }
                }
                break;
            case DataObject.Type.MMS:

                break;
        }

        threadView.getItems().sort(new ThreadComparator());
        threadView.refresh();

        return output;
    }

    private DateSet newMessage(SMS sms, boolean sending) {
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

    private void newMessage(MMS mms, boolean sending) {

    }

    private void sendMessage(DataObject object) {
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