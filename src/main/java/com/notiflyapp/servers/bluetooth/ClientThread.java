package com.notiflyapp.servers.bluetooth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.MalformedJsonException;
import com.notiflyapp.data.ConversationThread;
import com.notiflyapp.data.DataObject;
import com.notiflyapp.data.requestframework.Response;
import com.notiflyapp.data.requestframework.RequestHandler;

import javax.microedition.io.StreamConnection;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.StringJoiner;
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

    private static final int BUFFER_SIZE = 512;
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
                byte[] header = new byte[4];
                int headerValue;
                int bytes;
                bytes = iStream.read(header);
                if (bytes == -1) {
                    serverOut("Client session dropped");
                    client.close();
                    break;
                }
                headerValue = retrieveHeader(header);
                //serverOut(String.valueOf(headerValue));
                byte[] buffer = new byte[headerValue];
                bytes = iStream.read(buffer);
                //****************************************************************
                //Crucial for successful packet retrieval
                while(bytes < headerValue) {
                    byte[] newBuffer = new byte[headerValue - bytes];
                    bytes += iStream.read(newBuffer);
                    for(int i = 0; i < newBuffer.length; i++) {
                        buffer[headerValue - newBuffer.length + i] = newBuffer[i];
                    }
                }
                //***************************************************************
                //serverOut(String.valueOf(bytes));
                if (bytes == -1) {
                    serverOut("Client session dropped");
                    client.close();
                    break;
                }
                dataIn(buffer);
            } catch (InterruptedIOException e1) {
                //This is the client being forced to disconnect
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int retrieveHeader(byte[] header) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.put(header);
        buffer.flip();
        return buffer.getInt();
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

    private void dataIn(byte[] data) throws MalformedJsonException, JsonSyntaxException {
        String str = new String(data);
        Gson gson = new Gson();
        //serverOut(str);
        JsonObject json = gson.fromJson(str, JsonObject.class);
        DataObject obj = null;
        switch (json.get("type").toString().replace("\"","")) {
            case DataObject.Type.SMS:
                obj = gson.fromJson(json, com.notiflyapp.data.SMS.class);
                break;
            case DataObject.Type.MMS:
                obj = gson.fromJson(json, com.notiflyapp.data.MMS.class);
                break;
            case DataObject.Type.DEVICE_INFO:
                obj = gson.fromJson(json, com.notiflyapp.data.DeviceInfo.class);
                break;
            case DataObject.Type.NOTIFICATION:
                obj = gson.fromJson(json, com.notiflyapp.data.Notification.class);
                break;
            case DataObject.Type.REQUEST:
                obj = gson.fromJson(json, com.notiflyapp.data.requestframework.Request.class);
                break;
            case DataObject.Type.RESPONSE:
                obj = gson.fromJson(json, com.notiflyapp.data.requestframework.Response.class);
                break;
            case DataObject.Type.CONTACT:
                switch (json.get("body").toString().replace("\"", "")) {
                    case RequestHandler.RequestCode.CONTACT_BY_THREAD_ID:
                        obj = gson.fromJson(json, new TypeToken<Response<ConversationThread>>(){}.getType());
                        break;
                }
                break;
            case DataObject.Type.CONVERSATIONTHREAD:
                obj = gson.fromJson(json, com.notiflyapp.data.ConversationThread.class);
                break;
        }
        client.receivedMsg(obj);
    }

    void send(DataObject dataObject){
        Gson gson = new Gson();
        String object = gson.toJson(dataObject);
        if(object != null) {
            handler.add(object);
        }
    }

    boolean isConnected() {
        return connected;
    }

    private class MessageHandler extends Thread {

        private ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<String>(1024);
        private boolean running = false;

        final Object lock = new Object();

        MessageHandler() {}

        public void add(String object) {
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

        private byte[] createHeader(int size) {
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            buffer.putInt(size);
            return buffer.array();
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
                        String object = queue.poll();
                        if(oStream != null) {
                            try {
                                oStream.write(createHeader(object.getBytes().length));
                                oStream.write(object.getBytes());
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
