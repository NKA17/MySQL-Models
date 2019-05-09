package com.lazytools.sqlmodeling.models;

import com.lazytools.sqlmodeling.sql.modeling.object.Blobable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class Test implements Blobable{
    private int id;
    private String name;
    private BufferedImage image;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    @Override
    public void pullBlob(String column, InputStream blob) {
        try {
            BufferedImage bi = ImageIO.read(blob);
            setImage(bi);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public InputStream pushBlob(String column, Object blob) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(getImage(), "png", out);
            byte[] buf = out.toByteArray();
            // setup stream for blob
            return new ByteArrayInputStream(buf);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
