package com.lw.custom;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lw.custom.widget.StarView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StarView starView = findViewById(R.id.star_view);
        starView.setRating(4.5);
        starView.setStarCount(10);
        starView.setHalf(true);
        starView.setChange(true);
    }
}
