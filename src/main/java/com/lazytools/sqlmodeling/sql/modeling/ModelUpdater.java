package com.lazytools.sqlmodeling.sql.modeling;

import javafx.util.Pair;

import java.util.List;

public class ModelUpdater<T> extends ModelSaver<T> {

    public ModelUpdater(Class<T> clazz){
        super(clazz);
    }

    @Override
    protected String buildKeyValueString(List<Pair<String, Object>> pairs) {
        int i = 0;

        String updates = "";
        for(Pair<String,Object> pair: pairs){
            String key = pair.getKey();
            Object val = pair.getValue();
            String toAdd = null;
            if(strategy.getAliases().containsKey(key))
                key = strategy.getAliases().get(key);

            if(val.getClass().getName().contains("String")) {
                toAdd =  String.format("'%s'", val.toString().replaceAll("'", "\\\\'"));
            }else if(strategy.getBlobFields().contains(key)){
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

    @Override
    protected String buildQuery(String keyValuePairs) {
        String location;

        if(strategy.getDatabase()==null)
            location = strategy.getTable();
        else
            location = strategy.getDatabase()+"."+strategy.getTable();

        return String.format("Update %s %s;",location,keyValuePairs);
    }

}
