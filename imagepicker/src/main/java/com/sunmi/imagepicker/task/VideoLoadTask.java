package com.sunmi.imagepicker.task;

import android.content.Context;

import com.sunmi.imagepicker.data.MediaFile;
import com.sunmi.imagepicker.listener.MediaLoadCallback;
import com.sunmi.imagepicker.loader.MediaHandler;
import com.sunmi.imagepicker.loader.VideoScanner;

import java.util.ArrayList;

/**
 * 媒体库扫描任务（视频）
 * Create by: chenWei.li
 * Date: 2018/8/25
 * Time: 下午12:31
 * Email: lichenwei.me@foxmail.com
 */
public class VideoLoadTask implements Runnable {

    private Context mContext;
    private VideoScanner mVideoScanner;
    private MediaLoadCallback mMediaLoadCallback;

    public VideoLoadTask(Context context, MediaLoadCallback mediaLoadCallback) {
        this.mContext = context;
        this.mMediaLoadCallback = mediaLoadCallback;
        mVideoScanner = new VideoScanner(context);
    }

    @Override
    public void run() {

        //存放所有视频
        ArrayList<MediaFile> videoFileList = new ArrayList<>();

        if (mVideoScanner != null) {
            videoFileList = mVideoScanner.queryMedia();
        }

        if (mMediaLoadCallback != null) {
            mMediaLoadCallback.loadMediaSuccess(MediaHandler.getVideoFolder(mContext, videoFileList));
        }


    }

}
