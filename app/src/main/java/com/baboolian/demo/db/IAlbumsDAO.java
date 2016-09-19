package com.baboolian.demo.db;

import com.baboolian.demo.model.Album;
import com.baboolian.demo.model.MediaStoreFile;

import java.util.ArrayList;

public interface IAlbumsDAO {

    ArrayList<Album> getAllAlbums();

    Album getCameraRollAlbum();

    void createAlbum(String albumName, String fileIds);

    void deleteAlbum(Album album);

    void moveAlbum(int fromPosition, int toPosition);

    ArrayList<MediaStoreFile> getFilesInAlbum(Album album);

    void close();

}
