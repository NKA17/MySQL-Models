package com.lazytools.sqlmodeling.sql.modeling;

import javafx.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class ModelSaver<T> extends ModelBase<T> {
    public ModelSaver(Class<T> clazz){
        super(clazz);
    }


    /**
     * TODO
     * create field map
     * build string
     * fill blobs
     */
    /**
     * Creates an insert SQL statement for the provided object
     * @param obj
     * @return
     */
    public PreparedStatement enter(T obj, Connection con, String...ignoreFields)throws Exception{
        HashMap<String,Boolean> ignores = new HashMap<>();
        for(String s: ignoreFields){
            ignores.put(s,true);
        }

        List<Pair<String,Object>> pairs = buildPairs(obj,ignores);
        String keyValues = buildKeyValueString(pairs);
        String query = buildQuery(keyValues);
        PreparedStatement ps = con.prepareStatement(query);
        fillBlobs(obj,ps,pairs);

        return ps;
    }

    private List<Pair<String,Object>> buildPairs(T obj, HashMap<String,Boolean> ignore){

        List<Pair<String,Object>> pairs = new ArrayList<>();

        for(String field: strategy.getRelevantFields()){
            if(strategy.getAliases().containsKey(field))
                field = strategy.getAliases().get(field);

            //ignore field?
            if(ignore.containsKey(field))
                continue;

            Pair<String,Object> pair = createPair(obj,field);
            pairs.add(pair);
        }

        for(String field: strategy.getBlobFields()){

            if(strategy.getBlobAliases().containsKey(field))
                field = strategy.getBlobAliases().get(field);

            //ignore field?
            if(ignore.containsKey(field))
                continue;

            Pair<String,Object> pair = createPair(obj,field);
            pairs.add(pair);
        }

        return pairs;
    }

    private Pair<String,Object> createPair(T obj,String fieldName){
        try {
            //get field and make it accessible
            Field field = null;
            field = clazz.getDeclaredField(fieldName);
            boolean isAccessible = field.isAccessible();
            field.setAccessible(true);

            //get the value of the field from obj
            Object val = field.get(obj);

            //if field is null, don't insert it
            if (val == null)
                return null;

            field.setAccessible(isAccessible);
            Pair<String, Object> pair = new Pair<>(fieldName, val);

            return pair;
        }catch (Exception e){
            return null;
        }
    }

    protected abstract String buildKeyValueString(List<Pair<String,Object>> pairs);

    protected abstract String buildQuery(String keyValuePairs);

    protected void fillBlobs(T obj,PreparedStatement ps, List<Pair<String,Object>> pairs)throws IOException,SQLException{
        if(!( obj instanceof Blobable))
            return;

        HashMap<String,Object> values = new HashMap<>();
        for(Pair<String,Object> p: pairs){
            values.put(p.getKey(),p.getValue());
        }
        int i = 1;
        for(String b: strategy.getBlobFields()){
            InputStream is = ((Blobable)obj).pushBlob(b,values.get(b));
            if(is==null)continue;
            ps.setBinaryStream(i++,is,is.available());
        }
    }
}
