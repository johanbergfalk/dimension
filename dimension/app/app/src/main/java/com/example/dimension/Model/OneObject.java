package com.example.dimension.Model;

/**
 * Used to create one detected object when several objects are detected *
 * @author Robert Nilsson
 */
public class OneObject {

    private String title;
    private String objectImage;
    private String dimensions;
    private String distance;

    public OneObject(String title, String dimensions, String distance, String image){
        this.title = title;
        this.dimensions = dimensions;
        this.distance = distance;
        this.objectImage = image;
    }

    public String getTitle() {
        return title;
    }

    public String getDimensions() {
        return dimensions;
    }

    public String getDistance() {
        return distance;
    }

    public String getObjectImage(){return objectImage;}
}
