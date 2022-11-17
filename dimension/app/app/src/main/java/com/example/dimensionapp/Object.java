package com.example.dimensionapp;

/**
 * Class to make objects for each identified object found by camera
 * and lidar.
 * @author Erik Gustavsson
 */
public class Object {

    enum Type {
        Car,
        Person,
        Tree,
        Bus
    }
    private int height, weight, distance;
    private Type type;

    //Constructor
    public Object(int x, int y, int d,Type t){
            height = x;
            weight = y;
            distance = d;
            type = t;
    }
}
