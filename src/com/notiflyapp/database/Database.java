package com.notiflyapp.database;

import java.sql.*;

/**
 * Created by Brennan on 6/8/2016.
 */
public abstract class Database {

    private final static String DATABASE_NAME = "messages.db";
    private static String DATABASE_PATH = "";

    public static final String ID = "id";

    protected static Connection connection;
    protected static Statement stmt;

    public void initialize() {
        try {
            createDatabase();
            stmt.execute(getCreateTableString());
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void createDatabase() throws ClassNotFoundException, SQLException {
        if(connection == null) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_PATH + DATABASE_NAME);
            stmt = connection.createStatement();
            stmt.setQueryTimeout(30);
            System.out.println("Opened " + DATABASE_NAME + " database successfully");
        }
    }

    protected abstract String getCreateTableString();

    public ResultSet getAll(String table) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + table + ";");
        return rs;
    }

    protected ResultSet query(String table, String[] columns, String[] values) throws UnequalArraysException, SQLException {
        if(columns.length != values.length) {
            throw UnequalArraysException.makeException();
        }
        StringBuilder selection = new StringBuilder();
        for(int i = 0; i < columns.length; i++) {
            selection.append(columns[i]);
            selection.append("=");
            selection.append("\"").append(values[i]).append("\"");
            if(i < columns.length - 1) {
                selection.append(" AND ");
            }
        }
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + table + " WHERE " + selection.toString() + ";");
        return rs;
    }

    public void delete(String table, int id) throws SQLException {
        stmt.executeUpdate("DELETE FROM " + table + " WHERE " + ID + "=" + id + ";");
    }

    public void clear(String table) throws SQLException {
        stmt.executeUpdate("DROP TABLE IF EXISTS " + table + ";");
    }

}
