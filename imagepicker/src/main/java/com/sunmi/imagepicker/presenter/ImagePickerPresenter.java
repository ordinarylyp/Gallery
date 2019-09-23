package com.sunmi.imagepicker.presenter;

import android.content.Context;

import com.sunmi.commmonlib.view.BasePresenter;
import com.sunmi.imagepicker.contract.ImagePickerContract;
import com.sunmi.imagepicker.data.MediaFolder;
import com.sunmi.imagepicker.executors.CommonExecutor;
import com.sunmi.imagepicker.listener.MediaLoadCallback;
import com.sunmi.imagepicker.task.ImageLoadTask;
import com.sunmi.imagepicker.task.MediaLoadTask;
import com.sunmi.imagepicker.task.VideoLoadTask;

import java.util.List;

/**
 * Description:
 *
 * @author linyuanpeng on 2019-09-23.
 */
public class ImagePickerPresenter extends BasePresenter<ImagePickerContract.View>
        implements ImagePickerContract.Presenter {

    @Override
    public void loadMedia(final Context context, boolean isShowImage, boolean isShowVideo) {
        Runnable mediaLoadTask = null;
        MediaLoadCallback callback = new MediaLoadCallback() {
            @Override
            public void loadMediaSuccess(List<MediaFolder> mediaFolderList) {
                if (isViewAttached()) {
                    mView.loadMediaSuccess(mediaFolderList);
                }
            }
        };

        //照片、视频全部加载
        if (isShowImage && isShowVideo) {
            mediaLoadTask = new MediaLoadTask(context, callback);
        }

        //只加载视频
        if (!isShowImage && isShowVideo) {
            mediaLoadTask = new VideoLoadTask(context, callback);
        }

        //只加载图片
        if (isShowImage && !isShowVideo) {
            mediaLoadTask = new ImageLoadTask(context, callback);
        }

        //不符合以上场景，采用照片、视频全部加载
        if (mediaLoadTask == null) {
            mediaLoadTask = new MediaLoadTask(context, callback);
        }
        CommonExecutor.getInstance().execute(mediaLoadTask);
    }
}
