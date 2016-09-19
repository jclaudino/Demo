package com.baboolian.demo.db;

import android.content.Context;

public class AlbumsDAOFactory {

    //TODO: Create a MockAlbumsDAO that implements IAlbumsDAO for testing
    public static IAlbumsDAO createAlbumsDAO(Context context) {
        return new AlbumsDAO(context);
    }
}
