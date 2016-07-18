package com.notiflyapp.data;

import com.notiflyapp.data.DataObject;

/**
 * Created by Brennan on 7/17/2016.
 */
public abstract class Message<A,B> extends DataObject<A,B> {
    protected long date;
    protected long dateSent;
    protected long subscriptionId;
    protected int threadId;

    public long getDate() {
        if(date != 0) {
            return date;
        } else {
            return dateSent;
        }
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getDateSent() {
        return dateSent;
    }

    public void setDateSent(long dateSent) {
        this.dateSent = dateSent;
    }

    public long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }
}
