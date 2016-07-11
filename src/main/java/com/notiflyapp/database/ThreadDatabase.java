package com.notiflyapp.database;

import com.notiflyapp.data.Contact;
import com.notiflyapp.data.ConversationThread;
import com.notiflyapp.data.requestframework.Response;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Brennan on 6/29/2016.
 */
public class ThreadDatabase extends MacDatabase {

    protected final static String TABLE_NAME_PREFIX = "threads_";

    public final static String THREAD_ID = "thread_id";
    public final static String ARCHIVED = "archived";
    public final static String DATE = "date";
    public final static String ADDRESS = "address";
    public final static String CONTACT_IDS = "contact_ids";

    public ThreadDatabase(String mac) {
        super(mac);
    }

    @Override
    protected String getCreateTableString() {
        return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY, " +
                THREAD_ID + " INTEGER, " + ARCHIVED + " INTEGER, " + DATE + " TEXT, " +
                ADDRESS + " TEXT, " + CONTACT_IDS + " TEXT);";
    }

    @Override
    protected String getTableNamePrefix() {
        return TABLE_NAME_PREFIX;
    }

    public void insert(ConversationThread thread) throws SQLException {
        StringBuilder call = new StringBuilder();
        call.append("INSERT INTO ").append(TABLE_NAME).append(" (").append(THREAD_ID).append(", ").append(ARCHIVED)
                .append(", ").append(DATE).append(", ").append(ADDRESS).append(", ").append(CONTACT_IDS).append(") VALUES (");
        call.append(thread.getExtra()).append(", ");
        call.append(thread.isArchived() ? 1 : 0).append(", ");
        call.append("\"").append(thread.getDate()).append("\"").append(", ");
        call.append("\"").append(formatAddresses(thread.getContacts())).append("\"").append(", ");
        call.append("\"").append(formatContactIds(thread.getContacts())).append("\"").append(");");
        stmt.executeUpdate(call.toString());
    }

    public void nonDuplicateInsert(ConversationThread thread) throws SQLException {
        if(has(thread)) {
            update(thread);
        } else {
            insert(thread);
        }
    }

    public void update(ConversationThread thread) throws SQLException {
        StringBuilder call = new StringBuilder();
        call.append("INSERT OR REPLACE INTO ").append(TABLE_NAME).append(" (").append(ID).append(", ").append(THREAD_ID).append(", ").append(ARCHIVED)
                .append(", ").append(DATE).append(", ").append(ADDRESS).append(", ").append(CONTACT_IDS).append(") VALUES (");
        call.append(String.valueOf(getId(thread))).append(", ");
        call.append(thread.getExtra()).append(", ");
        call.append(thread.isArchived() ? 1 : 0).append(", ");
        call.append("\"").append(thread.getDate()).append("\"").append(", ");
        call.append("\"").append(formatAddresses(thread.getContacts())).append("\"").append(", ");
        call.append("\"").append(formatContactIds(thread.getContacts())).append("\"").append(");");
        stmt.executeUpdate(call.toString());
    }

    public ConversationThread queryThread(int threadId) throws SQLException {
        ResultSet rs = null;
        try {
            rs = query(TABLE_NAME, null, new String[]{ THREAD_ID }, new String[]{ String.valueOf(threadId) }, null, null);
        } catch (UnequalArraysException e) {
            e.printStackTrace();
        }
        if(rs != null) {
            if(rs.next()) {
                ConversationThread thread = makeConversationThread(rs);
                rs.close();
                return thread;
            }
        }
        return null;
    }

    public ConversationThread[] queryThreads(int[] threadIds) throws SQLException {
        String[] selection = new String[threadIds.length];
        String[] values = new String[threadIds.length];
        for(int i = 0; i < threadIds.length; i++) {
            selection[i] = THREAD_ID;
            values[i] = String.valueOf(threadIds[i]);
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
        return getAllConversationThreads(rs, true);
    }

    public ConversationThread[] getAllThreads() throws SQLException {
        ResultSet rs = null;
        try {
            rs = query(TABLE_NAME, null, null, null, null, null);
        } catch (UnequalArraysException e) {
            e.printStackTrace();
        }
        if(rs == null) {
            return null;
        }
        return getAllConversationThreads(rs, true);
    }

    public int getId(ConversationThread thread) throws SQLException {
        ResultSet rs = null;
        try {
            rs = query(TABLE_NAME, new String[]{ ID }, new String[]{ THREAD_ID }, new String[]{ String.valueOf(thread.getExtra()) }, null, null);
        } catch (UnequalArraysException e) {
            e.printStackTrace();
        }
        if(rs != null) {
            if(rs.next()) {
                int id = rs.getInt(ID);
                rs.close();
                return id;
            }
        }
        return -1;
    }

    public boolean has(ConversationThread thread) throws SQLException {
        ConversationThread[] threads = queryThreads(new int[]{ thread.getExtra() });
        if(threads.length > 0) {
            return true;
        }
        return false;
    }

    private ConversationThread makeConversationThread(ResultSet rs) throws SQLException {
        ConversationThread thread = new ConversationThread();
        thread.putExtra(rs.getInt(THREAD_ID));
        thread.setArchived(rs.getInt(ARCHIVED) == 1);
        thread.setDate(rs.getString(DATE));
        String address = rs.getString(ADDRESS);
        String contactIds = rs.getString(CONTACT_IDS);
        if(contactIds != null || contactIds.equals("")) {
            for(String s: contactIds.split(" ")) {
                if(s.equals("")) {
                    continue;
                }
                Contact c = makeContact(Integer.parseInt(s));
                if(c != null) {
                    thread.addContact(c);
                }
            }
        }
        Contact[] contacts = thread.getContacts();
        outerLoop:
        for(String s: address.split(";")) {
            for(int i = 0; i < contacts.length; i++) {
                if(contacts[i].getExtra().equals(s)) {
                    continue outerLoop;
                }
            }
            Contact c = new Contact();
            c.putExtra(s);
            thread.addContact(c);
        }
        return thread;
    }

    private ConversationThread[] getAllConversationThreads(ResultSet rs, boolean closeSet) throws SQLException {
        ArrayList<ConversationThread> list = new ArrayList<>();
        while (rs.next()) {
            list.add(makeConversationThread(rs));
        }
        if(closeSet) {
            rs.close();
        }
        ConversationThread[] array = new ConversationThread[list.size()];
        return list.toArray(array);
    }

    private Contact makeContact(int contactId) throws SQLException {
        ContactDatabase cd = DatabaseFactory.getContactDatabase(macAddress);
        return cd.queryContact(contactId);
    }

    private String formatAddresses(Contact[] contacts) {
        StringBuilder b = new StringBuilder();
        for(int i = 0; i < contacts.length; i++) {
            b.append(contacts[i].getExtra());
            if(i < contacts.length - 1) {
                b.append(";");
            }
        }
        return b.toString();
    }

    private String formatContactIds(Contact[] contacts) {
        StringBuilder b = new StringBuilder();
        for(int i = 0; i < contacts.length; i++) {
            b.append(contacts[i].getContactId());
            if(i < contacts.length - 1) {
                b.append(" ");
            }
        }
        return b.toString();
    }
}
