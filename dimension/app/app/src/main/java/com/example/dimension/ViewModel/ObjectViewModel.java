package com.example.dimension.ViewModel;

import androidx.lifecycle.ViewModel;

import com.example.dimension.Model.*;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Handles requests to raspberryPi and stores all detected
 * objects in an Array.
 * @author Robert Nilsson
 */
public class ObjectViewModel extends ViewModel{

    String objectTitle;
    String dimensionText;
    String distanceText;
    String objectImage;
    GetRequest request;
    ObjectBuilder[] builder; //All received detected objects
    String ipAddress = "";
    ArrayList<OneObject> objects = new ArrayList<>();

    public void displayObject() throws Exception {

        request = new GetRequest(ipAddress);

        //Run the network-service in separate thread
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

        //Add all detected objects into the Array
        for(ObjectBuilder b : builder) {
            objectTitle = b.getObjectType();
            dimensionText = b.getHeight() + " X " + b.getWidth() + " cm";
            distanceText = b.getDistance() + " cm";
            objectImage = b.getObjectImage();
            objects.add(new OneObject(objectTitle, dimensionText, distanceText, objectImage));
        }
    }

    public void setIpAddress(String ipAddress){
        this.ipAddress = ipAddress;
    }

    public ArrayList<OneObject> getObjects(){
        return this.objects;
    }
}
