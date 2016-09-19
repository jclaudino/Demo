package com.baboolian.demo.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.baboolian.demo.R;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private Handler handler;
    private Runnable splashRunnable;

    @Override
    protected void onCreate(Bundle savedInstancestate) {
        Log.d(TAG, "onCreate() called");
        super.onCreate(savedInstancestate);
        setContentView(R.layout.activity_splash);
        handler = new Handler();
        splashRunnable = new Runnable() {
            @Override
            public void run() {
                MainActivity.start(SplashActivity.this);
            }
        };
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() called");
        super.onResume();
        handler.postDelayed(splashRunnable, 2000);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause() called");
        super.onPause();
        handler.removeCallbacks(splashRunnable);
    }
}
