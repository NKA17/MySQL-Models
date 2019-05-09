package com.lazytools.sqlmodeling.sql.modeling.object;

import com.lazytools.sqlmodeling.sql.basic.SQLBasics;

import javax.imageio.ImageIO;
import javax.lang.model.type.PrimitiveType;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class ModelBuilder<T> {


    //TODO build support for each of these guys https://www.tutorialspoint.com/mysql/mysql-data-types.htm

    private Class<T> clazz;
    private String className;
    private String database;
    private List<String> blobs = new ArrayList<>();

    //when pulling to db
    private HashMap<String, String> column_alias = new HashMap<>();
    private String tableAlias;
    private List<String> pullIgnores = new ArrayList<>();

    //when pushing from db
    private HashMap<String, String> field_alias = new HashMap<>();
    private String classAlias;
    private List<String> insertIgnores = new ArrayList<>();
    private List<String> updateIgnores = new ArrayList<>();


    public ModelBuilder(Class<T> clazz){
        this.clazz = clazz;
        String canonicalName = clazz.getCanonicalName();
        className = canonicalName.split("\\.")[canonicalName.split("\\.").length-1];
        classAlias = className.toLowerCase();
        tableAlias = className;
        database = null;
    }

    public void initialize(){
        String location;
        if(database == null)
            location = tableAlias;
        else
            location = database+"."+tableAlias;

        //Todo autofill blob columns

    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public void setTableAlias(String tableAlias) {
        this.tableAlias = tableAlias;
    }

    public String getClassAlias() {
        return classAlias;
    }

    public void setClassAlias(String classAlias) {
        this.classAlias = classAlias;
    }

    public void addPushAlias(String fieldName, String columnName){
        field_alias.put(fieldName,columnName);
    }
    public void addPullAlias(String fieldName, String columnName){
        field_alias.put(fieldName,columnName);
    }
    public void addInsertIgnore(String fieldToIgnore){
        insertIgnores.add(fieldToIgnore);
    }
    public void addUpdateIgnore(String fieldToIgnore){
        updateIgnores.add(fieldToIgnore);
    }
    public void addPullIgnore(String columnToIgnore){
        pullIgnores.add(columnToIgnore);
    }
    public void addBlobField(String field){ blobs.add(field);}

    ///////////////////////////////////     Model      ////////////////////////////////////////////
    ///////////////////////////////////     Model      ////////////////////////////////////////////
    ///////////////////////////////////     Model      ////////////////////////////////////////////

    /**
     * Returns list of objects of type T, built from rs
     * @param rs
     * @return
     */
    public List<T> toObjectList(ResultSet rs)throws SQLException{

        List<T> list = new ArrayList<>();
        while(rs.next()){
            T obj = toObject(rs);
            if(obj!=null)
                list.add(obj);
        }
        return list;
    }

    /**
     * Returns object of type T, built from rs
     * @param rs
     * @return
     */
    public T toObject(ResultSet rs) throws SQLException {
        try {
            T obj = clazz.newInstance();

            ResultSetMetaData rsmd = rs.getMetaData();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                String fieldName = rsmd.getColumnName(i);
                if(column_alias.containsKey(fieldName))
                    fieldName = column_alias.get(fieldName);
                setField(obj, rs, fieldName);
            }

            return obj;
        }catch (IllegalAccessException ie){
            //shouldn't happen
            return null;
        }catch (InstantiationException i){
            //shoudn't happen
            return null;
        }
    }


    /**
     * Sets obj.<fieldName> to rs.get*(fieldName)
     * @param obj
     * @param rs
     * @param fieldName
     */
    private void setField(T obj, ResultSet rs, String fieldName){
        try {
            Field field = clazz.getDeclaredField(fieldName);
            Class type = field.getType();
            boolean isAccessible = field.isAccessible();
            field.setAccessible(true);
            if (type.getName().contains("String")) {
                String value = rs.getString(fieldName);
                value.replaceAll("'","\\'");
                field.set(obj, value);
            } else if (type.getName().contains("double")) {
                double value = rs.getDouble(fieldName);
                field.set(obj, value);
            }else if(type.getName().contains("BufferedImage")){
                InputStream is = rs.getBinaryStream(fieldName);
                BufferedImage bi = ImageIO.read(is);
                field.set(obj,bi);
            }else if(type.getName().contains("int")){
                int value = rs.getInt(fieldName);
                field.set(obj, value);
            }else if(blobs.contains(fieldName) && obj instanceof Blobable){
                ((Blobable)obj).pullBlob(fieldName,rs.getBinaryStream(fieldName));
            }
            field.setAccessible(isAccessible);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    ///////////////////////////////////     Insert      ////////////////////////////////////////////
    ///////////////////////////////////     Insert      ////////////////////////////////////////////
    ///////////////////////////////////     Insert      ////////////////////////////////////////////

    /**
     * Creates an insert SQL statement for the provided object
     * @param obj
     * @return
     */
    public PreparedStatement toInsertStatement(T obj,Connection con,String...ignoreFields)throws Exception{
        String insert = "insert into %s %s;";
        String location;

        if(database==null)
            location = classAlias;
        else
            location = database+"."+classAlias;

        HashMap<String,Boolean> ignoreFieldsAL = new HashMap<>();
        for(String s: ignoreFields){
            ignoreFieldsAL.put(s,true);
        }
        HashMap<String,Object> valueMap = collectInsertValues(obj,location,ignoreFieldsAL);
        String values = populateInsertValues(obj,valueMap);

        insert = String.format(insert,location,values);
        PreparedStatement ps = con.prepareStatement(insert);

        if(obj instanceof Blobable)
        fillBlobs(obj,valueMap,ps);

        return ps;
    }

    private HashMap<String,Object> collectInsertValues(T obj,String location,HashMap<String,Boolean> ignore)throws Exception{

        String query = String.format("describe %s",location);
        Connection con = SQLBasics.getConnection();
        ResultSet rs = con.createStatement().executeQuery(query);
        HashMap<String,Object> fields = new HashMap<>();
        while (rs.next()){
            String fieldName = rs.getString(1);
            if(field_alias.containsKey(fieldName))
                fieldName = field_alias.get(fieldName);

            //ignore field?
            if(insertIgnores.contains(fieldName)||ignore.containsKey(fieldName))
                continue;

            //get field and make it accessible
            Field field=null;
            try {
                field = clazz.getDeclaredField(fieldName);
            }catch (Exception e){
                continue;
            }
            boolean isAccessible = field.isAccessible();
            field.setAccessible(true);

            //get the value of the field from obj
            Object val = field.get(obj);

            //if field is null, don't insert it
            if(val == null)
                continue;


            fields.put(fieldName,val);
            field.setAccessible(isAccessible);
        }
        con.close();

        return fields;
    }

    private String populateInsertValues(Object obj, HashMap<String,Object> values){
        String insert = "(%s) values (%s)";
        int i = 0;
        Iterator<String> iter = values.keySet().iterator();

        String keys = "";
        String vals = "";
        while(iter.hasNext()){
            String key = iter.next();
            Object val = values.get(key);
            String toAdd = null;
            if(field_alias.containsKey(key))
                key = field_alias.get(key);

            if(val.getClass().getName().contains("String")) {
                toAdd =  String.format("'%s'", val.toString().replaceAll("'", "\\\\'"));
            }else if(obj instanceof Blobable && blobs.contains(key)){
                toAdd =  "?";
            }else if(val instanceof Number){
                toAdd = val.toString();
            }


            if(toAdd==null)
                continue;


            if(i > 0){
                keys += ", ";
                vals += ", ";
            }
            keys += key;
            vals += toAdd;
            i++;
        }

        return String.format(insert,keys,vals);
    }

    private void fillBlobs(T obj, HashMap<String,Object> values, PreparedStatement ps)throws Exception{
        int i = 1;
        for(String b: blobs){
            InputStream is = ((Blobable)obj).pushBlob(b,values.get(b));
            if(is==null)continue;
            ps.setBinaryStream(i++,is,is.available());
        }
    }

    ///////////////////////////////////     Update      ////////////////////////////////////////////
    ///////////////////////////////////     Update      ////////////////////////////////////////////
    ///////////////////////////////////     Update      ////////////////////////////////////////////

    /**
     * Creates an insert SQL statement for the provided object
     * @param obj
     * @return
     */
    public PreparedStatement toUpdateStatement(T obj,Connection con,String...ignoreFields)throws Exception{
        String insert = "update %s %s;";
        String location;

        if(database==null)
            location = classAlias;
        else
            location = database+"."+classAlias;

        HashMap<String,Boolean> ignoreFieldsAL = new HashMap<>();
        for(String s: ignoreFields){
            ignoreFieldsAL.put(s,true);
        }
        HashMap<String,Object> valueMap = collectUpdateValues(obj,location,ignoreFieldsAL);
        String values = populateUpdateValues(obj,valueMap);

        insert = String.format(insert,location,values);
        PreparedStatement ps = con.prepareStatement(insert);

        if(obj instanceof Blobable)
            fillBlobs(obj,valueMap,ps);

        return ps;
    }

    private HashMap<String,Object> collectUpdateValues(T obj,String location,HashMap<String,Boolean> ignore)throws Exception{

        String query = String.format("describe %s",location);
        Connection con = SQLBasics.getConnection();
        ResultSet rs = con.createStatement().executeQuery(query);
        HashMap<String,Object> fields = new HashMap<>();
        while (rs.next()){
            String fieldName = rs.getString(1);
            if(field_alias.containsKey(fieldName))
                fieldName = field_alias.get(fieldName);

            //ignore field?
            if(insertIgnores.contains(fieldName)||ignore.containsKey(fieldName))
                continue;

            //get field and make it accessible
            Field field=null;
            try {
                field = clazz.getDeclaredField(fieldName);
            }catch (Exception e){
                continue;
            }
            boolean isAccessible = field.isAccessible();
            field.setAccessible(true);

            //get the value of the field from obj
            Object val = field.get(obj);

            //if field is null, don't insert it
            if(val == null)
                continue;


            fields.put(fieldName,val);
            field.setAccessible(isAccessible);
        }
        con.close();

        return fields;
    }

    private String populateUpdateValues(Object obj, HashMap<String,Object> values){
        int i = 0;
        Iterator<String> iter = values.keySet().iterator();

        String updates = "";
        while(iter.hasNext()){
            String key = iter.next();
            Object val = values.get(key);
            String toAdd = null;
            if(field_alias.containsKey(key))
                key = field_alias.get(key);

            if(val.getClass().getName().contains("String")) {
                toAdd =  String.format("'%s'", val.toString().replaceAll("'", "\\\\'"));
            }else if(obj instanceof Blobable && blobs.contains(key)){
                toAdd =  "?";
            }else if(val instanceof Number){
                toAdd = val.toString();
            }


            if(toAdd==null)
                continue;


            if(i > 0){
                updates += ", ";
            }
            updates += key + " = " + toAdd;
            i++;
        }

        return "set "+updates;
    }


}
