package com.lazytools.sqlmodeling.models;

import com.lazytools.sqlmodeling.sql.modeling.object.ModelBuilder;

public class TestModeler extends ModelBuilder<Test> {

    public static final TestModeler MODELER = new TestModeler();

    public TestModeler(){
        super(Test.class);
        setDatabase("testdb");
        addBlobField("image");
        addInsertIgnore("id");
        addUpdateIgnore("id");
    }
}
