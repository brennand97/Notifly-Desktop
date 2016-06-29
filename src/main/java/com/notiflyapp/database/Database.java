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

    /*      Old method, replaced with more robust one (Brennan Douglas - 6/23/2016)
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
    */

    protected ResultSet query(String table, String[] columns, String[] selection, String[] selectionValues, String order, String direction) throws UnequalArraysException, SQLException {
        if(selection != null) {
            if(selectionValues != null) {
                if(selection.length != selectionValues.length) {
                    throw UnequalArraysException.makeException();
                }
            } else {
                throw UnequalArraysException.makeException();
            }
        }

        StringBuilder command = new StringBuilder("SELECT ");
        if(columns == null || columns.length == 0) {
            command.append("* ");
        } else {
            for(int i = 0; i < columns.length; i++) {
                command.append("\"");
                command.append(columns[i]);
                command.append("\" ");
                if(i < columns.length - 1) {
                    command.append("AND ");
                }
            }
        }
        command.append("FROM ").append(table);
        if(selection == null || selection.length == 0) {} else {
            command.append(" WHERE ");
            for(int i = 0; i < selection.length; i++) {
                command.append("\"");
                command.append(selection[i]);
                command.append("\"=\"");
                command.append(selectionValues[i]);
                command.append("\"");
                if(i < selection.length - 1) {
                    command.append(" AND ");
                }
            }
        }

        if(order == null) {
            command.append(";");
        } else {
            command.append(" ");
            command.append(order);
            command.append(" ");
            command.append(direction);
            command.append(";");
        }

        return stmt.executeQuery(command.toString());
    }

    public void delete(String table, int id) throws SQLException {
        stmt.executeUpdate("DELETE FROM " + table + " WHERE " + ID + "=" + id + ";");
    }

    public void drop(String table) throws SQLException {
        stmt.executeUpdate("DROP TABLE IF EXISTS " + table + ";");
    }

}
