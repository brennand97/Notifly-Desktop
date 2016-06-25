package com.notiflyapp.data;

import com.notiflyapp.servers.bluetooth.BluetoothClient;

/**
 * Created by Brennan on 6/23/2016.
 */
public class RequestHandler {

    public final static class RequestCode {

        public final static String CONTACT_BY_THREAD_ID = "com.notiflyapp.data.RequestHandler.RequestCode.CONTACT_BY_THREAD_ID";

    }

    private static RequestHandler handler;

    private RequestHandler() {

    }

    public static RequestHandler getInstance() {
        if(handler == null) {
            handler = new RequestHandler();
        }
        return handler;
    }

    public void handleRequest(Request request) {

    }

    public void handleResponse(Response response) {

    }

    public void sendRequest(BluetoothClient client, Request request, RequestCallback callback) {
        //TODO handle tracking and sending of message
    }

    public interface RequestCallback {
        void responseReceived(Response response);
    }

    public void sendResponse(Response response) {

    }

}
