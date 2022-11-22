package com.example.dimension.Model;

/**
 * Class to make objects for each identified object found by camera
 * and lidar.
 * @author Erik Gustavsson
 */
public class DimObject {

    enum Type {
        Car,
        Person,
        Tree,
        Bus;
    }
    private int height, width, distance;
    private Type type;

    //Constructor
    public DimObject(int x, int y, int d,Type t){
        height = x;
        width = y;
        distance = d;
        type = t;
    }

    public String getString(DimObject o){
        String text = o.height + "cm, " + o.width + "cm, " + o.distance + "cm, " + "Object = " +o.type.toString();
        return text;
    }
}
