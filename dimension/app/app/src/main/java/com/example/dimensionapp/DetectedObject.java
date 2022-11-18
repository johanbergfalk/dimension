package com.example.dimensionapp;

/**
 * Class that "store" JSON object
 * @author Robert Nilsson
 *
 */

public class DetectedObject {

    private String objectType;
    private String height;
    private String width;
    private String distance;


    //---- Getters and Setters ------------------------------------
    public String getObjectType(){
        return objectType;
    }

    public void setObjectType(String objectType){
        this.objectType = objectType;
    }

    public String getHeight(){
        return height;
    }

    public void setHeight(String height){
        this.height = height;
    }

    public String getWidth(){
        return width;
    }

    public void setWidth(String width){
        this.width = width;
    }

    public String getDistance(){
        return this.distance;
    }

    public void setDistance(String distance){
        this.distance = distance;
    }

}
