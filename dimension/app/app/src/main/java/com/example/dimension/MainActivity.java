package com.example.dimension;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dimension.Model.BitmapDecode;
import com.example.dimension.Model.ConnectionState;
import com.example.dimension.Model.OneObject;
import com.example.dimension.ViewModel.ObjectViewModel;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {

    Button show;
    ImageButton settings;
    ImageView objectImage;
    TextView objectTitle;
    TextView dimensionText;
    TextView distanceText;
    TextView detailsText;
    Timer timer1;
    boolean connected = false;
    String ipAddress = "192.168.68.123"; //This is default address of raspberryPi. Possible to change in app
    ObjectViewModel objectViewModel;
    ArrayList<OneObject> allObjects = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        objectViewModel = new ViewModelProvider(this).get(ObjectViewModel.class);


        detailsText = findViewById(R.id.objectDetailsText);
        show = findViewById(R.id.show);
        settings = findViewById(R.id.ipButton);

        settings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                setIp();
            }
        });


        show.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        checkConnectedState();
        setConnectedState();

    }

    //Displays the detected object with measurements
    private void showDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_dialog);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.setCancelable(false);

        objectViewModel.setIpAddress(ipAddress);

        try {
            objectViewModel.displayObject();
            allObjects = objectViewModel.getObjects();
        } catch (Exception e) {
            e.printStackTrace();
        }

        OneObject obj = allObjects.get(0);
        allObjects.remove(0);

        //Get JSON String data and convert it to an image
        Bitmap detectedObject = BitmapDecode.stringToBitmap(obj.getObjectImage());
        ImageView detected = (ImageView) findViewById(R.id.imageOfDetectedObject);
        detected.setImageBitmap(detectedObject);

        //sets image of detected object
        switch (obj.getTitle()) {

            case "No object found":
                objectImage = findViewById(R.id.nothingImage);
                break;
            case "Server not found":
                objectImage = findViewById(R.id.serverNotFound);
                break;
            default:
                objectImage = detected;
                break;
        }
        
        objectTitle = dialog.findViewById(R.id.objectTitle);
        dimensionText = dialog.findViewById(R.id.dimensionText);
        distanceText = dialog.findViewById(R.id.distanceText);

        objectTitle.setText(obj.getTitle());
        dimensionText.setText(obj.getDimensions());
        distanceText.setText(obj.getDistance());

        show.setVisibility(View.GONE);
        detailsText.setText(R.string.object_details);

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        fadeInAndShowImage(objectImage);

        Button buttonClose = dialog.findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                fadeOutAndHideImage(objectImage);
                show.setVisibility(View.VISIBLE);
                detailsText.setText(R.string.looking_for_object);
            }
        });

        //If only one object is detected, the "Next" button is not shown
        Button buttonNext = dialog.findViewById(R.id.buttonNext);
        if(allObjects.size() < 1){
            buttonNext.setVisibility(View.INVISIBLE);
            buttonNext.setClickable(false);
        }

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fadeOutAndHideImage(objectImage);
                showNextObject(buttonNext);
                }

        });
    }

    //Displays the next detected object in the list received from raspberryPie
    private void showNextObject(Button next){

        OneObject obj = allObjects.get(0);
        allObjects.remove(0);

        //Get JSON String data and convert it to an image
        Bitmap detectedObject = BitmapDecode.stringToBitmap(obj.getObjectImage());
        ImageView detected = (ImageView) findViewById(R.id.imageOfDetectedObject);
        detected.setImageBitmap(detectedObject);

        //sets image of detected object
        switch (obj.getTitle()) {

            case "No object found":
                objectImage = findViewById(R.id.nothingImage);
                break;
            case "Server not found":
                objectImage = findViewById(R.id.serverNotFound);
                break;
            default:
                objectImage = detected;
                break;
        }

        objectTitle.setText(obj.getTitle());
        dimensionText.setText(obj.getDimensions());
        distanceText.setText(obj.getDistance());

        fadeInAndShowImage(objectImage);

        if(allObjects.isEmpty()){
            next.setVisibility(View.INVISIBLE);
            next.setClickable(false);
        }

    }

    private void fadeInAndShowImage(final ImageView img) {

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateInterpolator());
        fadeIn.setDuration(500);

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                img.setVisibility(View.VISIBLE);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });

        img.startAnimation(fadeIn);
    }

    private void fadeOutAndHideImage(final ImageView img) {

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(500);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                img.setVisibility(View.GONE);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });

        img.startAnimation(fadeOut);
    }

    private void setIp() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Enter server IP address");

        final EditText input = new EditText(MainActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Current IP: " + ipAddress);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ipAddress = input.getText().toString();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    //"ping" raspberryPi to check if it is reachable and can respond to REST connection
    private void checkConnectedState() {

        //Running in loop to continuously test connection state with server
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            ConnectionState serverConnection;

            @Override
            public void run() {
                timer1 = new Timer();
                timer1.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        serverConnection = new ConnectionState(ipAddress);
                        try {
                            connected = serverConnection.checkState();
                        } catch (Exception e) {
                            connected = false;
                        }
                    }
                }, 1000, 2000);
            }
        });
    }

    //Shows the state as a red(connected) or green(not connected) led at the home screen depending on connected state to raspberryPi
    private void setConnectedState(){
        //Create the 2 views for red and green led
        ImageView connectedToServer = findViewById(R.id.connectedView);;
        ImageView notConnectedToSer = findViewById(R.id.notConnectedView);

        Handler h = new Handler();
        int delay = 1000; //milliseconds

        h.postDelayed(new Runnable(){
            public void run(){
                connectedToServer.setVisibility(connected ? View.VISIBLE : View.GONE);
                notConnectedToSer.setVisibility(connected ? View.GONE : View.VISIBLE);
                h.postDelayed(this, delay);
            }
        }, delay);
    }
}