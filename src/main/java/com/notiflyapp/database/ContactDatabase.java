package com.notiflyapp.database;

import com.notiflyapp.data.Contact;
import com.notiflyapp.data.ConversationThread;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Brennan on 6/29/2016.
 */
public class ContactDatabase extends MacDatabase {

    //TODO **WARNING** this database should still be in development because the contact object hasn't been fully fleshed out

    private final static String TABLE_NAME_PREFIX = "contacts_";

    public final static String CONTACT_ID = "contact_id";
    public final static String NAME = "name";
    public final static String PHONE_NUMBER = "phone_number";

    public ContactDatabase(String mac) {
        super(mac);
    }

    @Override
    protected String getTableNamePrefix() {
        return TABLE_NAME_PREFIX;
    }

    @Override
    protected String getCreateTableString() {
        return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY, " +
                CONTACT_ID + " INTEGER, " + NAME + " TEXT, " + PHONE_NUMBER + " TEXT);";
    }

    public void insert(Contact contact) throws SQLException {
        StringBuilder call = new StringBuilder();
        call.append("INSERT INTO ").append(TABLE_NAME).append(" (").append(CONTACT_ID).append(", ").append(NAME)
                .append(", ").append(PHONE_NUMBER).append(") VALUES (");
        call.append(contact.getContactId()).append(", ");
        call.append("\"").append(contact.getBody()).append("\"").append(", ");
        call.append("\"").append(contact.getExtra()).append("\"").append(");");
        stmt.executeUpdate(call.toString());
    }

    public void nonDuplicateInsert(Contact contact) throws SQLException {
        if(has(contact)) {
            update(contact);
        } else {
            insert(contact);
        }
    }

    public void update(Contact contact) throws SQLException {
        StringBuilder call = new StringBuilder();
        call.append("INSERT OR REPLACE INTO ").append(TABLE_NAME).append(" (").append(ID).append(", ").append(CONTACT_ID).append(", ").append(NAME)
                .append(", ").append(PHONE_NUMBER).append(") VALUES (");
        call.append(String.valueOf(getId(contact))).append(", ");
        call.append(contact.getContactId()).append(", ");
        call.append("\"").append(contact.getBody()).append("\"").append(", ");
        call.append("\"").append(contact.getExtra()).append("\"").append(");");
        stmt.executeUpdate(call.toString());
    }

    public Contact queryContact(int contactId) throws SQLException {
        ResultSet rs = null;
        try {
            rs = query(TABLE_NAME, null, new String[]{ CONTACT_ID }, new String[]{ String.valueOf(contactId) }, null, null);
        } catch (UnequalArraysException e) {
            e.printStackTrace();
        }
        if(rs != null) {
            if(rs.first()) {
                Contact c = makeContact(rs);
                rs.close();
                return c;
            }
        }
        return null;
    }

    public Contact[] queryContacts(int[] contactIds) throws SQLException {
        String[] selection = new String[contactIds.length];
        String[] values = new String[contactIds.length];
        for(int i = 0; i < contactIds.length; i++) {
            selection[i] = CONTACT_ID;
            values[i] = String.valueOf(contactIds[i]);
        }
        ResultSet rs = null;
        try {
            rs = query(TABLE_NAME, null, selection, values, null, null);
        } catch (UnequalArraysException e) {
            e.printStackTrace();
        }
        if(rs == null) {
            return null;
        }
        return makeContacts(rs, true);
    }

    public Contact[] getAllContacts() throws SQLException {
        ResultSet rs = null;
        try {
            rs = query(TABLE_NAME, null, null, null, null, null);
        } catch (UnequalArraysException e) {
            e.printStackTrace();
        }
        if(rs == null) {
            return null;
        }
        return makeContacts(rs, true);
    }

    public int getId(Contact c) throws SQLException {
        ResultSet rs = null;
        try {
            rs = query(TABLE_NAME, null, new String[]{ CONTACT_ID }, new String[]{ String.valueOf(c.getContactId()) }, null, null);
        } catch (UnequalArraysException e) {
            e.printStackTrace();
        }
        if(rs != null) {
            if(rs.first()) {
                int id = rs.getInt(ID);
                rs.close();
                return id;
            }
        }
        return -1;
    }

    public boolean has(Contact contact) throws SQLException {
        Contact[] threads = queryContacts(new int[]{ contact.getContactId() });
        if(threads.length > 0) {
            return true;
        }
        return false;
    }

    private Contact makeContact(ResultSet rs) throws SQLException {
        Contact c = new Contact();
        c.setContactId(rs.getInt(CONTACT_ID));
        c.putBody(rs.getString(NAME));
        c.putExtra(rs.getString(PHONE_NUMBER));
        return c;
    }

    private Contact[] makeContacts(ResultSet rs, boolean closeSet) throws SQLException {
        ArrayList<Contact> list = new ArrayList<>();
        while (rs.next()) {
            list.add(makeContact(rs));
        }
        if(closeSet) {
            rs.close();
        }
        Contact[] array = new Contact[list.size()];
        return list.toArray(array);
    }

    /**
     * This is the same phone number format function from the android application, this is done for simplicity
     * and unity between the clients.  This function serves mostly as a precaution than a necessity.
     *
     * @param s any unformatted incoming phone number.
     * @return formatted phone number.
     */
    private static String formatPhoneNumber(String s) {
        String raw = s.replace(" ", "").replace("(", "").replace(")", "").replace("+", "").replace("-", "");
        if(raw.substring(0, 1).equals("1") && raw.length() == 11 && s.contains("+")) {
            raw = raw.substring(1);
        }
        StringBuilder b = new StringBuilder();
        if(raw.length() == 10) {
            b.append("(");
            b.append(raw.substring(0, 3));
            b.append(")");
            b.append(" ");
            b.append(raw.substring(3, 6));
            b.append("-");
            b.append(raw.substring(6));
        } else if(raw.length() >= 11) {
            b.append(raw.substring(0, raw.length() - 10));
            b.append(" ");
            b.append(raw.substring(raw.length() - 10, raw.length() - 7));
            b.append("-");
            b.append(raw.substring(raw.length() - 7, raw.length() - 4));
            b.append("-");
            b.append(raw.substring(raw.length() - 4));
        }
        return b.toString();
    }

}
