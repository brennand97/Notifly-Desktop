package com.notiflyapp.data.requestframework;

import com.notiflyapp.data.ConversationThread;
import com.notiflyapp.servers.bluetooth.BluetoothClient;

import java.util.HashMap;

/**
 * Created by Brennan on 6/23/2016.
 */
public class RequestHandler {

    public final static class RequestCode {

        public final static String CONTACT_BY_THREAD_ID = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.CONTACT_BY_THREAD_ID";
            public final static String EXTRA_CONTACT_BY_THREAD_ID_THREAD = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.EXTRA_CONTACT_BY_THREAD_ID_THREAD";

        public final static String SEND_SMS = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.SEND_SMS";
            public final static String EXTRA_SEND_SMS_SMSOBJECT = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.EXTRA_SEND_SMS_SMSOBJECT";
            public final static String CONFIRMATION_SEND_SMS_SENT = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.CONFIRMATION_SEND_SMS_SENT";
            public final static String CONFIRMATION_SEND_SMS_FAILED = "com.notiflyapp.data.requestframework.RequestHandler.RequestCode.CONFIRMATION_SEND_SMS_FAILED";

    }

    private HashMap<String, Request> requestHashMap = new HashMap<>();              //String is the UUID of the request in string form and the Request object is the request itself
    private HashMap<String, BluetoothClient> clientHashMap = new HashMap<>();       //String is the UUID of the request in string form and the BluetoothClient is the client related to the request
    private HashMap<String, ResponseCallback> callbackHashMap = new HashMap<>();    //String is the UUID of the request in string form and the ResponseCallback is the callback assigned with the request

    private static RequestHandler handler;

    private RequestHandler() {

    }

    public interface ResponseCallback {
        void responseReceived(Request request, Response response);
    }

    public static RequestHandler getInstance() {
        if(handler == null) {
            handler = new RequestHandler();
        }
        return handler;
    }

    public void handleRequest(BluetoothClient client, Request request) {
        clientHashMap.put(request.getExtra().toString(), client);
        Response response = Response.makeResponse(request);
        switch (request.getBody()) {
            case RequestCode.CONTACT_BY_THREAD_ID:
                //Not relevant to current client, so null values will be sent
                response.putItem(RequestCode.EXTRA_CONTACT_BY_THREAD_ID_THREAD, null);
                break;
            default:
                //TODO handle if the given key does not match any of the defined request codes
                break;
        }
    }

    public void handleResponse(Response response) {
        if(callbackHashMap.containsKey(response.getExtra().toString()) && requestHashMap.containsKey(response.getExtra().toString())) {
            //TODO handle the incoming of a requested response
            String uuid = response.getExtra().toString();
            callbackHashMap.get(uuid).responseReceived(requestHashMap.get(uuid), response);
            callbackHashMap.remove(response.getExtra().toString());
            requestHashMap.remove(response.getExtra().toString());
        } else {
            //The response doesn't have a matching request so for now just drop
        }
    }

    //TODO if the server framework is ever completely generalized on all OSes then change this function the the generalized client object
    public void sendRequest(BluetoothClient client, Request request, ResponseCallback callback) {
        requestHashMap.put(request.getExtra().toString(), request);
        callbackHashMap.put(request.getExtra().toString(), callback);
        client.sendMsg(request);
    }

    public void sendResponse(Response response) {
        String uuid = response.getExtra().toString();
        if(clientHashMap.containsKey(uuid)) {
            BluetoothClient client = clientHashMap.get(uuid);
            client.sendMsg(response);
            clientHashMap.remove(uuid);
        }
    }

}
