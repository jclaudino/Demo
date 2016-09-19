package com.baboolian.demo.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.baboolian.demo.R;
import com.baboolian.demo.model.MediaStoreFile;

public class MediaFragment extends Fragment {

    private static final String TAG = MediaFragment.class.getSimpleName();

    private ImageView mediaImage;
    private VideoView mediaVideo;
    private View dimView;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called");
        View view = inflater.inflate(R.layout.fragment_media, container);

        mediaImage = (ImageView) view.findViewById(R.id.media_image);
        mediaVideo = (VideoView) view.findViewById(R.id.media_video);
        dimView = view.findViewById(R.id.loading_dim_view);
        progressBar = (ProgressBar) view.findViewById(R.id.loading_progress_bar);

        return view;
    }

    public void displayMedia(MediaStoreFile file) {
        Log.i(TAG, "Displaying media " + (file == null ? "<null>" : file.toString()));
        //If file is null, fail silently, no need to take down the entire app
        if (file == null) {
            mediaVideo.setVisibility(View.GONE);
            mediaImage.setImageBitmap(null);
            mediaImage.setImageResource(R.drawable.ic_photo_placeholder);
            mediaImage.setVisibility(View.VISIBLE);
            return;
        }

        Uri uri = Uri.parse(file.getDataUri());
        switch (file.getMediaType()) {
            case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
                showImage(file);
                break;
            case MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO:
                showVideo(uri, file.getWidth(), file.getHeight());
                break;
        }
    }

    private void showImage(MediaStoreFile file) {
        new LoadImageAsyncTask().execute(file);
    }

    private void showVideo(Uri uri, int width, int height) {
        MediaController mediaController = new MediaController(getContext());
        mediaController.setAnchorView(mediaVideo);
        mediaController.setMediaPlayer(mediaVideo);

        //Adjust the video height to account for aspect ratio
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mediaVideo.getLayoutParams();
        int adjustedHeight = (int) (layoutParams.width * (((double) width) / height));
        layoutParams.height = adjustedHeight;
        mediaVideo.setLayoutParams(layoutParams);

        mediaVideo.setMediaController(mediaController);
        mediaVideo.setVideoURI(uri);
        mediaImage.setVisibility(View.GONE);
        mediaVideo.setVisibility(View.VISIBLE);
        mediaVideo.start();
        hideProgress();
    }

    private void showProgress() {
        dimView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        dimView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private class LoadImageAsyncTask extends AsyncTask<MediaStoreFile, Void, Bitmap> {

        private MediaStoreFile file;

        @Override
        protected void onPreExecute() {
            showProgress();
        }

        @Override
        protected Bitmap doInBackground(MediaStoreFile... params) {
            file = params[0];
            return decodeSampledBitmapFromUri(file.getDataUri());
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (file.isSelected()) {
                hideProgress();
                mediaImage.setImageBitmap(bitmap);
                mediaVideo.setVisibility(View.GONE);
                mediaImage.setVisibility(View.VISIBLE);
            }
        }
    }

    //ImageView's setImageURI(Uri) method clogs up the UI thread, this is more optimal
    //TODO: Try out Glide and compare performance
    private Bitmap decodeSampledBitmapFromUri(String uri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //Cut the quality in half for faster loading
        options.inSampleSize = 2;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(uri);
    }
}
