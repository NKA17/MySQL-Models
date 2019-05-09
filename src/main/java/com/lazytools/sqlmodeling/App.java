package com.lazytools.sqlmodeling;

import com.lazytools.sqlmodeling.models.Test;
import com.lazytools.sqlmodeling.models.TestModeler;
import com.lazytools.sqlmodeling.sql.basic.SQLBasics;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )throws Exception
    {

        Connection con = SQLBasics.getConnection();


        BufferedImage bi = ImageIO.read(new File("C:\\Users\\Nate\\Pictures\\Project Photos\\meat.jpeg"));


        TestModeler tm = new TestModeler();
        ResultSet rs = con.createStatement().executeQuery("Select * from testdb.test where id = 10");
        rs.next();
        Test t = tm.toObject(rs);


        t.setImage(bi);
        PreparedStatement ps = tm.toUpdateStatement(t,con);
//        ps.executeUpdate();
        System.out.println(ps.toString());

        //displayPicture(t.getImage());

        con.close();
    }

    public static void displayPicture(BufferedImage img){
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.getContentPane().setLayout(new GridLayout(1, 1));
        ImagePanel panel = new ImagePanel(img);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }
    static class ImagePanel extends JPanel
    {
        private final BufferedImage image;

        ImagePanel(BufferedImage image)
        {
            this.image = image;
        }

        @Override
        public Dimension getPreferredSize()
        {
            if (super.isPreferredSizeSet())
            {
                return super.getPreferredSize();
            }
            return new Dimension(image.getWidth(), image.getHeight());
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, null);
        }
    }
}
