package com.baboolian.demo.ui.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.baboolian.demo.model.Album;

/**
 * We don't really need to fire a broadcast for DB updates because reading/writing to the local
 * SQLiteDataBase is both fast and synchronous, but I wanted to throw a broadcast receiver in somewhere :-)
 *
 * This would however be useful if we were storing things asynchronously, like to a remote
 * db or REST service, or even reading/writing large binary data to disk.
 */
public class DBUpdatedReceiver extends BroadcastReceiver {

    private static final String TAG = DBUpdatedReceiver.class.getSimpleName();

    private static final String ALBUM_CREATED_ACTION = "com.baboolian.ALBUM_CREATED)";
    private static final String ALBUM_DELETED_ACTION = "com.baboolian.ALBUM_DELETED)";

    private static final String EXTRA_ALBUM = "EXTRA_ALBUM";

    public interface DBUpdatedListener {
        void onAlbumCreated(Album album);
        void onAlbumDeleted(Album album);
    }

    private DBUpdatedListener listener;

    public static void sendAlbumCreatedBroadcast(Context context, Album album) {
        Log.i(TAG, "Sending '" + ALBUM_CREATED_ACTION + "' broadcast");
        Intent intent = new Intent(ALBUM_CREATED_ACTION);
        intent.putExtra(EXTRA_ALBUM, album);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void sendAlbumDeletedBroadcast(Context context, Album album) {
        Log.i(TAG, "Sending '" + ALBUM_DELETED_ACTION + "' broadcast");
        Intent intent = new Intent(ALBUM_DELETED_ACTION);
        intent.putExtra(EXTRA_ALBUM, album);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public DBUpdatedReceiver(DBUpdatedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received '" + intent.getAction() + "' broadcast");
        Album album = intent.getParcelableExtra(EXTRA_ALBUM);
        switch (intent.getAction()) {
            case ALBUM_CREATED_ACTION:
                listener.onAlbumCreated(album);
                break;
            case ALBUM_DELETED_ACTION:
                listener.onAlbumDeleted(album);
                break;
        }
    }

    public void registerReceiver(Context context) {
        LocalBroadcastManager.getInstance(context).registerReceiver(this, buildIntentFilter());
    }

    public void unregisterReceiver(Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }

    private IntentFilter buildIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ALBUM_CREATED_ACTION);
        intentFilter.addAction(ALBUM_DELETED_ACTION);
        return intentFilter;
    }
}