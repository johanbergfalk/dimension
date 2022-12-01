package com.example.dimension.Model;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

public class ConnectionState {

    private String piAddress = "";
    private static HttpURLConnection connection;
    private URL url;

    public ConnectionState(String piAddress){

        this.piAddress = "http://" + piAddress + ":5000/objects";

    }

    public boolean checkState() throws Exception{

        int status = 0;

        try {
            url = new URL(piAddress);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000); //TODO - Find "good" values here
            connection.setReadTimeout(2000); //TODO - Find "good" values here

            status = connection.getResponseCode(); //We want the code 200 for successful connection

            connection.disconnect();
            return status == 200;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ConnectException e){
            e.printStackTrace();
        } catch (SocketTimeoutException e){
            e.printStackTrace();
        }finally {
            connection.disconnect();
        }
        return false;
    }
}
