package com.sunmi.imagepicker.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunmi.commmonlib.router.ModuleConfig;
import com.sunmi.commmonlib.view.BaseActivity;
import com.sunmi.imagepicker.R;
import com.sunmi.imagepicker.adapter.ImagePreViewAdapter;
import com.sunmi.imagepicker.data.MediaFile;
import com.sunmi.imagepicker.manager.ConfigManager;
import com.sunmi.imagepicker.manager.SelectionManager;
import com.sunmi.imagepicker.provider.ImagePickerProvider;
import com.sunmi.imagepicker.utils.DataUtil;
import com.sunmi.imagepicker.view.HackyViewPager;
import com.xiaojinzi.component.anno.RouterAnno;
import com.xiaojinzi.component.impl.RouterRequest;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 大图预览界面
 */
@EActivity(resName = "activity_pre_image")
public class ImagePreActivity extends BaseActivity {

    @ViewById(resName = "tv_actionBar_title")
    TextView mTvTitle;
    @ViewById(resName = "tv_actionBar_commit")
    TextView mTvCommit;
    @ViewById(resName = "iv_main_play")
    ImageView mIvPlay;
    @ViewById(resName = "vp_main_preImage")
    HackyViewPager mViewPager;
    @ViewById(resName = "iv_item_check")
    ImageView mIvPreCheck;

    public static final String IMAGE_POSITION = "imagePosition";
    private List<MediaFile> mMediaFileList;
    private int mPosition = 0;
    private ImagePreViewAdapter mImagePreViewAdapter;


    @RouterAnno(
            path = ModuleConfig.ImagePicker.IMAGEPRE
    )
    public static Intent start(RouterRequest request) {
        Intent intent = new Intent(request.getRawContext(), ImagePreActivity_.class);
        return intent;
    }

    @AfterViews
    void init() {
        mMediaFileList = DataUtil.getInstance().getMediaData();
        mPosition = getIntent().getIntExtra(IMAGE_POSITION, 0);
        mTvTitle.setText(String.format("%d/%d", mPosition + 1, mMediaFileList.size()));
        mImagePreViewAdapter = new ImagePreViewAdapter(this, mMediaFileList);
        mViewPager.setAdapter(mImagePreViewAdapter);
        mViewPager.setCurrentItem(mPosition);
        //更新当前页面状态
        setIvPlayShow(mMediaFileList.get(mPosition));
        updateSelectButton(mMediaFileList.get(mPosition).getPath());
        updateCommitButton();
        initListener();
    }


    protected void initListener() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTvTitle.setText(String.format("%d/%d", position + 1, mMediaFileList.size()));
                setIvPlayShow(mMediaFileList.get(position));
                updateSelectButton(mMediaFileList.get(position).getPath());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    @Click(resName = "iv_actionBar_back")
    protected void backClick() {
        finish();
    }

    @Click(resName = "ll_pre_select")
    protected void selectClick() {
        //如果是单类型选取，判断添加类型是否满足（照片视频不能共存）
        if (ConfigManager.getInstance().isSingleType()) {
            ArrayList<String> selectPathList = SelectionManager.getInstance().getSelectPaths();
            if (!selectPathList.isEmpty()) {
                //判断选中集合中第一项是否为视频
                if (!SelectionManager.isCanAddSelectionPaths(mMediaFileList.get(mViewPager.getCurrentItem()).getPath(), selectPathList.get(0))) {
                    //类型不同
                    shortTip(R.string.single_type_choose);
                    return;
                }
            }
        }

        boolean addSuccess = SelectionManager.getInstance().addImageToSelectList(mMediaFileList.get(mViewPager.getCurrentItem()).getPath());
        if (addSuccess) {
            updateSelectButton(mMediaFileList.get(mViewPager.getCurrentItem()).getPath());
            updateCommitButton();
        } else {
            shortTip(getString(R.string.select_image_max, SelectionManager.getInstance().getMaxCount()));
        }
    }

    @Click(resName = "tv_actionBar_commit")
    protected void commitClick() {
        setResult(RESULT_OK, new Intent());
        finish();
    }

    @Click(resName = "iv_main_play")
    protected void playClick() {
        //实现播放视频的跳转逻辑(调用原生视频播放器)
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(context, ImagePickerProvider.getFileProviderName(context),
                new File(mMediaFileList.get(mViewPager.getCurrentItem()).getPath()));
        intent.setDataAndType(uri, "video/*");
        //给所有符合跳转条件的应用授权
        List<ResolveInfo> resInfoList = getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        startActivity(intent);
    }

    /**
     * 更新确认按钮状态
     */
    private void updateCommitButton() {

        int maxCount = SelectionManager.getInstance().getMaxCount();

        //改变确定按钮UI
        int selectCount = SelectionManager.getInstance().getSelectPaths().size();
        if (selectCount == 0) {
            mTvCommit.setEnabled(false);
            mTvCommit.setText(getString(R.string.confirm));
            return;
        }
        if (selectCount < maxCount) {
            mTvCommit.setEnabled(true);
            mTvCommit.setText(String.format(getString(R.string.confirm_msg), selectCount, maxCount));
            return;
        }
        if (selectCount == maxCount) {
            mTvCommit.setEnabled(true);
            mTvCommit.setText(String.format(getString(R.string.confirm_msg), selectCount, maxCount));
            return;
        }
    }

    /**
     * 更新选择按钮状态
     */
    private void updateSelectButton(String imagePath) {
        boolean isSelect = SelectionManager.getInstance().isImageSelect(imagePath);
        if (isSelect) {
            mIvPreCheck.setImageDrawable(getResources().getDrawable(R.mipmap.icon_image_checked));
        } else {
            mIvPreCheck.setImageDrawable(getResources().getDrawable(R.mipmap.icon_image_check));
        }
    }

    /**
     * 设置是否显示视频播放按钮
     *
     * @param mediaFile
     */
    private void setIvPlayShow(MediaFile mediaFile) {
        if (mediaFile.getDuration() > 0) {
            mIvPlay.setVisibility(View.VISIBLE);
        } else {
            mIvPlay.setVisibility(View.GONE);
        }
    }

}
