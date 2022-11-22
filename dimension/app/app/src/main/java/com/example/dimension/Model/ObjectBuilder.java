package com.example.dimension.Model;

/**
 * Class used when to "store/create" JSON object
 * @author Robert Nilsson
 *
 */

public class ObjectBuilder {

    private String objectType;
    private int height;
    private int width;
    private int distance;

    /*public ObjectBuilder() {
        this.objectType = "Person";
        this.height = 150;
        this.width = 130;
        this.distance = 1000;
    }*/


    //---- Getters and Setters ------------------------------------
    public String getObjectType(){
        return objectType;
    }

    public void setObjectType(String objectType){
        this.objectType = objectType;
    }

    public int getHeight(){
        return height;
    }

    public void setHeight(int height){
        this.height = height;
    }

    public int getWidth(){
        return width;
    }

    public void setWidth(int width){
        this.width = width;
    }

    public int getDistance(){
        return this.distance;
    }

    public void setDistance(int distance){
        this.distance = distance;
    }

}

