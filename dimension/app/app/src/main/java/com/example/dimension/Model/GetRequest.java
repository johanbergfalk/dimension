package com.example.dimension.Model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;



/**
 * Class used to get data via REST from python server.
 * @author Robert Nilsson
 */

public class GetRequest {

    private String piAddress = "";
    private static HttpURLConnection connection;
    private ObjectBuilder[] receivedObjects;
    private URL url;
    private boolean connected = true;

    /**
     * Constructor
     * @param piAddress the address to the server
     */
    public GetRequest(String piAddress) throws MalformedURLException {

        this.piAddress = "http://" + piAddress + ":5000/objects";

    }

    /**
     * Establish connection to raspberry server and get REST stream
     * @throws Exception
     */
    public void getObjects() throws Exception {

        int status = 0;
        BufferedReader reader;
        String line = "";
        StringBuilder responseContent = new StringBuilder();

        try {
            url = new URL(piAddress);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);



            status = connection.getResponseCode(); //We want the code 200 for successful connection

            //Collect errors
            if(status > 299){
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                while((line = reader.readLine()) != null){
                    responseContent.append(line);
                }
                reader.close();
            //Get whole JSON and store in stream
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while((line = reader.readLine()) != null){
                    responseContent.append(line);
                }
                reader.close();
            }

        //Catch errors but continue to run and show appropriate message in app
        } catch (MalformedURLException e) {
            e.printStackTrace();
            connected = false;
            receivedObjects = allObjects("IP");
        } catch (NullPointerException e){
            connected = false;
            receivedObjects = allObjects("[]");
        } catch (ConnectException e) {
            connected = false;
            receivedObjects = allObjects("IP");
        } catch (SocketTimeoutException e){
            connected = false;
            receivedObjects = allObjects("IP");
        }finally {
        connection.disconnect();
        }

        if(connected){
            receivedObjects = allObjects(responseContent.toString());
        }
    }

    //Extract the stream and make new object with the data
    public ObjectBuilder[] allObjects(String response){
        //If no objects are found, create an No object found "object"
        if(response.equals("[]")){
            response = "[{'objectType': 'No object found', 'height': 0, 'width': 0, 'distance': 0}]";
        }
        //If disconnected from server create on Server not found "object"
        if(response.equals("IP")){
            response = "[{'objectType': 'Server not found', 'height': 0, 'width': 0, 'distance': 0}]";
        }
        final GsonBuilder gsonBuilder = new GsonBuilder();
        final Gson gson = gsonBuilder.create();
        return gson.fromJson(response, ObjectBuilder[].class);

    }

    public ObjectBuilder[] getAllObjects() throws Exception {
        getObjects();
        return this.receivedObjects;
    }
}