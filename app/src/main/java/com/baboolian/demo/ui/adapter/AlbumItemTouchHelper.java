package com.baboolian.demo.ui.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.baboolian.demo.R;


public class AlbumItemTouchHelper extends ItemTouchHelper.SimpleCallback {

    private Drawable swipeBackground;
    private Drawable trashCan;
    private int trashMargin;

    private ItemTouchListener listener;

    public interface ItemTouchListener {

        void onItemMove(int fromPosition, int toPosition);

        void onItemSwipe(int position);
    }

    public AlbumItemTouchHelper(Context context, ItemTouchListener listener) {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT);
        this.listener = listener;
        swipeBackground = new ColorDrawable(ContextCompat.getColor(context, R.color.item_album_bg_pending_removal));
        trashCan = ContextCompat.getDrawable(context, R.drawable.ic_trash_can);
        trashMargin = (int) context.getResources().getDimension(R.dimen.item_album_border);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getDragDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        //Don't allow movement of the Camera Roll album which is always at position 0
        //Also don't allow movement of albums that are pending removal (activated implies pending removal)
        if (viewHolder.getAdapterPosition() == 0 || viewHolder.itemView.isActivated()) {
            return 0;
        }
        return super.getDragDirs(recyclerView, viewHolder);
    }

    @Override
    public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder current, RecyclerView.ViewHolder target) {
        return target.getAdapterPosition() != 0;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        listener.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        //Don't allow deletion of the Camera Roll album which is always at position 0
        if (viewHolder.getAdapterPosition() == 0) {
            return 0;
        }
        return super.getSwipeDirs(recyclerView, viewHolder);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onItemSwipe(viewHolder.getAdapterPosition());
    }

    @Override
    public void onChildDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (viewHolder.getAdapterPosition() == RecyclerView.NO_POSITION) {
            return;
        }
        View itemView = viewHolder.itemView;

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            //Draw a red background
            swipeBackground.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + (int) dX, itemView.getBottom());
            swipeBackground.draw(canvas);

            //Draw a trash can to signify deletion
            int itemHeight = itemView.getBottom() - itemView.getTop();
            int trashWidth = trashCan.getIntrinsicWidth();
            int trashHeight = trashCan.getIntrinsicWidth();

            int trashLeftBound = itemView.getLeft() + trashMargin;
            int trashTop = itemView.getTop() + (itemHeight - trashHeight) / 2;
            int trashRightBound = itemView.getLeft() + trashMargin + trashWidth;
            int trashBottomBound = trashTop + trashHeight;
            trashCan.setBounds(trashLeftBound, trashTop, trashRightBound, trashBottomBound);
            trashCan.draw(canvas);
        } else if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            //TODO: Highlight the item being dragged
        }

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
