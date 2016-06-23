package com.notiflyapp.data;

/**
 * Created by Brennan on 6/23/2016.
 */
public class RequestHandler {

    public final static class RequestCode {

        public final static String CONTACT_BY_THREAD_ID = "com.notiflyapp.data.RequestHandler.RequestCode.CONTACT_BY_THREAD_ID";

    }

    private RequestHandler handler;

    private RequestHandler() {

    }

    public RequestHandler getInstance() {
        if(handler == null) {
            handler = new RequestHandler();
        }
        return handler;
    }

    public void handleRequest(Request request) {

    }

    public void sendResponse(Response response) {

    }

}
