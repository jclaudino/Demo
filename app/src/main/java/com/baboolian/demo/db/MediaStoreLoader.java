package com.baboolian.demo.db;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.util.Log;

import com.baboolian.demo.model.Album;
import com.baboolian.demo.model.MediaStoreFile;

import org.json.JSONArray;

import java.util.ArrayList;

public class MediaStoreLoader {

    private static final String TAG = MediaStoreLoader.class.getSimpleName();

    private Context context;

    protected MediaStoreLoader(Context context) {
        this.context = context;
    }

    protected ArrayList<MediaStoreFile> getAlbumFilesFromExternalStorage(Album album) {
        String selection = "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO + ")"
                + " AND "
                + MediaStore.Files.FileColumns._ID
                + " IN "
                + album.getFileIds().replace("[", "(").replace("]",")");

        return getMediaFromExternalStorageWithSelection(context, selection);
    }

    protected String getAllMediaFileIds() {
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

        return getMediaFileIdsWithSelection(selection);
    }

    private ArrayList<MediaStoreFile> getMediaFromExternalStorageWithSelection(Context context, String selection) {
        ArrayList<MediaStoreFile> files = new ArrayList<>();

        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.WIDTH,
                MediaStore.Files.FileColumns.HEIGHT,
                MediaStore.Images.Media.ORIENTATION,
                MediaStore.Video.Media.RESOLUTION
        };

        Uri queryUri = MediaStore.Files.getContentUri("external");

        CursorLoader cursorLoader = new CursorLoader(
                context,
                queryUri,
                projection,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
        );

        Cursor cursor = cursorLoader.loadInBackground();

        int idColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
        int dataColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
        int mediaTypeColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
        int widthColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.WIDTH);
        int heightColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.HEIGHT);
        int imageOrientationColumn = cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);
        int videoResolutionColumn = cursor.getColumnIndex(MediaStore.Video.Media.RESOLUTION);

        try {
            long id;
            String dataUri;
            int mediaType;
            int width;
            int height;
            int imageOrientation;
            String videoResolution;

            while (cursor.moveToNext()) {
                id = cursor.getLong(idColumn);
                dataUri = cursor.getString(dataColumn);
                mediaType = cursor.getInt(mediaTypeColumn);
                width = cursor.getInt(widthColumn);
                height = cursor.getInt(heightColumn);
                imageOrientation = cursor.getInt(imageOrientationColumn);
                videoResolution = cursor.getString(videoResolutionColumn);

                if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                    //Swap height and width if orientation is 90 or 270 degrees
                    if (imageOrientation % 180 != 0) {
                        int temp = width;
                        width = height;
                        height = temp;
                    }
                } else if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                    //Video files store dimensions in the "resolution" field as a string in the format "1920x1080"
                    try {
                        String[] resolution = videoResolution.split("x");
                        width = Integer.valueOf(resolution[0]);
                        height = Integer.valueOf(resolution[1]);
                    } catch (Exception e) {
                        //Fail silently. The thumbnail's aspect ratio will look messed up but it's not fatal.
                        Log.e(TAG, "Failed to get video dimensions", e);
                    }
                }

                MediaStoreFile file = new MediaStoreFile(id, dataUri, mediaType, width, height);
                files.add(file);
            }
        } finally {
            cursor.close();
        }

        return files;
    }

    private String getMediaFileIdsWithSelection(String selection) {
        JSONArray fileIds = new JSONArray();

        String[] projection = {
                MediaStore.Files.FileColumns._ID
        };

        Uri queryUri = MediaStore.Files.getContentUri("external");

        CursorLoader cursorLoader = new CursorLoader(
                context,
                queryUri,
                projection,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
        );

        Cursor cursor = cursorLoader.loadInBackground();

        int idColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID);

        try {
            long id;

            while (cursor.moveToNext()) {
                id = cursor.getLong(idColumn);
                fileIds.put(id);
            }
        } finally {
            cursor.close();
        }

        return fileIds.toString();
    }
}
