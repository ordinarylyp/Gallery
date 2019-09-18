package com.sunmi.imagepicker;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sunmi.commmonlib.router.ImagePickerApi;
import com.sunmi.imagepicker.manager.ConfigManager;
import com.sunmi.imagepicker.utils.ImageLoader;
import com.xiaojinzi.component.impl.BiCallback;
import com.xiaojinzi.component.impl.Router;
import com.xiaojinzi.component.impl.RouterErrorResult;
import com.xiaojinzi.component.impl.RouterRequest;
import com.xiaojinzi.component.impl.RouterResult;

import java.util.ArrayList;

/**
 * 统一调用入口
 * Create by: chenWei.li
 * Date: 2018/8/26
 * Time: 下午6:31
 * Email: lichenwei.me@foxmail.com
 */
public class ImagePicker {

    public static final String EXTRA_SELECT_IMAGES = "selectItems";

    private static volatile ImagePicker mImagePicker;

    private ImagePicker() {
    }

    /**
     * 创建对象
     *
     * @return
     */
    public static ImagePicker getInstance() {
        if (mImagePicker == null) {
            synchronized (ImagePicker.class) {
                if (mImagePicker == null) {
                    mImagePicker = new ImagePicker();
                }
            }
        }
        return mImagePicker;
    }


    /**
     * 设置标题
     *
     * @param title
     * @return
     */
    public ImagePicker setTitle(String title) {
        ConfigManager.getInstance().setTitle(title);
        return mImagePicker;
    }

    /**
     * 是否支持相机
     *
     * @param showCamera
     * @return
     */
    public ImagePicker showCamera(boolean showCamera) {
        ConfigManager.getInstance().setShowCamera(showCamera);
        return mImagePicker;
    }

    /**
     * 是否展示图片
     *
     * @param showImage
     * @return
     */
    public ImagePicker showImage(boolean showImage) {
        ConfigManager.getInstance().setShowImage(showImage);
        return mImagePicker;
    }

    /**
     * 是否展示视频
     *
     * @param showVideo
     * @return
     */
    public ImagePicker showVideo(boolean showVideo) {
        ConfigManager.getInstance().setShowVideo(showVideo);
        return mImagePicker;
    }


    /**
     * 图片最大选择数
     *
     * @param maxCount
     * @return
     */
    public ImagePicker setMaxCount(int maxCount) {
        ConfigManager.getInstance().setMaxCount(maxCount);
        return mImagePicker;
    }

    /**
     * 设置单类型选择（只能选图片或者视频）
     *
     * @param isSingleType
     * @return
     */
    public ImagePicker setSingleType(boolean isSingleType) {
        ConfigManager.getInstance().setSingleType(isSingleType);
        return mImagePicker;
    }


    /**
     * 设置图片加载器
     *
     * @param imageLoader
     * @return
     */
    public ImagePicker setImageLoader(ImageLoader imageLoader) {
        ConfigManager.getInstance().setImageLoader(imageLoader);
        return mImagePicker;
    }

    /**
     * 设置图片选择历史记录
     *
     * @param imagePaths
     * @return
     */
    public ImagePicker setImagePaths(ArrayList<String> imagePaths) {
        ConfigManager.getInstance().setImagePaths(imagePaths);
        return mImagePicker;
    }

    /**
     * 启动
     *
     * @param activity
     */
    public void start(Activity activity) {
        Router.
                withApi(ImagePickerApi.class).goToImagePicker(activity, new BiCallback<Integer>() {
            @Override
            public void onSuccess(@NonNull RouterResult result, @NonNull Integer integer) {

            }

            @Override
            public void onCancel(@Nullable RouterRequest originalRequest) {

            }

            @Override
            public void onError(@NonNull RouterErrorResult errorResult) {

            }
        });

    }

}
