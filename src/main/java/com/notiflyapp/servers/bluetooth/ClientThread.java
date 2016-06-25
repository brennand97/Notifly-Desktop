package com.notiflyapp.servers.bluetooth;

import com.notiflyapp.data.DataObject;
import com.notiflyapp.data.Serial;

import javax.microedition.io.StreamConnection;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Brennan on 4/16/2016.
 *
 * Runs main bluetooth client loop to receive data when sent.  Also sends data to connected device when needed.
 */
class ClientThread extends Thread{

    private StreamConnection conn;
    private BluetoothClient client;
    private DataInputStream iStream;
    private DataOutputStream oStream;
    private MessageHandler handler = new MessageHandler();

    private static final int BUFFER_SIZE = 1024;
    private ArrayList<Byte> byteHolder = new ArrayList<>();
    private boolean multiBufferObject = false;

    private boolean connected = false;

    ClientThread(BluetoothClient client, StreamConnection conn) {
        this.conn = conn;
        this.client = client;
        connected = true;
    }


    public void run() {
        try{
            iStream = new DataInputStream(conn.openInputStream());
            oStream = new DataOutputStream(conn.openOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        handler.start();

        while(connected) {
            try {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytes;
                bytes = iStream.read(buffer);
                if (bytes == -1) {
                    serverOut("Client session dropped");
                    client.close();
                    break;
                }
                handleData(buffer);
            } catch (InterruptedIOException e1) {
                //This is the client being forced to disconnect
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleData(byte[] buffer) {
        if (!multiBufferObject) {
            try {
                dataIn(buffer);
            } catch (EOFException e1) {
                for (Byte b : buffer) {
                    byteHolder.add(b);
                }
                multiBufferObject = true;
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        } else {
            for (Byte b : buffer) {
                byteHolder.add(b);
            }
            byte[] bBuffer = new byte[byteHolder.size()];
            for (int i = 0; i < byteHolder.size(); i++) {
                bBuffer[i] = byteHolder.get(i);
            }
            try {
                dataIn(bBuffer);
                byteHolder.clear();
                multiBufferObject = false;
            } catch (EOFException e2) {
                //Still isn't a complete object
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    void close() {
        connected = false;
        try {
            conn.close();
            iStream.close();
            oStream.close();
            this.join();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private void serverOut(String out) {
        client.serverOut(out);
    }

    private void dataIn(byte[] data) throws IOException, ClassNotFoundException {
        Object object = Serial.deserialize(data);
        if(object instanceof DataObject) {
            client.receivedMsg((DataObject) object);
        }
    }

    void send(DataObject object){
        if(object != null) {
            handler.add(object);
        }
    }

    boolean isConnected() {
        return connected;
    }

    private class MessageHandler extends Thread {

        private ArrayBlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(1024);
        private boolean running = false;

        private final Object lock = new Object();

        MessageHandler() {}

        public void add(DataObject object) {
            if(object != null) {
                queue.add(object);
                if(running) {
                    try {
                        synchronized (lock) {
                            lock.notify();
                        }
                    } catch (IllegalMonitorStateException e) {}
                }
            }
        }

        public void run() {
            serverOut("Bluetooth MessageHandler started");
            running = true;
            synchronized (lock) {
                while (running) {
                    if(queue.isEmpty()) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Object object = queue.poll();
                        if(oStream != null) {
                            try {
                                oStream.write(Serial.serialize(object));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

    }

}
