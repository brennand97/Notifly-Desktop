package com.notiflyapp.GUI;

import com.notiflyapp.bluetooth.BluetoothServer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    private BluetoothServer btServer;

    private boolean serverActive = false;

    @Override
    public void start(Stage primaryStage) throws Exception{
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
                    btServer = new BluetoothServer();
                    btServer.start();
                    serverActive = true;
                };
                (new Thread(runnable)).start();
                status.setText("Server started");
                btn.setText("Stop server");
            } else {
                Runnable runnable = () -> {
                    btServer.close();
                    serverActive = false;
                };
                (new Thread(runnable)).start();
                status.setText("Server stopped");
                btn.setText("Start server");
            }
        });
        btn.setLayoutX(0);
        btn.setLayoutX(50);

        VBox layout = new VBox(10);
        layout.getChildren().addAll(status, btn);
        root.getChildren().add(layout);

        Scene scene = new Scene(root, 300, 275);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        if(btServer != null) {
            btServer.close();
        }

        System.out.println("Stage is closing");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
