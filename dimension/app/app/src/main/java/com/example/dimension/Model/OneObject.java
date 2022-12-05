package com.example.dimension.Model;

/**
 * Used to create one detected object when several objects are detected *
 * @author Robert Nilsson
 */
public class OneObject {

    private String title;
    private String dimensions;
    private String distance;

    public OneObject(String title, String dimensions, String distance){
        this.title = title;
        this.dimensions = dimensions;
        this.distance = distance;
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
}
