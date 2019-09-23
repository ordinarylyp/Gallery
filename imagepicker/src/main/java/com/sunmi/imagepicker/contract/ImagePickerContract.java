package com.sunmi.imagepicker.contract;

import android.content.Context;

import com.sunmi.commmonlib.view.BaseView;
import com.sunmi.imagepicker.data.MediaFolder;

import java.util.List;

/**
 * Description:
 *
 * @author linyuanpeng on 2019-09-23.
 */
public interface ImagePickerContract {

    interface View extends BaseView{
       void loadMediaSuccess(final List<MediaFolder> mediaFolderList);
    }

    interface Presenter{
        void loadMedia(Context context, boolean isShowImage,boolean isShowVideo);
    }

}
