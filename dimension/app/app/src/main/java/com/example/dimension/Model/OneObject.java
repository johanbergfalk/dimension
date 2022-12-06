package com.example.dimension.Model;

/**
 * Used to create one detected object.
 * @author Robert Nilsson
 */
public class OneObject {

    private final String title;
    private final String objectImage;
    private final String dimensions;
    private final String distance;

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
