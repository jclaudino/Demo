package com.baboolian.demo.ui.misc;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 *
 * Original Source: https://github.com/bumptech/glide/blob/master/integration/recyclerview/src/main/java/com/bumptech/glide/integration/recyclerview/RecyclerToListViewScrollListener.java
 *
 * This class is part of the unreleased and undocumented Glide master branch
 *
 * The original only supported subclasses of LinearLayoutManager, I've modified it to support
 * both LinearLayoutManager and StaggeredGridLayoutManager
 *
 * Converts {@link android.support.v7.widget.RecyclerView.OnScrollListener} events to
 * {@link AbsListView} scroll events.
 *
 */
public final class RecyclerToListViewScrollListener extends RecyclerView.OnScrollListener {
    public static final int UNKNOWN_SCROLL_STATE = Integer.MIN_VALUE;
    private final AbsListView.OnScrollListener scrollListener;
    private int lastFirstVisible = -1;
    private int lastVisibleCount = -1;
    private int lastItemCount = -1;

    public RecyclerToListViewScrollListener(AbsListView.OnScrollListener scrollListener) {
        this.scrollListener = scrollListener;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        int listViewState;
        switch (newState) {
            case RecyclerView.SCROLL_STATE_DRAGGING:
                listViewState = ListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;
                break;
            case RecyclerView.SCROLL_STATE_IDLE:
                listViewState = ListView.OnScrollListener.SCROLL_STATE_IDLE;
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:
                listViewState = ListView.OnScrollListener.SCROLL_STATE_FLING;
                break;
            default:
                listViewState = UNKNOWN_SCROLL_STATE;
        }

        scrollListener.onScrollStateChanged(null /*view*/, listViewState);
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

        int firstVisible;
        int lastVisible;

        if (layoutManager instanceof StaggeredGridLayoutManager) {
            int[] firstVisibleItemPositions = ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(null);
            int[] lastVisibleItemPositions = ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(null);
            firstVisible = min(firstVisibleItemPositions);
            lastVisible = max(lastVisibleItemPositions);
        } else if (layoutManager instanceof LinearLayoutManager) {
            firstVisible = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
            lastVisible = ((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
        } else {
            throw new IllegalArgumentException("Only descendants of LinearLayoutManager and StaggeredGridLayoutManager are supported!");
        }

        int visibleCount = Math.abs(firstVisible - lastVisible);
        int itemCount = recyclerView.getAdapter().getItemCount();

        if (firstVisible != lastFirstVisible || visibleCount != lastVisibleCount
                || itemCount != lastItemCount) {
            scrollListener.onScroll(null, firstVisible, visibleCount, itemCount);
            lastFirstVisible = firstVisible;
            lastVisibleCount = visibleCount;
            lastItemCount = itemCount;
        }
    }

    private int min(int[] values) {
        int min = Integer.MAX_VALUE;
        for (int value : values) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    private int max(int[] values) {
        int max = Integer.MIN_VALUE;
        for (int value : values) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }
}