package com.notiflyapp.data;

import java.io.File;
import java.util.UUID;

/**
 * Created by Brennan on 5/7/2016.
 */
public class Response< A > extends DataObject< A , UUID> {

    private static final long serialVersionUID = 3349238414148539472L;

    //Body id the request string
    //Extra is UUID of specific message

    public Response() {
        super();
        type = Type.REQUEST;
    }

    /**
     * @return The body of the DataObject as a String
     */
    @Override
    public A getBody() {
        return body;
    }

    /**
     * @param body The body of the message being sent as a String
     */
    @Override
    public void putBody(A body) {
        this.body = body;
    }

    /**
     * @return Extra data stored in the message as a File
     */
    @Override
    public UUID getExtra() {
        return extra;
    }

    /**
     * @param extra Extra data that goes along with the body as a File
     */
    @Override
    public void putExtra(UUID extra) {
        this.extra = extra;
    }

}
