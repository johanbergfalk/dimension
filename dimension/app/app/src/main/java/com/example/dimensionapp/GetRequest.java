package com.example.dimensionapp;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Class used to get data via REST from python server.
 * @author Robert Nilsson
 */

public class GetRequest {

    private String piAddress = "http://192.168.68.109:5000/objects";
    private static HttpURLConnection connection;
    private DetectedObject[] allObjects;
    private URL url;
    private JSONArray detectedObjects;
    String response;

    /**
     * Constructor
     * @param piAddress the address to the server
     */
    public GetRequest(String piAddress) throws MalformedURLException {
        //TODO IP should be provided
        //this.piAddress = piAddress;

    }

    public int getObjects() throws Exception { //TODO change return type to JSONArray

        int status = 0;
        BufferedReader reader;
        String line;
        StringBuilder responseContent = new StringBuilder();

        try {
            url = new URL(piAddress);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); //TODO - Find "good" values here
            connection.setReadTimeout(5000); //TODO - Find "good" values here

            status = connection.getResponseCode(); //We want the code 200 for successful connection

            if(status > 299){
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                while((line = reader.readLine()) != null){
                    responseContent.append(line);
                }
                reader.close();
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while((line = reader.readLine()) != null){
                    responseContent.append(line);
                }
                reader.close();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
        detectedObjects = new JSONArray(responseContent.toString());

        return status;
    }

    public String parse() throws JSONException {

        String allJason = "";

        for (int i = 0; i < 2; i++){
            JSONObject detectedObject = detectedObjects.getJSONObject(i);

            String objectType = detectedObject.getString("objectType");
            int height = detectedObject.getInt("height");
            int width = detectedObject.getInt("width");
            int distance = detectedObject.getInt("distance");

            allJason += "Object: " + objectType + " Height: " + height + " Width: " + width + " Distance: " + distance + "\n";

        }
        return allJason;
    }

    public DetectedObject[] getAllObjects() throws Exception{ //TODO - for later use
        final GsonBuilder gsonBuilder = new GsonBuilder();
        final Gson gson = gsonBuilder.create();
        allObjects = gson.fromJson(response, DetectedObject[].class);
        return allObjects;
    }

}
