package com.baboolian.demo.ui.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.baboolian.demo.DemoApplication;
import com.baboolian.demo.R;
import com.baboolian.demo.ui.adapter.AlbumAdapter;
import com.baboolian.demo.ui.adapter.AlbumItemTouchHelper;
import com.baboolian.demo.ui.adapter.ThumbnailAdapter;
import com.baboolian.demo.db.IAlbumsDAO;
import com.baboolian.demo.ui.fragment.NewAlbumDialogFragment;
import com.baboolian.demo.ui.fragment.ThumbnailFragment;
import com.baboolian.demo.ui.fragment.MediaFragment;
import com.baboolian.demo.model.Album;
import com.baboolian.demo.model.MediaStoreFile;
import com.baboolian.demo.ui.receiver.DBUpdatedReceiver;
import com.baboolian.demo.util.MediaStoreUtil;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ThumbnailAdapter.MediaFileClickedListener,
        AlbumAdapter.AlbumListener, DBUpdatedReceiver.DBUpdatedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String EXTRA_ALBUM_ADAPTER_STATE = "EXTRA_ALBUM_ADAPTER_STATE";
    private static final String EXTRA_SELECTED_ALBUM_POSITION = "EXTRA_SELECTED_ALBUM_POSITION";

    private static final int[] heartColors = { 0xff0097e1, 0xff39c1f8, 0xfffc9402, 0xffffc126, 0xffa9d73f, 0xffe33271, 0xffe93aa8 };

    private FrameLayout rootView;
    private DrawerLayout drawerLayout;
    private RecyclerView albumRecyclerView;
    private FloatingActionButton fab;
    private Snackbar snackbar;
    private ActionBarDrawerToggle drawerToggle;
    private AlbumAdapter albumAdapter;

    private MediaFragment mediaFragment;
    private ThumbnailFragment thumbnailFragment;

    private DBUpdatedReceiver dbUpdatedReceiver;
    private IAlbumsDAO albumsDAO;

    private boolean hasTwoPanes;

    private int heartCount = 0;

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hasTwoPanes = getResources().getBoolean(R.bool.has_two_panes);
        albumsDAO = ((DemoApplication) getApplication()).getAlbumsDAO();
        dbUpdatedReceiver = new DBUpdatedReceiver(this);

        Toolbar toolbar = setupToolbar();
        setupDrawer(toolbar, savedInstanceState);
        setupFragments();
        setupViews();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() called");
        super.onResume();
        dbUpdatedReceiver.registerReceiver(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState() called");
        outState.putParcelable(EXTRA_ALBUM_ADAPTER_STATE, albumRecyclerView.getLayoutManager().onSaveInstanceState());
        outState.putInt(EXTRA_SELECTED_ALBUM_POSITION, albumAdapter.getSelectedAlbumPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause() called");
        super.onPause();
        dbUpdatedReceiver.unregisterReceiver(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //Restore new album icon state
        updateNewAlbumIcon(menu.findItem(R.id.action_create_album));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_create_album:
                switch (thumbnailFragment.getMode()) {
                    case VIEW:
                        thumbnailFragment.setMode(ThumbnailAdapter.Mode.BUILD_ALBUM);
                        snackbar.show();
                        break;

                    case BUILD_ALBUM:
                        item.getIcon().clearColorFilter();
                        snackbar.dismiss();
                        createAlbum();
                        break;

                    default:
                        throw new IllegalArgumentException("Invalid mode \"" + thumbnailFragment.getMode().toString() + "\" in MainActivity!");
                }

                updateNewAlbumIcon(item);
                return true;

            case R.id.action_show_resume:
                ResumeActivity.start(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAlbumClicked(Album album) {
        Log.i(TAG, "Album '" + album.getName() + "' clicked");
        //If we were in album building mode, revert back to view mode
        if (thumbnailFragment.getMode() == ThumbnailAdapter.Mode.BUILD_ALBUM) {
            thumbnailFragment.setMode(ThumbnailAdapter.Mode.VIEW);
            if (hasTwoPanes) {
                mediaFragment.displayMedia(null);
            }
            invalidateOptionsMenu();
            snackbar.dismiss();
        }

        displayAlbum(album);

        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        }
    }

    @Override
    public void onAlbumCreated(Album album) {
        Log.i(TAG, "Album '" + album.getName() + "' created");
        displayAlbum(album);
        albumAdapter.addAlbum(album);
    }

    @Override
    public void onAlbumDeleted(Album album) {
        Log.i(TAG, "Album '" + album.getName() + "' deleted");
        albumAdapter.removeAlbum(album);
        if (album.equals(thumbnailFragment.getAlbum())) {
            displayAlbum(albumsDAO.getCameraRollAlbum());
        }
    }

    @Override
    public void onMediaFileClicked(MediaStoreFile file) {
        Log.i(TAG, "File '" + file.getDataUri() + "' clicked");

        if (thumbnailFragment.getMode() == ThumbnailAdapter.Mode.VIEW) {
            if (hasTwoPanes) {
                mediaFragment.displayMedia(file);
            } else if (file != null) {
                MediaActivity.start(this, file);
            }
        }
    }

    private Toolbar setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        return toolbar;
    }

    private void setupDrawer(Toolbar toolbar, Bundle savedInstanceState) {
        drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);

        //This hooks up the home button to open/close the drawer
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerToggle.setHomeAsUpIndicator(R.drawable.ic_menu);
        drawerLayout.addDrawerListener(drawerToggle);

        ArrayList<Album> albums = albumsDAO.getAllAlbums();
        albumAdapter = new AlbumAdapter(this, albums, this);
        albumRecyclerView = (RecyclerView) findViewById(R.id.main_drawer_recyclerview);
        albumRecyclerView.setAdapter(albumAdapter);
        albumRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //TODO: Make the dividers draw under views being dragged
//        drawerRecyclerView.addItemDecoration(new DividerItemDecoration(this));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new AlbumItemTouchHelper(this, albumAdapter));
        itemTouchHelper.attachToRecyclerView(albumRecyclerView);
        if (savedInstanceState != null) {
            albumAdapter.setSelectedAlbum(savedInstanceState.getInt(EXTRA_SELECTED_ALBUM_POSITION));
            albumRecyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable(EXTRA_ALBUM_ADAPTER_STATE));
        } else {
            //If no saved instance state, we're starting on the camera roll which is always at position 0
            albumAdapter.setSelectedAlbum(0);
        }
    }

    private void setupFragments() {
        thumbnailFragment = (ThumbnailFragment) getSupportFragmentManager().findFragmentById(R.id.main_thumbnail_fragment);
        setTitle(thumbnailFragment.getAlbum().getName());

        if (hasTwoPanes) {
            mediaFragment = (MediaFragment) getSupportFragmentManager().findFragmentById(R.id.main_media_fragment);
            mediaFragment.displayMedia(thumbnailFragment.getSelectedFile());
        }
    }

    private void setupViews() {
        //Used as the backdrop for heart animations
        rootView = (FrameLayout) findViewById(R.id.main_root);

        fab = (FloatingActionButton) findViewById(R.id.main_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spawnHeart();
            }
        });

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinator_layout);
        snackbar = Snackbar.make(coordinatorLayout, R.string.main_new_album_snackbar_tip, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.main_new_album_snackbar_done, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        invalidateOptionsMenu(); //Sets the album icon back to white
                        createAlbum();
                    }
                });

        if (thumbnailFragment.getMode() == ThumbnailAdapter.Mode.BUILD_ALBUM) {
            snackbar.show();
        }
    }

    private void displayAlbum(Album album) {
        setTitle(album.getName());
        if (album.equals(thumbnailFragment.getAlbum())) {
            Log.i(TAG, "Tried to display '" + album.getName() + "' which was already being displayed.");
            return;
        } else if (hasTwoPanes) {
            //If switching albums, clear out the current media file on display
            mediaFragment.displayMedia(null);
        }
        thumbnailFragment.showAlbum(album, thumbnailFragment.getMode());
        invalidateOptionsMenu();
    }

    private void updateNewAlbumIcon(MenuItem item) {
        if (thumbnailFragment.getMode() == ThumbnailAdapter.Mode.BUILD_ALBUM) {
            item.getIcon().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
        } else {
            item.getIcon().clearColorFilter();
        }
    }

    private void createAlbum() {
        String fileIds = MediaStoreUtil.getFileIds(thumbnailFragment.getSelectedFiles());
        thumbnailFragment.setMode(ThumbnailAdapter.Mode.VIEW);
        if (fileIds.length() > 2) {//fileIds is a JSONArray string, so an empty array would be "[]"
            NewAlbumDialogFragment.show(this, fileIds);
        }
    }

    /**
     * HEART STUFF
     */

    private void spawnHeart() {
        final ImageView heart = new ImageView(this);
        heart.setImageResource(R.drawable.ic_heart);
        heart.setColorFilter(heartColors[heartCount++ % heartColors.length], PorterDuff.Mode.MULTIPLY);
        //Heart icon is 24x24dp, but isn't laid out yet so getWidth() and getHeight would return 0
        float heartDimensionPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());

        int[] fabGlobalCoord = new int[2];
        fab.getLocationOnScreen(fabGlobalCoord);
        int[] animationBackdropGlobalCoord = new int[2];
        rootView.getLocationOnScreen(animationBackdropGlobalCoord);

        heart.setX(fabGlobalCoord[0] - animationBackdropGlobalCoord[0] + ((fab.getWidth() - heartDimensionPx) / 2));
        heart.setY(fabGlobalCoord[1] - animationBackdropGlobalCoord[1] + ((fab.getHeight() - heartDimensionPx) / 2));
        rootView.addView(heart, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        //Post the animation setup so the heart view is layed out by the time the HeartAnimatorUpdateListener constructor is called
        heart.post(new Runnable() {
            @Override
            public void run() {
                ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
                animator.setDuration(6000);
                animator.setInterpolator(new LinearInterpolator());
                animator.addUpdateListener(new HeartAnimatorUpdateListener(heart));
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        rootView.removeView(heart);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });

                animator.start();
            }
        });

    }

    private class HeartAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        private static final int ANIMATION_HEIGHT_DP = 300;
        private ImageView heart;
        private float randomOffset;
        private float randomFrequency;
        private float amplitude;
        private float initialX, initialY;
        private float lastX, lastY;
        private float animationHeight;

        private HeartAnimatorUpdateListener(ImageView heart) {
            this.heart = heart;
            randomOffset = (float) Math.random(); // [0, 1)
            randomFrequency = (float) (4 + Math.random() * 2); //Use a random value between [4, 6) * Pi to get 2 to 3 complete sine waves
            //We calculate the amplitude of the sin wave by finding the distance between the spawn point
            //and the right most part of the screen without the heart going off screen
            //amplitude = (fab.rightMargin + (fab.width / 2) - (heart.width / 2)) / 2
            amplitude = (((CoordinatorLayout.LayoutParams) fab.getLayoutParams()).rightMargin + (fab.getWidth() / 2) - (heart.getWidth() / 2)) / 2;
            animationHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, ANIMATION_HEIGHT_DP, getResources().getDisplayMetrics());
            initialX = heart.getX() - calculateXTranslation(0); //Offset the starting X by the T_0 offset so hearts always spawn in the center of the fab
            initialY = heart.getY();
            lastX = initialX;
            lastY = initialY;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float value = (float) animation.getAnimatedValue();
            float xTranslation = calculateXTranslation(value); //Sine wave
            float yTranslation = value * animationHeight; //Linear
            heart.setX(initialX + xTranslation);
            heart.setY(initialY - yTranslation);
            heart.setRotation(calculateAngle() / 2); //Soften the angle by cutting it in half
            heart.setAlpha(1 - value);

            lastX = heart.getX();
            lastY = heart.getY();
        }

        private float calculateXTranslation(float value) {
            return amplitude * (float) Math.sin((randomOffset + value) * randomFrequency * Math.PI);
        }

        private float calculateAngle() {
            double slope = (heart.getY() - lastY) / (heart.getX() - lastX);
            double invertedSlope = Math.pow(slope, -1); //Invert the slope since the hearts travel along the Y axis
            return (float) Math.toDegrees(Math.atan(invertedSlope)); // arcTan(slope) = angle in radians, convert to degrees
        }
    }
}
