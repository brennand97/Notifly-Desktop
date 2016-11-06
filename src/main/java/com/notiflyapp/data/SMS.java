/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.data;

import java.io.File;
import java.util.HashMap;

/**
 * Created by Brennan on 4/17/2016.
 *
 * Used for sending Short Message Service (SMS) messages between devices
 */
public class SMS extends Message<String, File> {

    private static final long serialVersionUID = 3349238414148539467L;

    private int id;
    private String address;
    private String originatingAddress;
    private String creator;
    private String person;
    private boolean read;

    /**
     * Default constructor, degines DataObject.Type
     */
    public SMS() {
        super();
        type = Type.SMS;
    }

    /**
     * Constructor that sets sender and body on creation.
     *
     * @param address The phone number of the sender of the SMS as a String.
     * @param originatingAddress The phone number of the recipient of the message.
     * @param body The body of the SMS message as a string.
     */
    public SMS(String address, String originatingAddress, String body) {
        super();
        type = Type.SMS;
        this.address = address;
        this.originatingAddress = originatingAddress;
        this.body = body;
    }

    /**
     * @return The body of the DataObject as a String
     */
    @Override
    public String getBody() {
        return body;
    }

    /**
     * @param body The body of the message being sent as a String
     */
    @Override
    public void putBody(String body) {

    }

    /**
     * @return Extra data stored in the message as a File
     */
    @Override
    public File getExtra() {
        return null;
    }

    /**
     * @param file Extra data that goes along with the body as a File
     */
    @Override
    public void putExtra(File file) {

    }

    public SMS clone() {
        SMS sms = new SMS();
        sms.setId(id);
        sms.setAddress(address);
        sms.setOriginatingAddress(originatingAddress);
        sms.setBody(body);
        sms.setCreator(creator);
        sms.setDate(date);
        sms.setDateSent(dateSent);
        sms.setPerson(person);
        sms.setRead(read);
        sms.setSubscriptionId(subscriptionId);
        sms.setThreadId(threadId);
        return sms;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The {@code equals} method implements an equivalence relation
     * on non-null object references:
     * <ul>
     * <li>It is <i>reflexive</i>: for any non-null reference value
     * {@code x}, {@code x.equals(x)} should return
     * {@code true}.
     * <li>It is <i>symmetric</i>: for any non-null reference values
     * {@code x} and {@code y}, {@code x.equals(y)}
     * should return {@code true} if and only if
     * {@code y.equals(x)} returns {@code true}.
     * <li>It is <i>transitive</i>: for any non-null reference values
     * {@code x}, {@code y}, and {@code z}, if
     * {@code x.equals(y)} returns {@code true} and
     * {@code y.equals(z)} returns {@code true}, then
     * {@code x.equals(z)} should return {@code true}.
     * <li>It is <i>consistent</i>: for any non-null reference values
     * {@code x} and {@code y}, multiple invocations of
     * {@code x.equals(y)} consistently return {@code true}
     * or consistently return {@code false}, provided no
     * information used in {@code equals} comparisons on the
     * objects is modified.
     * <li>For any non-null reference value {@code x},
     * {@code x.equals(null)} should return {@code false}.
     * </ul>
     * <p>
     * The {@code equals} method for class {@code Object} implements
     * the most discriminating possible equivalence relation on objects;
     * that is, for any non-null reference values {@code x} and
     * {@code y}, this method returns {@code true} if and only
     * if {@code x} and {@code y} refer to the same object
     * ({@code x == y} has the value {@code true}).
     * <p>
     * Note that it is generally necessary to override the {@code hashCode}
     * method whenever this method is overridden, so as to maintain the
     * general contract for the {@code hashCode} method, which states
     * that equal objects must have equal hash codes.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj
     * argument; {@code false} otherwise.
     * @see #hashCode()
     * @see HashMap
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SMS) {
            SMS sms = (SMS) obj;
            if(sms.getThreadId() == threadId &&
                    sms.getBody().equals(body) &&
                    sms.getDate() == date &&
                    sms.getDateSent() == dateSent &&
                    sms.getOriginatingAddress().equals(originatingAddress)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "SMS{" +
                "id=" + id +
                ", address='" + address + '\'' +
                ", originatingAddress='" + originatingAddress + '\'' +
                ", creator='" + creator + '\'' +
                ", date=" + date +
                ", dateSent=" + dateSent +
                ", person='" + person + '\'' +
                ", read=" + read +
                ", subscriptionId=" + subscriptionId +
                ", threadId=" + threadId +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOriginatingAddress() {
        return originatingAddress;
    }

    public void setOriginatingAddress(String originatingAddress) {
        this.originatingAddress = originatingAddress;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setDateSent(long dateSent) {
        this.dateSent = dateSent;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

}
