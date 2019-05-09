package com.lazytools.sqlmodeling.sql.modeling;

public abstract class ModelBase<T> {

    protected Class<T> clazz;
    protected String className;
    protected Strategy strategy;


    public ModelBase(Class<T> clazz){
        this.clazz = clazz;
        String canonicalName = clazz.getCanonicalName();
        className = canonicalName.split("\\.")[canonicalName.split("\\.").length-1];
        strategy = new Strategy();
    }

    public Strategy getStrategy(){
        return strategy;
    }

    public void setStrategy(Strategy strategy){
        this.strategy = strategy;
    }
}
