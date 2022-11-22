package com.example.dimension.ViewModel;

import androidx.lifecycle.ViewModel;

import com.example.dimension.Model.ObjectBuilder;
import com.example.dimension.R;

import java.util.ArrayList;

public class ObjectViewModel extends ViewModel {

    String objectTitle;
    String dimensionText;
    String distanceText;

    public void displayObject() throws Exception {

        //GetRequest request = new GetRequest("");
        //ObjectBuilder[] builder = request.getAllObjects();

        //for testing purposes only
        ObjectBuilder object = new ObjectBuilder();
        ArrayList<ObjectBuilder> builder = new ArrayList<>();
        builder.add(object);

        for(ObjectBuilder b : builder) {
            objectTitle = b.getObjectType();
            dimensionText = b.getHeight() + " X " + b.getWidth() + " cm";
            distanceText = b.getDistance() + " cm";

        }
    }

    public String getObjectTitle() {
        return objectTitle;
    }

    public void setObjectTitle(String objectTitle) {
        this.objectTitle = objectTitle;
    }

    public String getDimensionText() {
        return dimensionText;
    }

    public void setDimensionText(String dimensionText) {
        this.dimensionText = dimensionText;
    }

    public String getDistanceText() {
        return distanceText;
    }

    public void setDistanceText(String distanceText) {
        this.distanceText = distanceText;
    }

}
