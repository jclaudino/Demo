package com.baboolian.demo.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.baboolian.demo.R;
import com.baboolian.demo.ui.fragment.MediaFragment;
import com.baboolian.demo.model.MediaStoreFile;

/**
 * This activity is only used on single pane mode for devices with small screens
 */
public class MediaActivity extends AppCompatActivity {

    private static final String TAG = MediaActivity.class.getSimpleName();

    private static final String EXTRA_MEDIA_FILE = "EXTRA_MEDIA_FILE";

    private MediaStoreFile file;

    public static void start(Context context, MediaStoreFile file) {
        Intent intent = new Intent(context, MediaActivity.class);
        intent.putExtra(EXTRA_MEDIA_FILE, file);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        if (savedInstanceState != null) {
            file = savedInstanceState.getParcelable(EXTRA_MEDIA_FILE);
        } else if (getIntent() != null) {
            file = getIntent().getParcelableExtra(EXTRA_MEDIA_FILE);
        } else {
            Log.e(TAG, "Activity created with no media file!", new IllegalArgumentException());
            return;
        }

        MediaFragment mediaFragment = (MediaFragment) getSupportFragmentManager().findFragmentById(R.id.main_media_fragment);
        mediaFragment.displayMedia(file);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState() called");
        outState.putParcelable(EXTRA_MEDIA_FILE, file);
        super.onSaveInstanceState(outState);
    }
}
