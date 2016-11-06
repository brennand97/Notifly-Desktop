/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.controlcenter;

import com.notiflyapp.servers.bluetooth.BluetoothServer;
import com.notiflyapp.ui.commandline.Commands;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static ServerHandler serverHandler = new ServerHandler();

    private static boolean serverActive = false;

    @Override
    public void start(Stage primaryStage) throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource("/com/notiflyapp/ui/GUI/fxml/main.fxml"));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/com/notiflyapp/ui/notifly_icon-512.png")));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/notiflyapp/ui/GUI/style/Notifly.css").toExternalForm());
        primaryStage.setScene(scene);

        Houston.initialize(primaryStage, scene);

        primaryStage.show();

        Houston.getInstance().startBluetoothServer();

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
