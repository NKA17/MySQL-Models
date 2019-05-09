package com.lazytools.sqlmodeling.sql.basic;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SQLBasics {

    //Tells the program what driver to use at startup
    static{
        try {
            Class.forName("com.mysql.jdbc.Driver");
        }catch (ClassNotFoundException cnf){
            System.out.println("DEPENDENCY ERROR!! Don't worry, its an easy fix for us!\n" +
                    "1.\tDownload 'mysql-connector-java' version '5.1.39' from 'https://dev.mysql.com/downloads/connector/j/5.1.html'\n" +
                    "2.\tUnzip the folder and note the directory you are extracting to.\n" +
                    "3.\tGo to File > Project Structure > Modules > Dependencies\n" +
                    "4.\tClick the PLUS icon and add 'jars or directories'\n" +
                    "5.\tChoose the jar file you downloaded (NOT the binary one)");

        }catch (Exception e){
            System.out.println("ABANDON HOPE!! I don't know what's wrong!");
        }
    }


    //Establishes a connection to the database
    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(
                "jdbc:mysql://192.168.1.135", "nate", "7.51430Nine");

    }
    public static Connection getConnection(String host, String user, String pass)throws SQLException{
        return DriverManager.getConnection(host,user,pass);
    }

    /**
     * List the tables in the DB and the Columns in each table
     * @throws SQLException
     */
    public static void describeDataBase(String db)throws SQLException{

        //Get tables
        List<String> tables = new ArrayList<>();
        Connection con = getConnection();
        Statement s = con.createStatement();
        ResultSet rs = s.executeQuery("show tables in "+db);
        while(rs.next()){
            tables.add(rs.getString(1));
        }

        //Describe each table
        for(String table: tables){
            System.out.println(table);
            String query = String.format("describe %s.%s",db,table);
            ResultSet trs = s.executeQuery(query);
            while (trs.next()){
                String description = String.format("\t%s:%s",trs.getString(1),trs.getString(2));
                System.out.println(description);
            }
        }
        con.close();
    }

    public static String getPrimaryKeyForTable(String tableName)throws SQLException{


        String query = String.format("describe meetup.%s",tableName);
        Connection con = getConnection();
        ResultSet trs = con.createStatement().executeQuery(query);
        while (trs.next()){
            if(trs.getString("Key").equals("PRI")) {
                String pKey = trs.getString("FIELD");
                con.close();
                return pKey;
            }
        }

        con.close();
        return null;
    }
}
