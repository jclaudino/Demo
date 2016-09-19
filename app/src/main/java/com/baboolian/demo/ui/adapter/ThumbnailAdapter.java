package com.baboolian.demo.ui.adapter;

import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.baboolian.demo.R;
import com.baboolian.demo.model.MediaStoreFile;
import com.baboolian.demo.ui.view.DynamicHeightImageView;
import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ThumbnailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ListPreloader.PreloadModelProvider<String>, ListPreloader.PreloadSizeProvider<String> {

    private static final String TAG = ThumbnailAdapter.class.getSimpleName();

    private List<MediaStoreFile> files;
    private MediaFileClickedListener listener;
    private int selectedFilePosition = -1; //Used for View mode
    private RecyclerView.LayoutManager layoutManager;
    private GenericRequestBuilder<String, ?, ?, ?> glide;

    private Mode mode;

    private int itemWidth;

    public enum Mode {
        VIEW,
        BUILD_ALBUM,
        PREVIEW
    }

    public interface MediaFileClickedListener {
        void onMediaFileClicked(MediaStoreFile file);
    }

    //We use a fragment instead of a context because Glide works with the fragment's lifecycle
    public ThumbnailAdapter(Fragment fragment, MediaFileClickedListener listener, RecyclerView.LayoutManager layoutManager) {
        this.files = new ArrayList<>();
        this.listener = listener;
        this.layoutManager = layoutManager;
        mode = Mode.VIEW;
        glide = Glide.with(fragment).fromString();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_thumbnail, parent, false);
        return new MediaFileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        MediaFileViewHolder mediaFileViewHolder = (MediaFileViewHolder) holder;
        MediaStoreFile file = files.get(position);

        //Set the height ratio to get an explicit height for the image, this allows Glide to recycle cached transformed images
        if (layoutManager instanceof StaggeredGridLayoutManager) {
            //Force the layout to remeasure so it's dimensions match the cached image (if there is one) before Glide does it's business
            mediaFileViewHolder.getThumbnail().layout(0, 0, 0, 0);
            mediaFileViewHolder.getThumbnail().setHeightRatio(((double) file.getHeight()) / file.getWidth());
        } else {
            //If the layoutmanager isn't staggered, use a square aspect ratio to keep all items the same size
            mediaFileViewHolder.getThumbnail().setHeightRatio(1);
        }

        Target<?> target = glide
                .load(file.getDataUri())
                .placeholder(R.color.item_thumbnail_bg_placeholder)
                .into(mediaFileViewHolder.getThumbnail());

        if (itemWidth == 0) {
            target.getSize(new SizeReadyCallback() {
                @Override
                public void onSizeReady(int width, int height) {
                    //The width of all items will be the same, so we store this to use later for calculating heights during preloading
                    itemWidth = width;
                }
            });
        }

        mediaFileViewHolder.getVideoIcon().setVisibility(file.getMediaType() == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO ? View.VISIBLE : View.GONE);
        mediaFileViewHolder.getSelectedLayout().setSelected(file.isSelected());
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    @Override
    public List<String> getPreloadItems(int position) {
        return Collections.singletonList(files.get(position).getDataUri());
    }

    @Override
    public GenericRequestBuilder getPreloadRequestBuilder(String url) {
        return glide.load(url);
    }

    @Override
    public int[] getPreloadSize(String url, int adapterPosition, int perItemPosition) {
        MediaStoreFile file = files.get(adapterPosition);
        double heightRatio = ((double) file.getHeight()) / file.getWidth();
        int height = (int) (itemWidth * heightRatio);

        return new int[] { itemWidth, height };
    }

    public ThumbnailAdapterState getSavedInstanceState() {
        ArrayList<Integer> selectedFilePositions = new ArrayList<>();

        MediaStoreFile file;
        for (int i = 0; i < files.size(); i++) {
            file = files.get(i);
            if (file.isSelected()) {
                selectedFilePositions.add(i);
            }
        }
        return new ThumbnailAdapterState(selectedFilePosition, selectedFilePositions);
    }

    public void restoreSavedInstanceState(ThumbnailAdapterState adapterState) {
        selectedFilePosition = adapterState.getSelectedFilePosition();
        for (int position : adapterState.getSelectedFilePositions()) {
            files.get(position).setSelected(true);
        }
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        if (mode == Mode.VIEW) {
            clearSelections();
        }
    }

    public void showAlbum(List<MediaStoreFile> files, Mode mode) {
        clearSelections();
        this.mode = mode;
        this.files = files;
    }

    public void clearSelections() {
        Log.i(TAG, "Clearing selections");
        selectedFilePosition = -1;
        for (MediaStoreFile file : files) {
            file.setSelected(false);
        }
        notifyDataSetChanged();
    }

    public Mode getMode() {
        return mode;
    }

    public MediaStoreFile getSelectedFile() {
        return selectedFilePosition > -1 ? files.get(selectedFilePosition) : null;
    }

    public ArrayList<MediaStoreFile> getSelectedFiles() {
        ArrayList<MediaStoreFile> selectedFiles = new ArrayList<>();
        for (MediaStoreFile file : files) {
            if (file.isSelected()) {
                selectedFiles.add(file);
            }
        }
        return selectedFiles;
    }

    private boolean isPositionVisible(int position) {
        int firstVisible = getFirstVisibleItem();
        int lastVisible = getLastVisibleItem() + 1;

        if (firstVisible <= position && position <= lastVisible) {
            return true;

        }
        Log.d("tindme", "skipping notify on item " + position);
        return false;
    }

    private int getFirstVisibleItem() {
        if (layoutManager instanceof StaggeredGridLayoutManager) {
            int[] firstVisibleItemPositions = ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(null);
            int min = Integer.MAX_VALUE;
            for (int itemPosition : firstVisibleItemPositions) {
                if (itemPosition < min) {
                    min = itemPosition;
                }
            }
            return min;
        } else if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        } else {
            throw new IllegalArgumentException("Only descendants of LinearLayoutManager and StaggeredGridLayoutManager allowed!");
        }
    }

    private int getLastVisibleItem() {
        if (layoutManager instanceof StaggeredGridLayoutManager) {
            int[] lasttVisibleItemPositions = ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(null);
            int max = Integer.MIN_VALUE;
            for (int itemPosition : lasttVisibleItemPositions) {
                if (itemPosition > max) {
                    max = itemPosition;
                }
            }
            return max;
        } else if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        } else {
            throw new IllegalArgumentException("Only descendants of LinearLayoutManager and StaggeredGridLayoutManager allowed!");
        }
    }

    private class MediaFileViewHolder extends RecyclerView.ViewHolder {

        private DynamicHeightImageView thumbnail;
        private ImageView videoIcon;
        private ImageView selectedLayout;

        public MediaFileViewHolder(View itemView) {
            super(itemView);
            thumbnail = (DynamicHeightImageView) itemView.findViewById(R.id.item_thumbnail_image);
            videoIcon = (ImageView) itemView.findViewById(R.id.item_thumbnail_video_icon);
            selectedLayout = (ImageView) itemView.findViewById(R.id.item_thumbnail_selected_layout);

            thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) {
                        return;
                    }

                    MediaStoreFile file = files.get(position);

                    switch (mode) {
                        case VIEW:
                            //User clicks the file that's already selected; animate the click but don't do anything
                            if (selectedFilePosition == position) {
                                notifyItemChanged(position);
                                return;
                            }
                            //Deselect previously selected file
                            else if (selectedFilePosition > -1) {
                                files.get(selectedFilePosition).setSelected(false);
                                //If we update an item off screen, it sometimes causes the items to shuffle (usually happens after scrolling up)
                                //Only update the old item if it's currently visible
                                if (isPositionVisible(selectedFilePosition)) {
                                    notifyItemChanged(selectedFilePosition);
                                }
                            }
                            selectedFilePosition = position;
                            file.setSelected(!file.isSelected());
                            notifyItemChanged(position);
                            listener.onMediaFileClicked(file);
                            break;
                        case BUILD_ALBUM:
                            file.setSelected(!file.isSelected());
                            notifyItemChanged(position);
                            break;
                        case PREVIEW:
                            //no-op
                            break;
                    }
                }
            });
        }

        private DynamicHeightImageView getThumbnail() {
            return thumbnail;
        }

        private ImageView getSelectedLayout() {
            return selectedLayout;
        }

        private ImageView getVideoIcon() {
            return videoIcon;
        }

    }
}
