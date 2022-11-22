package com.example.dimension.ViewModel;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.ViewModel;

import com.example.dimension.Model.GetRequest;
import com.example.dimension.Model.ObjectBuilder;
import com.example.dimension.R;

import java.util.ArrayList;

public class ObjectViewModel extends ViewModel {

    int objectImage;
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
            switch(b.getObjectType()) {
                case "Car":
                    objectImage = R.drawable.porsche;
                    objectTitle = b.getObjectType();
                    dimensionText = b.getHeight() + " X " + b.getWidth() + " cm";
                    distanceText = b.getDistance() + " cm";
                    break;

                case "Person":
                    objectImage = R.drawable.ic_distance;
                    objectTitle = b.getObjectType();
                    dimensionText = b.getHeight() + " X " + b.getWidth() + " cm";
                    distanceText = b.getDistance() + " cm";
                    break;
            }
        }
    }

    public int getObjectImage() {
        return objectImage;
    }

    public void setObjectImage(int objectImage) {
        this.objectImage = objectImage;
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
