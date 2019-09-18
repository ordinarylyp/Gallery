package com.sunmi.imagepicker.listener;

import android.graphics.Color;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.sunmi.imagepicker.adapter.ImagePickerAdapter;

import java.util.Collections;

/**
 * Description:
 *
 * @author linyuanpeng on 2019-09-11.
 */
public class RecyItemTouchHelperCallback extends ItemTouchHelper.Callback {

    RecyclerView.Adapter mAdapter;
    boolean isSwipeEnable;
    boolean isFirstDragUnable;

    public RecyItemTouchHelperCallback(RecyclerView.Adapter adapter) {
        mAdapter = adapter;
        isSwipeEnable = true;
        isFirstDragUnable = false;
    }

    public RecyItemTouchHelperCallback(RecyclerView.Adapter adapter, boolean isSwipeEnable, boolean isFirstDragUnable) {
        mAdapter = adapter;
        this.isSwipeEnable = isSwipeEnable;
        this.isFirstDragUnable = isFirstDragUnable;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            int swipeFlags = 0;
            return makeMovementFlags(dragFlags, swipeFlags);
        } else {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();
        if (isFirstDragUnable && toPosition == 0) {
            return false;
        }
        int start = Math.min(fromPosition, toPosition);
        int end = Math.max(fromPosition, toPosition);
        for (int i = start; i < end - 1; i++) {
            Collections.swap(((ImagePickerAdapter) mAdapter).getDataList(), i, i + 1);
        }

        mAdapter.notifyItemMoved(fromPosition, toPosition);
        //mAdapter.notifyDataSetChanged();
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int adapterPosition = viewHolder.getAdapterPosition();
        mAdapter.notifyItemRemoved(adapterPosition);
        ((ImagePickerAdapter) mAdapter).getDataList().remove(adapterPosition);
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        viewHolder.itemView.setBackgroundColor(Color.WHITE);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return !isFirstDragUnable;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return isSwipeEnable;
    }
}
