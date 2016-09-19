package com.baboolian.demo.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.baboolian.demo.DemoApplication;
import com.baboolian.demo.R;
import com.baboolian.demo.db.IAlbumsDAO;
import com.baboolian.demo.model.Album;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class AlbumAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements AlbumItemTouchHelper.ItemTouchListener {

    private static final String TAG = AlbumAdapter.class.getSimpleName();

    private static final int ITEM_TYPE_CAMERA_ROLL = 0;
    private static final int ITEM_TYPE_ALBUM = 1;
    private static final int UNDO_TIMEOUT = 3000;

    private Context context;
    private ArrayList<Album> albums;
    private AlbumListener listener;

    private ArrayList<Album> albumsPendingRemoval;
    private HashMap<Album, Runnable> pendingRunnables;

    private Handler handler;
    private IAlbumsDAO albumsDAO;

    public interface AlbumListener {
        void onAlbumClicked(Album album);
    }

    public AlbumAdapter(Activity activity, ArrayList<Album> albums, AlbumListener listener) {
        this.context = activity;
        this.albums = albums;
        this.listener = listener;
        albumsPendingRemoval = new ArrayList<>();
        pendingRunnables = new HashMap<>();
        handler = new Handler();
        albumsDAO = ((DemoApplication) activity.getApplication()).getAlbumsDAO();

        albums.add(0, albumsDAO.getCameraRollAlbum());
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ITEM_TYPE_CAMERA_ROLL;
        }
        return ITEM_TYPE_ALBUM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case ITEM_TYPE_CAMERA_ROLL:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album_camera_roll, parent, false);
                return new BaseAlbumViewHolder(view);
            case ITEM_TYPE_ALBUM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false);
                return new AlbumViewHolder(view);
            default:
                throw new IllegalArgumentException("No such view type '" + viewType + "'");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Album album = albums.get(position);
        BaseAlbumViewHolder baseAlbumViewHolder = (BaseAlbumViewHolder) holder;
        boolean pendingRemoval = albumsPendingRemoval.contains(album);

        baseAlbumViewHolder.itemView.setSelected(album.isSelected());
        if (holder instanceof AlbumViewHolder) {
            AlbumViewHolder albumViewHolder = (AlbumViewHolder) holder;
            albumViewHolder.itemView.setActivated(pendingRemoval); //Activated means pending removal, so red background
            albumViewHolder.undoButton.setVisibility(pendingRemoval ? View.VISIBLE : View.INVISIBLE);

        }
        baseAlbumViewHolder.albumName.setVisibility(pendingRemoval ? View.INVISIBLE : View.VISIBLE);
        baseAlbumViewHolder.fileCount.setVisibility(pendingRemoval ? View.INVISIBLE : View.VISIBLE);
        baseAlbumViewHolder.albumName.setText(album.getName());
        baseAlbumViewHolder.fileCount.setText(context.getString(R.string.item_album_file_count, album.getFileCount()));
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    @Override
    public void onItemSwipe(int position) {
        final Album album = albums.get(position);

        if (!albumsPendingRemoval.contains(album)) {
            Log.i(TAG, "Adding pending removal of album '" + album + "'");
            albumsPendingRemoval.add(album);
            notifyItemChanged(position);

            Runnable pendingRemovalRunnable = new Runnable() {
                @Override
                public void run() {
                    removeAlbum(album);
                }
            };

            pendingRunnables.put(album, pendingRemovalRunnable);
            handler.postDelayed(pendingRemovalRunnable, UNDO_TIMEOUT);

        } else {
            Log.i(TAG, "pendingalbums already contains album!");
        }
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Log.i(TAG, "onItemMove from " + fromPosition + " to " + toPosition);
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(albums, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(albums, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        //Persist the move to the DB
        albumsDAO.moveAlbum(fromPosition - 1, toPosition - 1); //Subtract one to account for the Camera Roll album
    }

    public void addAlbum(Album album) {
        albums.add(album);
        notifyItemInserted(albums.size() - 1);
        selectAlbum(albums.size() - 1);
    }

    public void removeAlbum(Album album) {
        int position = albums.indexOf(album);
        if (position > -1) {
            Album removedAlbum = albums.remove(position);
            albumsPendingRemoval.remove(removedAlbum);
            notifyItemRemoved(position);
            if (removedAlbum.isSelected()) {
                //Camera roll is always at position 0, select it
                selectAlbum(0);
            }
            albumsDAO.deleteAlbum(removedAlbum);
        }
    }

    public int getSelectedAlbumPosition() {
        Album album;
        for (int i = 0; i < albums.size(); i++) {
            album = albums.get(i);
            if (album.isSelected()) {
                return i;
            }
        }
        return -1;
    }

    public void setSelectedAlbum(int position) {
        if (position > -1) {
            Album album = albums.get(position);
            if (album != null) {
                album.setSelected(true);
                notifyItemChanged(position);
            }
        }
    }

    private void selectAlbum(int position) {
        //Can't store selected index because it may get moved
        Album album;
        for (int i = 0; i < albums.size(); i++) {
            album = albums.get(i);
            if (album.isSelected()) {
                album.setSelected(false);
                notifyItemChanged(i);
            }
        }

        albums.get(position).setSelected(true);
        notifyItemChanged(position);
    }

    private class AlbumViewHolder extends BaseAlbumViewHolder {
        Button undoButton;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            undoButton = (Button) itemView.findViewById(R.id.item_album_undo_button);
            undoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) {
                        return;
                    }

                    Album album = albums.get(position);
                    Runnable pendingRemovalRunnable = pendingRunnables.remove(album);
                    if (pendingRemovalRunnable != null) {
                        Log.i(TAG, "Undoing removal of album '" + album + "'");
                        handler.removeCallbacks(pendingRemovalRunnable);
                        albumsPendingRemoval.remove(album);
                    }

                    notifyItemChanged(position);
                    notifyDataSetChanged();
                }
            });
        }
    }

    private class BaseAlbumViewHolder extends RecyclerView.ViewHolder {

        TextView albumName;
        TextView fileCount;

        public BaseAlbumViewHolder(View itemView) {
            super(itemView);
            albumName = (TextView) itemView.findViewById(R.id.item_album_name);
            fileCount = (TextView) itemView.findViewById(R.id.item_album_file_count);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) {
                        return;
                    }

                    selectAlbum(position);

                    Album album = albums.get(position);
                    listener.onAlbumClicked(album);
                }
            });
        }
    }
}
