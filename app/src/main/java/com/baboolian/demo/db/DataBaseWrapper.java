package com.baboolian.demo.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.baboolian.demo.model.Album;

import java.util.ArrayList;
import java.util.Collections;

public class DataBaseWrapper extends SQLiteOpenHelper {

    private static final String TAG = DataBaseWrapper.class.getSimpleName();

    private static final String DATABASE_NAME = "albums.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_ALBUMS = "albums";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_ALBUM_NAME = "_album_name";
    private static final String COLUMN_FILE_IDS = "_file_ids";
    private static final String COLUMN_ORDER = "_order";

    private static final String CREATE_TABLE_ALBUMS = "create table " + TABLE_ALBUMS
            + "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_ALBUM_NAME + " integer not null, "
            + COLUMN_FILE_IDS + " text not null, "
            + COLUMN_ORDER + " integer);";

    String DATABASE_ALTER_V1_TO_V2 = "ALTER TABLE " + TABLE_ALBUMS + " ADD COLUMN " + COLUMN_ORDER + " integer;"
            + "UPDATE " + TABLE_ALBUMS + " SET " + COLUMN_ORDER + " = " + COLUMN_ID;

    public DataBaseWrapper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_ALBUMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        //Don't add breaks here so each update happens one after the other
        switch (oldVersion) {
            case 1:
                db.execSQL(DATABASE_ALTER_V1_TO_V2);
            case 2:
                //update v2 to v3
            case 3:
                //update v3 to v4
                //etc...
        }
    }

    protected ArrayList<Album> getAllAlbums() {
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<Album> albums = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_ALBUMS + " ORDER BY " + COLUMN_ORDER + " ASC";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String albumName = cursor.getString(cursor.getColumnIndex(COLUMN_ALBUM_NAME));
                    String fileIds = cursor.getString(cursor.getColumnIndex(COLUMN_FILE_IDS));
                    Album album = new Album(albumName, fileIds);
                    albums.add(album);
                } while (cursor.moveToNext());
            }

            cursor.close();
            return albums;
        }

        return null;
    }

    protected Album createAlbum(String albumName, String fileIds) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ALBUM_NAME, albumName);
        values.put(COLUMN_FILE_IDS, fileIds);

        db.insert(TABLE_ALBUMS, null, values);

        return new Album(albumName, fileIds);
    }

    protected void deleteAlbum(Album album) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_ALBUMS, COLUMN_ALBUM_NAME + " = '" + album.getName() + "'", null);
    }

    protected void moveAlbum(int fromPosition, int toPosition) {
        //TODO: Do this more efficiently using the newly added COLUMN_ORDER
        //For now removing, reordering, and rewriting all albums is good enough
        //because the data set is tiny and the operations are quick
        if (fromPosition == toPosition) {
            return;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        //Get all albums in the current order
        ArrayList<Album> albums = getAllAlbums();
        //Swap albums
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(albums, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(albums, i, i - 1);
            }
        }
        //Remove all albums from the DB
        db.execSQL("DELETE FROM " + TABLE_ALBUMS);
        //Rewrite the newly ordered albums to the DB
        for (Album album : albums) {
            createAlbum(album.getName(), album.getFileIds());
        }
    }

    //Used for debugging
    private String getTableAsString(SQLiteDatabase db, String tableName) {
        Log.i(TAG, "getTableAsString called");
        String tableString = String.format("Table %s:\n", tableName);
        Cursor allRows  = db.rawQuery("SELECT * FROM " + tableName, null);
        if (allRows.moveToFirst() ){
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name: columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToNext());
            allRows.close();
        }

        return tableString;
    }
}
