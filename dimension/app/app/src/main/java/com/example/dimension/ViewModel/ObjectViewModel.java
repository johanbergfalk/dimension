package com.example.dimension.ViewModel;

import androidx.lifecycle.ViewModel;

import com.example.dimension.Model.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ObjectViewModel extends ViewModel{

    String objectTitle;
    String dimensionText;
    String distanceText;
    GetRequest request;
    ObjectBuilder[] builder;

    public void displayObject() throws Exception {

        request = new GetRequest("");

        //Run the networkservice in separate thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    builder = request.getAllObjects();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        future.get();

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
