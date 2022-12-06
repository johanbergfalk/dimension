package com.example.dimension.Model;

/**
 * Class used when to "store/create" JSON object
 * @author Robert Nilsson
 *
 */

public class ObjectBuilder {

    private String objectType;
    private String objectImage;
    private int height;
    private int width;
    private int distance;


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

    public String getObjectImage() { return this.objectImage; }

}

