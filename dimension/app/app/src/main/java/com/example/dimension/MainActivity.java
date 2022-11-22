package com.example.dimension;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dimension.ViewModel.ObjectViewModel;

public class MainActivity extends AppCompatActivity {

    Button show;
    ImageView objectImage;
    TextView objectTitle;
    TextView dimensionText;
    TextView distanceText;
    TextView detailsText;

    ObjectViewModel objectViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        objectViewModel = new ViewModelProvider(this).get(ObjectViewModel.class);

        detailsText = findViewById(R.id.objectDetailsText);
        show = findViewById(R.id.show);

        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

    }

    private void showDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_dialog);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.setCancelable(false);

        try {
            objectViewModel.displayObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        objectImage = findViewById(R.id.objectImage);
        objectTitle = dialog.findViewById(R.id.objectTitle);
        dimensionText = dialog.findViewById(R.id.dimensionText);
        distanceText = dialog.findViewById(R.id.distanceText);

        objectImage.setBackgroundResource(objectViewModel.getObjectImage());
        objectTitle.setText(objectViewModel.getObjectTitle());
        dimensionText.setText(objectViewModel.getDimensionText());
        distanceText.setText(objectViewModel.getDistanceText());

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
    }

    private void fadeInAndShowImage(final ImageView img) {

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateInterpolator());
        fadeIn.setDuration(500);

        fadeIn.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                img.setVisibility(View.VISIBLE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        img.startAnimation(fadeIn);
    }

    private void fadeOutAndHideImage(final ImageView img) {

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(500);

        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                img.setVisibility(View.GONE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        img.startAnimation(fadeOut);
    }
}