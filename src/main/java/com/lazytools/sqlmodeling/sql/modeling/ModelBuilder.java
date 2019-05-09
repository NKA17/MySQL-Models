package com.lazytools.sqlmodeling.sql.modeling;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ModelBuilder<T> extends ModelBase<T> {


    //TODO build support for each of these guys https://www.tutorialspoint.com/mysql/mysql-data-types.htm

    public ModelBuilder(Class<T> tClass){
        super(tClass);
    }

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
                if(strategy.getAliases().containsKey(fieldName))
                    fieldName = strategy.getAliases().get(fieldName);
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
            }else if(strategy.getBlobFields().contains(fieldName) && obj instanceof Blobable){
                ((Blobable)obj).pullBlob(fieldName,rs.getBinaryStream(fieldName));
            }
            field.setAccessible(isAccessible);
        }catch (Exception e){
            e.printStackTrace();
        }
    }



}
