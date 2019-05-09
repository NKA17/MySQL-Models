package com.lazytools.sqlmodeling.sql.modeling;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ModelController<T>  {

    private ModelBuilder<T> builder;
    private ModelInserter<T> inserter;
    private ModelUpdater<T> updater;

    public ModelController(Class<T> clazz){
        builder = new ModelBuilder<>(clazz);
        inserter = new ModelInserter<>(clazz);
        updater = new ModelUpdater<>(clazz);

    }

    /**
     * Creates a clone of the given strategy for 'building' 'inserting' and 'updating'
     */
    public void distributeStrategey(Strategy strategy){
        builder.setStrategy(strategy.clone());
        inserter.setStrategy(strategy.clone());
        updater.setStrategy(strategy.clone());
    }

    public Strategy getBuildStrategy(){
        return builder.getStrategy();
    }

    public Strategy getInsertStrategy(){
        return inserter.getStrategy();
    }

    public Strategy getUpdateStrategy(){
        return updater.getStrategy();
    }

    public void setBuilderStragegy(Strategy s){
        builder.setStrategy(s);
    }

    public void setInsertStragegy(Strategy s){
        inserter.setStrategy(s);
    }

    public void setUpdateStragegy(Strategy s){
        updater.setStrategy(s);
    }

    /**
     * Returns object of type T, built from rs
     * @param rs
     * @return
     */
    public T toObject(ResultSet rs) throws SQLException{
        return builder.toObject(rs);
    }

    /**
     * Returns list of objects of type T, built from rs
     * @param rs
     * @return
     */
    public List<T> toObjectList(ResultSet rs)throws SQLException{
        return builder.toObjectList(rs);
    }


    /**
     * Creates an insert SQL statement for the provided object
     * @param obj
     * @return
     */
    public PreparedStatement toInsertStatement(T obj, Connection con, String...ignoreFields)throws Exception{
        return inserter.enter(obj,con,ignoreFields);
    }


    /**
     * Creates an insert SQL statement for the provided object
     * @param obj
     * @return
     */
    public PreparedStatement toUpdateStatement(T obj, Connection con, String...ignoreFields)throws Exception{
        return updater.enter(obj,con,ignoreFields);
    }
}
