package com.baboolian.demo.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baboolian.demo.DemoApplication;
import com.baboolian.demo.R;
import com.baboolian.demo.db.IAlbumsDAO;
import com.baboolian.demo.ui.adapter.ThumbnailAdapter;
import com.baboolian.demo.model.Album;
import com.baboolian.demo.model.MediaStoreFile;
import com.baboolian.demo.ui.adapter.ThumbnailAdapterState;
import com.baboolian.demo.ui.misc.RecyclerViewPreloader;

import java.util.ArrayList;

public class ThumbnailFragment extends Fragment implements ThumbnailAdapter.MediaFileClickedListener {

    public static final String TAG = ThumbnailFragment.class.getSimpleName();

    private static final String EXTRA_ALBUM = "EXTRA_ALBUM";
    private static final String EXTRA_MODE = "EXTRA_MODE";
    private static final String EXTRA_ADAPTER_STATE = "EXTRA_ADAPTER_STATE";
    private static final String EXTRA_LAYOUT_MANAGER_STATE = "EXTRA_LAYOUT_MANAGER_STATE";

    private ThumbnailAdapter.MediaFileClickedListener listener;

    private RecyclerView recyclerView;

    private ThumbnailAdapter thumbnailAdapter;

    private Album album;

    private IAlbumsDAO albumsDAO;

    private boolean hasTwoPanes;

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach() called");
        super.onAttach(context);
        listener = (ThumbnailAdapter.MediaFileClickedListener) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() called");
        super.onCreate(savedInstanceState);

        albumsDAO = ((DemoApplication) getActivity().getApplication()).getAlbumsDAO();
        hasTwoPanes = getResources().getBoolean(R.bool.has_two_panes);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called");
        View view = inflater.inflate(R.layout.fragment_thumbnails, container, false);

        int columns = getResources().getInteger(R.integer.columns);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        thumbnailAdapter = new ThumbnailAdapter(this, this, layoutManager);

        recyclerView = (RecyclerView) view.findViewById(R.id.thumbnail_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(thumbnailAdapter);
        recyclerView.addOnScrollListener(new RecyclerViewPreloader<>(this, thumbnailAdapter, thumbnailAdapter, 50));

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated() called");
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            album = savedInstanceState.getParcelable(EXTRA_ALBUM);
            ThumbnailAdapter.Mode mode = (ThumbnailAdapter.Mode) savedInstanceState.getSerializable(EXTRA_MODE);
            recyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable(EXTRA_LAYOUT_MANAGER_STATE));
            showAlbum(album, mode);
            thumbnailAdapter.restoreSavedInstanceState((ThumbnailAdapterState) savedInstanceState.getParcelable(EXTRA_ADAPTER_STATE));
            //We don't want to preserve selection when in VIEW mode on single pane layouts
            if (!hasTwoPanes && mode == ThumbnailAdapter.Mode.VIEW) {
                thumbnailAdapter.clearSelections();
            }
        } else {
            //First time launching, show the camera roll
            Log.i(TAG, "ThumbnailFragment launching for the first time, showing camera roll");
            Album album = albumsDAO.getCameraRollAlbum();
            showAlbum(album, ThumbnailAdapter.Mode.VIEW);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState() called");
        outState.putParcelable(EXTRA_ALBUM, album);
        outState.putSerializable(EXTRA_MODE, thumbnailAdapter.getMode());
        outState.putParcelable(EXTRA_LAYOUT_MANAGER_STATE, recyclerView.getLayoutManager().onSaveInstanceState());
        outState.putParcelable(EXTRA_ADAPTER_STATE, thumbnailAdapter.getSavedInstanceState());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach() called");
        super.onDetach();
        listener = null;
    }

    @Override
    public void onMediaFileClicked(MediaStoreFile file) {
        listener.onMediaFileClicked(file);
        if (!hasTwoPanes && getMode() == ThumbnailAdapter.Mode.VIEW) {
            thumbnailAdapter.clearSelections();
        }
    }

    public void showAlbum(Album album, ThumbnailAdapter.Mode mode) {
        Log.i(TAG, "Showing album '" + album.getName() + "'");
        this.album = album;
        thumbnailAdapter.showAlbum(albumsDAO.getFilesInAlbum(album), mode);
    }

    public Album getAlbum() {
        return album;
    }

    public void setMode(ThumbnailAdapter.Mode mode) {
        Log.i(TAG, "Setting mode to '" + mode.toString() + "'");
        thumbnailAdapter.setMode(mode);
    }

    public ThumbnailAdapter.Mode getMode() {
        return thumbnailAdapter.getMode();
    }

    //This is used for View mode
    public MediaStoreFile getSelectedFile() {
        return thumbnailAdapter.getSelectedFile();
    }

    //This is used for BUILD_ALBUM mode
    public ArrayList<MediaStoreFile> getSelectedFiles() {
        return thumbnailAdapter.getSelectedFiles();
    }
}
