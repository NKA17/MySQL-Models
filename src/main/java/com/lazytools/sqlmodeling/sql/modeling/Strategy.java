package com.lazytools.sqlmodeling.sql.modeling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Strategy {
    /**
     * TODO
     * What to insert
     * what to update
     * What to pull
     *
     * relevant fiels
     * what aliases to use
     * what database to use
     * what table to use
     * what fields are blobs
     */

    /**
     * The Table to use
     */
    private String table = null;

    /**
     * The Schema which the Table belongs to
     */
    //The database which the table belongs to
    private String database = null;

    /**
     * A map of relevantFields and their aliases
     */
    private HashMap<String, String> aliases = new HashMap<>();


    /**
     * A map of blobs and their aliases
     */
    private HashMap<String, String> blobAliases = new HashMap<>();

    /**
     * The fields that should be considered when building
     */
    private List<String> relevantFields = new ArrayList<>();

    /**
     * The fields which are built to/from Blob columns
     */
    private List<String> blobFields = new ArrayList<>();

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public HashMap<String, String> getAliases() {
        return aliases;
    }

    public HashMap<String, String> getBlobAliases() {
        return blobAliases;
    }

    public void setBlobAliases(HashMap<String, String> blobAliases) {
        this.blobAliases = blobAliases;
    }

    public void setAliases(HashMap<String, String> aliases) {
        this.aliases = aliases;
    }

    public List<String> getRelevantFields() {
        return relevantFields;
    }

    public void setRelevantFields(List<String> relevantFields) {
        this.relevantFields = relevantFields;
    }

    public List<String> getBlobFields() {
        return blobFields;
    }

    public void setBlobFields(List<String> blobFields) {
        this.blobFields = blobFields;
    }

    public Strategy clone(){
        Strategy strategy = new Strategy();
        strategy.setTable(getTable());
        strategy.setDatabase(getDatabase());
        strategy.getBlobFields().addAll(getBlobFields());
        strategy.getRelevantFields().addAll(getRelevantFields());
        strategy.setBlobAliases(new HashMap<>(getBlobAliases()));

        return strategy;
    }
}
