package com.baboolian.demo.db;

import android.content.Context;
import android.util.Log;

import com.baboolian.demo.R;
import com.baboolian.demo.model.Album;
import com.baboolian.demo.model.MediaStoreFile;
import com.baboolian.demo.ui.receiver.DBUpdatedReceiver;

import java.util.ArrayList;

/**
 * Albums Data Access Object
 *
 * This class acts as the gatekeeper to persistent storage, through a SQLite DB and
 * the OS's Media Provider
 *
 * This class should only have one instance to avoid multiple simultaneous connection to the db.
 */
public class AlbumsDAO implements IAlbumsDAO {

    private static final String TAG = AlbumsDAO.class.getSimpleName();

    private Context context;
    private DataBaseWrapper dataBaseWrapper;
    private MediaStoreLoader mediaStoreLoader;

    public AlbumsDAO(Context context) {
        this.context = context;
        dataBaseWrapper = new DataBaseWrapper(context);
        mediaStoreLoader = new MediaStoreLoader(context);
    }

    @Override
    public ArrayList<Album> getAllAlbums() {
        return dataBaseWrapper.getAllAlbums();
    }

    @Override
    public Album getCameraRollAlbum() {
        Log.i(TAG, "Retrieving the camera roll album");
        String fileIds = mediaStoreLoader.getAllMediaFileIds();
        return new Album(context.getString(R.string.main_cameraroll_album_name), fileIds);
    }

    @Override
    public void createAlbum(String name, String fileIds) {
        Log.i(TAG, "Creating album '" + name + "' in the db");
        Album album = dataBaseWrapper.createAlbum(name, fileIds);
        DBUpdatedReceiver.sendAlbumCreatedBroadcast(context, album);
    }

    @Override
    public void deleteAlbum(Album album) {
        Log.i(TAG, "Deleting album '" + album + "' in the db");
        dataBaseWrapper.deleteAlbum(album);
        DBUpdatedReceiver.sendAlbumDeletedBroadcast(context, album);
    }

    @Override
    public void moveAlbum(int fromPosition, int toPosition) {
        Log.i(TAG, "Moving album from position " + fromPosition + " to " + toPosition + " in the db");
        dataBaseWrapper.moveAlbum(fromPosition, toPosition);
    }

    @Override
    public ArrayList<MediaStoreFile> getFilesInAlbum(Album album) {
        Log.i(TAG, "Retrieving files from album '" + album + "'");
        return mediaStoreLoader.getAlbumFilesFromExternalStorage(album);
    }

    //TODO: Optimize opening/closing the DB connection in concert with the app/UI's lifecycle
    @Override
    public void close() {
        dataBaseWrapper.close();
    }
}
