package com.baboolian.demo;

import android.app.Application;

import com.baboolian.demo.db.AlbumsDAOFactory;
import com.baboolian.demo.db.IAlbumsDAO;

public class DemoApplication extends Application {

    private IAlbumsDAO albumsDAO;

    @Override
    public void onCreate() {
        super.onCreate();
        albumsDAO = AlbumsDAOFactory.createAlbumsDAO(this);
    }

    public IAlbumsDAO getAlbumsDAO() {
        return albumsDAO;
    }
}
