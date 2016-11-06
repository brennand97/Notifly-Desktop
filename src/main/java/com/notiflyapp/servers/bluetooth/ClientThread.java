/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.servers.bluetooth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.MalformedJsonException;
import com.notiflyapp.data.ConversationThread;
import com.notiflyapp.data.DataObject;
import com.notiflyapp.data.DataObjectDeserializer;
import com.notiflyapp.data.requestframework.*;

import javax.microedition.io.StreamConnection;
import java.io.*;
import java.nio.ByteBuffer;
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
                if(true) {
                    byte[] header = new byte[4];
                    int headerValue;
                    int bytes;
                    bytes = iStream.read(header);
                    if (bytes == -1) {
                        serverOut("Client session dropped at header");
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
                        int tmpBytes = iStream.read(newBuffer);
                        if(tmpBytes == -1) { // Catch for a client drop/disconnect
                            serverOut("Client session dropped from within catch loop.");
                            client.close();
                            break;
                        }
                        bytes += tmpBytes;
                        for(int i = 0; i < newBuffer.length; i++) {
                            buffer[headerValue - newBuffer.length + i] = newBuffer[i];
                        }
                    }
                    //***************************************************************
                    //serverOut(String.valueOf(bytes));
                    if (bytes == -1) {
                        serverOut("Client session dropped after loop");
                        client.close();
                        break;
                    }
                    dataIn(buffer);
                } else {
                    Thread.sleep(100);
                }
            } catch (InterruptedIOException e1) {
                //This is the client being forced to disconnect
            } catch (IOException | InterruptedException e) {
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
        //serverOut(str);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Response.class, new ResponseDeserializer());
        gsonBuilder.registerTypeAdapter(Request.class, new RequestDeserializer());
        gsonBuilder.registerTypeAdapter(DataObject.class, new DataObjectDeserializer());
        gsonBuilder.serializeNulls();
        Gson gson = gsonBuilder.create();
        DataObject obj = gson.fromJson(str, DataObject.class);
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
                                byte[] bytes = object.getBytes();
                                byte[] header = createHeader(bytes.length);
                                oStream.write(header);
                                oStream.write(bytes);
                            } catch (IOException e) {
                                e.printStackTrace();
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                                continue;
                            }
                            //queue.poll();
                        }
                    }
                }
            }
        }

    }

}
