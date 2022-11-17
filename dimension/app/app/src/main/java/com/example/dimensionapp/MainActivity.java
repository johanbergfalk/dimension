package com.example.dimensionapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main class. This sets the first activity page in the app.
 * In this phase the app only creates random ints to make it look like
 * new inputs. These inputs are to be replaced with actuall input from raspberry pi.
 * @author Erik Gustavsson
 */
public class MainActivity extends AppCompatActivity {

    private DimObject obj = new DimObject(0,0,0,DimObject.Type.Car);
    private Timer timer;
    private String text = "";
    private TextView objectView;

    /**
     * This is the init method when app starts
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //This sets current view
        setContentView(R.layout.activity_main);

        //Declaring activit view to corresponding view objects
        objectView = findViewById(R.id.textViewObjects);

        //Creating a new branch to run a timer in the background, timer running objectgenerator
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                timer = new Timer();
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        Objectgenerator();
                    }
                },1000, 2000);
            }
        });
    }

    /**
     * A test method to print out the "received" objects.
     */
    private void Objectgenerator() {
        Random rand = new Random();

        int randx = rand.nextInt(100);
        int randy = rand.nextInt(100);
        int randd = rand.nextInt(10);

        obj = new DimObject(randx, randy, randd, DimObject.Type.Car);
        text = text + "\n" + obj.getString(obj);
        objectView.setText(text);
    }


}