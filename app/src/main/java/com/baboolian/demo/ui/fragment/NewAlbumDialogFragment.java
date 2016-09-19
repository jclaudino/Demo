package com.baboolian.demo.ui.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baboolian.demo.DemoApplication;
import com.baboolian.demo.R;
import com.baboolian.demo.db.IAlbumsDAO;
import com.baboolian.demo.model.Album;
import com.baboolian.demo.ui.adapter.ThumbnailAdapter;
import com.baboolian.demo.ui.misc.RecyclerViewPreloader;

import java.util.ArrayList;

public class NewAlbumDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    private static final String TAG = NewAlbumDialogFragment.class.getSimpleName();

    private static final String EXTRA_FILE_IDS = "EXTRA_FILE_IDS";
    private static final String EXTRA_LAYOUT_MANAGER_STATE = "EXTRA_LAYOUT_MANAGER_STATE";

    private TextInputLayout albumNameTextInputLayout;
    private TextInputEditText albumNameEditText;
    private RecyclerView recyclerView;

    private String fileIds;

    private IAlbumsDAO albumsDAO;

    public static void show(AppCompatActivity activity, String fileIds) {
        NewAlbumDialogFragment dialogFragment = new NewAlbumDialogFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_FILE_IDS, fileIds);
        dialogFragment.setArguments(args);
        dialogFragment.show(activity.getSupportFragmentManager(), TAG);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() called");
        super.onCreate(savedInstanceState);

        albumsDAO = ((DemoApplication) getActivity().getApplication()).getAlbumsDAO();

        if (savedInstanceState != null) {
            fileIds = savedInstanceState.getString(EXTRA_FILE_IDS);
        } else if (getArguments() != null) {
            fileIds = getArguments().getString(EXTRA_FILE_IDS);
        } else {
            //fail silently
            Log.e(TAG, "Started dialog without any files!", new IllegalArgumentException());
            dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called");
        View view = inflater.inflate(R.layout.fragment_new_album, container, false);

        albumNameTextInputLayout = (TextInputLayout) view.findViewById(R.id.new_album_name_wrapper);
        albumNameEditText = (TextInputEditText) view.findViewById(R.id.new_album_name);
        albumNameEditText.addTextChangedListener(new AlbumNameTextWatcher());

        //Note: We could do a regular dialogfragment and get the buttons for free,
        //but bottomsheets are more fun and they don't support the dialogfragment buttons
        view.findViewById(R.id.new_album_cancel_button).setOnClickListener(this);
        view.findViewById(R.id.new_album_save_button).setOnClickListener(this);

        Album album = new Album(getString(R.string.new_album_name), fileIds);

        int columns = getResources().getInteger(R.integer.previewColumns);
        columns = Math.min(columns, album.getFileCount());
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), columns);
        ThumbnailAdapter thumbnailAdapter = new ThumbnailAdapter(this, null, layoutManager);

        recyclerView = (RecyclerView) view.findViewById(R.id.new_album_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(thumbnailAdapter);
        recyclerView.addOnScrollListener(new RecyclerViewPreloader<>(this, thumbnailAdapter, thumbnailAdapter, 20));

        thumbnailAdapter.showAlbum(albumsDAO.getFilesInAlbum(album), ThumbnailAdapter.Mode.PREVIEW);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated() called");
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            recyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable(EXTRA_LAYOUT_MANAGER_STATE));
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog() called");
        final BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState() called");
        outState.putString(EXTRA_FILE_IDS, fileIds);
        outState.putParcelable(EXTRA_LAYOUT_MANAGER_STATE, recyclerView.getLayoutManager().onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView() called");
        Dialog dialog = getDialog();
        //fixes this known bug: https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.new_album_cancel_button:
                //no-op
                break;
            case R.id.new_album_save_button:
                String albumName = this.albumNameEditText.getText().toString();
                if (albumName.trim().length() == 0) {
                    albumNameTextInputLayout.setError(getString(R.string.new_album_name_too_short));
                    return;
                }

                ArrayList<Album> existingAlbums = albumsDAO.getAllAlbums();
                for (Album existingAlbum : existingAlbums) {
                    if (albumName.equals(existingAlbum.getName())) {
                        albumNameTextInputLayout.setError(getString(R.string.new_album_duplicate_name, albumName));
                        return;
                    }
                }

                albumsDAO.createAlbum(albumName, fileIds);
                break;
        }
        dismiss();
    }

    private class AlbumNameTextWatcher implements TextWatcher {

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        public void afterTextChanged(Editable editable) {
            albumNameTextInputLayout.setErrorEnabled(false);
        }
    }
}
