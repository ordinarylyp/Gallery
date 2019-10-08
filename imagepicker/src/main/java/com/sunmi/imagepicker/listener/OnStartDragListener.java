package com.sunmi.imagepicker.listener;

import android.support.v7.widget.RecyclerView;

/**
 * Description: 监听RecyclerView Item的拖动
 *
 * @author linyuanpeng on 2019-09-20.
 */
public interface OnStartDragListener {
    /**
     * Called when a view is requesting a start of a drag.
     *
     * @param viewHolder The holder of the view to drag.
     */
    void onStartDrag(RecyclerView.ViewHolder viewHolder);
}
