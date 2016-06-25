package com.notiflyapp.controlcenter;

import com.notiflyapp.controlcenter.ServerHandler;
import com.notiflyapp.controlcenter.Houston;
import com.notiflyapp.servers.bluetooth.BluetoothServer;
import com.notiflyapp.ui.commandline.Commands;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static ServerHandler serverHandler = new ServerHandler();

    private static boolean serverActive = false;

    @Override
    public void start(Stage primaryStage) throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource("/com/notiflyapp/ui/GUI/view/main.fxml"));
        Scene scene = new Scene(root);
        //scene.getStylesheets().add(getClass().getResource(Put Style sheet reference here));
        primaryStage.setScene(scene);

        Houston.initialize(primaryStage, scene);

        primaryStage.show();

        Houston.getInstance().startBluetoothServer();

        /*
        Group root = new Group();
        primaryStage.setTitle("Hello World");

        Label status = new Label();
        status.setText("Server not initialized");
        status.setLayoutX(0);
        status.setLayoutY(0);

        Button btn = new Button("Start server");
        btn.setOnAction(event -> {
            if(!serverActive) {
                Runnable runnable = () -> {
                    BluetoothServer btServer = new BluetoothServer();
                    btServer.start();
                    serverActive = true;
                    serverHandler.addServer(btServer);
                };
                (new Thread(runnable)).start();
                status.setText("Server started");
                btn.setText("Stop server");
            } else {
                Runnable runnable = () -> {
                    serverHandler.closeServers();
                    serverActive = false;
                };
                (new Thread(runnable)).start();
                status.setText("Server stopped");
                btn.setText("Start server");
            }
        });
        btn.setLayoutX(0);
        btn.setLayoutX(50);

        final TextField phoneNumber = new TextField("Phone Number");
        phoneNumber.setLayoutX(0);
        phoneNumber.setLayoutY(100);

        final TextField message = new TextField("Message");
        message.setLayoutX(0);
        message.setLayoutY(150);

        Button sendBtn = new Button("Send Message");
        sendBtn.setOnAction(event -> {
            String number = phoneNumber.getText().replace("-", "");
            String smsMessage = message.getText();
            message.setText("");
            serverHandler.sendSMSMessage(new SMS(number, null, smsMessage), new DeviceInfo("G4", "00:00:00:00:00:20", BluetoothClient.Type.PHONE));
        });

        VBox layout = new VBox(10);
        layout.getChildren().addAll(status, btn, phoneNumber, message, sendBtn);
        root.getChildren().add(layout);

        Scene scene = new Scene(root, 300, 275);
        primaryStage.setScene(scene);
        primaryStage.show();
        */

    }

    @Override
    public void stop() throws Exception {
        super.stop();

        Houston.getInstance().close();

        System.out.println("Stage is closing");

        System.exit(0);
    }

    public static void main(String[] args) {
        boolean gui = true;
        for(String arg: args) {
            if(arg.equals(Commands.NO_GUI_LONG)) {
                gui = false;
                break;
            }
        }
        if(gui) {
            launch(args);
        } else {
            if(!serverActive) {
                Runnable runnable = () -> {
                    BluetoothServer btServer = new BluetoothServer();
                    btServer.start();
                    serverActive = true;
                    serverHandler.addServer(btServer);
                };
                (new Thread(runnable)).start();
            } else {
                Runnable runnable = () -> {
                    serverHandler.closeServers();
                    serverActive = false;
                };
                (new Thread(runnable)).start();
            }
        }
    }
}
