package com.baboolian.demo.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.baboolian.demo.R;
import com.bumptech.glide.Glide;

public class ResumeActivity extends AppCompatActivity {

    private static final String TAG = ResumeActivity.class.getSimpleName();

    private static final String RESUME_FILE_NAME = "resume.pdf";

    public static void start(Context context) {
        Intent intent = new Intent(context, ResumeActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_collapsing_toolbar);
        collapsingToolbarLayout.setTitle(getString(R.string.resume_title));
        collapsingToolbarLayout.setExpandedTitleColor(ContextCompat.getColor(this, android.R.color.transparent));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.resume_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setType("text/plain");
                intent.setData(Uri.parse("mailto:julian.claudino@gmail.com"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(ResumeActivity.this, R.string.resume_no_email_client, Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageView resumeImage = (ImageView) findViewById(R.id.resume_image);

        Glide.with(this)
                .load(Uri.parse("file:///android_asset/resume.png"))
                .placeholder(R.drawable.ic_photo_placeholder)
                .into(resumeImage);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
