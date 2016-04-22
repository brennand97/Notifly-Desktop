package com.notiflyapp.bluetooth;

import com.data.DataObject;
import com.data.Serial;

import javax.microedition.io.StreamConnection;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by Brennan on 4/16/2016.
 */
public class ClientThread extends Thread{

    private StreamConnection conn;
    private BluetoothClient client;
    private DataInputStream iStream;
    private DataOutputStream oStream;

    private int BUFFER_SIZE = 1024;
    private ArrayList<Byte> byteHolder = new ArrayList<>();
    private boolean multiBufferObject = false;

    private boolean connected = false;

    public ClientThread(BluetoothClient client, StreamConnection conn) {
        this.conn = conn;
        this.client = client;
        connected = true;
    }


    public void run() {
        try{
            iStream = new DataInputStream(conn.openInputStream());
            oStream = new DataOutputStream(conn.openOutputStream());
            serverOut("Client connected");
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(connected) {
            try {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytes;
                bytes = iStream.read(buffer);
                if ((new String(buffer)).toLowerCase().contains("exit")) {
                    serverOut("Client quit session");
                    client.close();
                    break;
                } else if (bytes == -1) {
                    serverOut("Client session dropped");
                    client.close();
                    break;
                }
                if (bytes > 0) {
                    handleData(buffer);
                }
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
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
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
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        connected = false;
        try {
            conn.close();
            iStream.close();
            oStream.close();
            this.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
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

    public void send(DataObject msg) throws IOException {
        oStream.write(msg.serialize());
    }

    public boolean isConnected() {
        return connected;
    }

}
