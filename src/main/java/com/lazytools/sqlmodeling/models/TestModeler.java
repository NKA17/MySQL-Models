package com.lazytools.sqlmodeling.models;


import com.lazytools.sqlmodeling.sql.modeling.ModelController;
import com.lazytools.sqlmodeling.sql.modeling.Strategy;

public class TestModeler extends ModelController<Test> {

    public static final TestModeler MODELER = new TestModeler();

    public TestModeler(){
        super(Test.class);
        Strategy strategy = new Strategy();
        strategy.getBlobFields().add("image");
        strategy.getRelevantFields().add("name");
        strategy.getRelevantFields().add("id");
        strategy.setTable("test");
        strategy.setDatabase("testdb");

        distributeStrategey(strategy);

        getInsertStrategy().getRelevantFields().remove("id");
        getUpdateStrategy().getRelevantFields().remove("id");
    }
}
