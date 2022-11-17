package com.example.dimensionapp;

import java.util.Map;

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
    private int height, weight, distance;
    private Type type;

    //Constructor
    public DimObject(int x, int y, int d,Type t){
            height = x;
            weight = y;
            distance = d;
            type = t;
    }

    public String getString(DimObject o){
        String text = o.height + ", " + o.weight + ", " + o.distance + ", " + o.type.toString();
        return text;
    }
}
