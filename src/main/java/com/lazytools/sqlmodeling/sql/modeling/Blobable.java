package com.lazytools.sqlmodeling.sql.modeling;

import java.io.InputStream;

public interface Blobable {

    /**
     * The {@Link ModelBuilder} will call this function when retrieving model information from the db
     * @param column - The name of the field you should fill
     * @param blob - The data you should fill the field with
     */
    public void pullBlob(String column, InputStream blob);

    /**
     * The {@Link ModelBuilder} will call this function when retrieving model information from the db
     * @param column - The name of the field you should fill
     * @param blob - The data you should fill the field with
     */
    public InputStream pushBlob(String column, Object blob);
}
