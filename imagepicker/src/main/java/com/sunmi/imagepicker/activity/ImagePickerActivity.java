package com.sunmi.imagepicker.activity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunmi.commmonlib.router.ImagePickerApi;
import com.sunmi.commmonlib.router.ModuleConfig;
import com.sunmi.commmonlib.utils.PermissionUtils;
import com.sunmi.commmonlib.view.BaseMvpActivity;
import com.sunmi.imagepicker.ImagePicker;
import com.sunmi.imagepicker.R;
import com.sunmi.imagepicker.adapter.ImageFoldersAdapter;
import com.sunmi.imagepicker.adapter.ImagePickerAdapter;
import com.sunmi.imagepicker.contract.ImagePickerContract;
import com.sunmi.imagepicker.data.MediaFile;
import com.sunmi.imagepicker.data.MediaFolder;
import com.sunmi.imagepicker.listener.OnStartDragListener;
import com.sunmi.imagepicker.listener.SimpleItemTouchHelperCallback;
import com.sunmi.imagepicker.manager.ConfigManager;
import com.sunmi.imagepicker.manager.SelectionManager;
import com.sunmi.imagepicker.presenter.ImagePickerPresenter;
import com.sunmi.imagepicker.provider.ImagePickerProvider;
import com.sunmi.imagepicker.utils.DataUtil;
import com.sunmi.imagepicker.utils.MediaFileUtil;
import com.sunmi.imagepicker.utils.Utils;
import com.sunmi.imagepicker.view.ImageFolderPopupWindow;
import com.xiaojinzi.component.anno.RouterAnno;
import com.xiaojinzi.component.impl.BiCallback;
import com.xiaojinzi.component.impl.Router;
import com.xiaojinzi.component.impl.RouterErrorResult;
import com.xiaojinzi.component.impl.RouterRequest;
import com.xiaojinzi.component.impl.RouterResult;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 多图选择器主页面
 */
@EActivity(resName = "activity_imagepicker")
public class ImagePickerActivity extends BaseMvpActivity<ImagePickerPresenter> implements ImagePickerAdapter.OnItemClickListener,
        ImageFoldersAdapter.OnImageFolderChangeListener, OnStartDragListener, ImagePickerContract.View {

    @ViewById(resName = "tv_actionBar_title")
    TextView mTvTitle;
    @ViewById(resName = "tv_actionBar_commit")
    TextView mTvCommit;
    @ViewById(resName = "tv_image_time")
    TextView mTvImageTime;             //滑动悬浮标题相关
    @ViewById(resName = "rv_main_images")
    RecyclerView mRecyclerView;
    @ViewById(resName = "tv_main_imageFolders")
    TextView mTvImageFolders;
    @ViewById(resName = "rl_main_bottom")
    RelativeLayout mRlBottom;

    private ImageFolderPopupWindow mImageFolderPopupWindow;


    /**
     * 启动参数
     */
    private String mTitle;
    private boolean isShowCamera;
    private boolean isShowImage;
    private boolean isShowVideo;
    private boolean isSingleType;
    private int mMaxCount;
    private List<String> mImagePaths;

    /**
     * 界面UI
     */
    private GridLayoutManager mGridLayoutManager;
    private ImagePickerAdapter mImagePickerAdapter;

    //图片数据源
    private List<MediaFile> mMediaFileList;
    //文件夹数据源
    private List<MediaFolder> mMediaFolderList;

    //是否显示时间
    private boolean isShowTime;

    //表示屏幕亮暗
    private static final int LIGHT_OFF = 0;
    private static final int LIGHT_ON = 1;

    private Handler mMyHandler = new Handler();
    private Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hideImageTime();
        }
    };


    /**
     * 大图预览页相关
     */
    private static final int REQUEST_SELECT_IMAGES_CODE = 0x01;//用于在大图预览页中点击提交按钮标识


    /**
     * 拍照相关
     */
    private String mFilePath;
    private static final int REQUEST_CODE_CAPTURE = 0x02;//点击拍照标识

    /**
     * RecyclerView拖拽移动相关
     */
    private ItemTouchHelper mItemTouchHelper;


    /**
     * 路由启动Activity
     *
     * @param request
     * @return
     */
    @RouterAnno(
            path = ModuleConfig.ImagePicker.IMAGEOICKER
    )
    public static Intent start(RouterRequest request) {
        Intent intent = new Intent(request.getRawContext(), ImagePickerActivity_.class);
        return intent;
    }

    /**
     * 初始化配置
     */
    protected void initConfig() {
        mTitle = ConfigManager.getInstance().getTitle();
        isShowCamera = ConfigManager.getInstance().isShowCamera();
        isShowImage = ConfigManager.getInstance().isShowImage();
        isShowVideo = ConfigManager.getInstance().isShowVideo();
        isSingleType = ConfigManager.getInstance().isSingleType();
        mMaxCount = ConfigManager.getInstance().getMaxCount();
        SelectionManager.getInstance().setMaxCount(mMaxCount);

        //载入历史选择记录
        mImagePaths = ConfigManager.getInstance().getImagePaths();
        if (mImagePaths != null && !mImagePaths.isEmpty()) {
            SelectionManager.getInstance().addImagePathsToSelectList(mImagePaths);
        }
    }

    /**
     * 初始化布局控件
     */
    @AfterViews
    void init() {
        initConfig();
        if (TextUtils.isEmpty(mTitle)) {
            mTvTitle.setText(getString(R.string.image_picker));
        } else {
            mTvTitle.setText(mTitle);
        }
        mPresenter = new ImagePickerPresenter();
        mPresenter.attachView(this);
        showLoadingDialog(getString(R.string.scanner_image));
        mGridLayoutManager = new GridLayoutManager(this, 4);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        //注释说当知道Adapter内Item的改变不会影响RecyclerView宽高的时候，可以设置为true让RecyclerView避免重新计算大小。
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemViewCacheSize(60);

        mMediaFileList = new ArrayList<>();
        mImagePickerAdapter = new ImagePickerAdapter(this, mMediaFileList, this);
        mImagePickerAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mImagePickerAdapter);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mImagePickerAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        initListener();
        getData();
    }

    /**
     * 初始化控件监听事件
     */
    protected void initListener() {

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                updateImageTime();
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                updateImageTime();
            }
        });

    }

    @Click(resName = "iv_actionBar_back")
    protected void backClick() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Click(resName = "tv_actionBar_commit")
    protected void commitClick() {
        commitSelection();
    }

    @Click(resName = "tv_main_imageFolders")
    protected void imageFoldersClick() {
        if (mImageFolderPopupWindow != null) {
            setLightMode(LIGHT_OFF);
            mImageFolderPopupWindow.showAsDropDown(mRlBottom, 0, 0);
        }
    }


    /**
     * 获取数据源
     */
    private void getData() {
        //进行权限的判断
        if (PermissionUtils.checkSDCardCameraPermission(this)) {
            mPresenter.loadMedia(context, isShowImage, isShowVideo);
        }
    }

    @UiThread
    @Override
    public void loadMediaSuccess(List<MediaFolder> mediaFolderList) {
        if (!mediaFolderList.isEmpty()) {
            //默认加载全部照片
            mMediaFileList.addAll(mediaFolderList.get(0).getMediaFileList());
            mImagePickerAdapter.notifyDataSetChanged();

            //图片文件夹数据
            mMediaFolderList = new ArrayList<>(mediaFolderList);
            mImageFolderPopupWindow = new ImageFolderPopupWindow(ImagePickerActivity.this, mMediaFolderList);
            mImageFolderPopupWindow.setAnimationStyle(R.style.imageFolderAnimator);
            mImageFolderPopupWindow.getAdapter().setOnImageFolderChangeListener(ImagePickerActivity.this);
            mImageFolderPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    setLightMode(LIGHT_ON);
                }
            });
            updateCommitButton();
        }
        hideLoadingDialog();
    }

    /**
     * 权限申请回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.REQ_PERMISSIONS_CAMERA_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mPresenter.loadMedia(context, isShowImage, isShowVideo);
            } else {
                shortTip(R.string.permission_tip);
                finish();
            }
        }
    }


    /**
     * 隐藏时间
     */
    private void hideImageTime() {
        if (isShowTime) {
            isShowTime = false;
            ObjectAnimator.ofFloat(mTvImageTime, "alpha", 1, 0).setDuration(300).start();
        }
    }

    /**
     * 显示时间
     */
    private void showImageTime() {
        if (!isShowTime) {
            isShowTime = true;
            ObjectAnimator.ofFloat(mTvImageTime, "alpha", 0, 1).setDuration(300).start();
        }
    }

    /**
     * 更新时间
     */
    private void updateImageTime() {
        int position = mGridLayoutManager.findFirstVisibleItemPosition();
        MediaFile mediaFile = mImagePickerAdapter.getMediaFile(position);
        if (mediaFile != null) {
            if (mTvImageTime.getVisibility() != View.VISIBLE) {
                mTvImageTime.setVisibility(View.VISIBLE);
            }
            String time = Utils.getImageTime(mediaFile.getDateToken());
            mTvImageTime.setText(time);
            showImageTime();
            mMyHandler.removeCallbacks(mHideRunnable);
            mMyHandler.postDelayed(mHideRunnable, 1500);
        }
    }

    /**
     * 设置屏幕的亮度模式
     *
     * @param lightMode
     */
    private void setLightMode(int lightMode) {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        switch (lightMode) {
            case LIGHT_OFF:
                layoutParams.alpha = 0.7f;
                break;
            case LIGHT_ON:
                layoutParams.alpha = 1.0f;
                break;
            default:
                break;
        }
        getWindow().setAttributes(layoutParams);
    }

    /**
     * 点击图片
     *
     * @param view
     * @param position
     */
    @Override
    public void onMediaClick(View view, int position) {
        if (isShowCamera) {
            if (position == 0) {
                if (!SelectionManager.getInstance().isCanChoose()) {
                    shortTip(getString(R.string.select_image_max, mMaxCount));
                    return;
                }
                showCamera();
                return;
            }
        }

        if (mMediaFileList != null) {
            DataUtil.getInstance().setMediaData(mMediaFileList);
            if (isShowCamera) {
                Router
                        .withApi(ImagePickerApi.class)
                        .goToImagePre(this, position - 1, new BiCallback<Integer>() {
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
            } else {
                Router
                        .withApi(ImagePickerApi.class)
                        .goToImagePre(this, position, new BiCallback<Integer>() {
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
    }

    /**
     * 选中/取消选中图片
     *
     * @param view
     * @param position
     */
    @Override
    public void onMediaCheck(View view, int position) {
        if (isShowCamera) {
            if (position == 0) {
                if (!SelectionManager.getInstance().isCanChoose()) {
                    shortTip(R.string.select_image_max);
                    return;
                }
                showCamera();
                return;
            }
        }
        //执行选中/取消操作
        MediaFile mediaFile = mImagePickerAdapter.getMediaFile(position);
        if (mediaFile != null) {
            String imagePath = mediaFile.getPath();
            if (isSingleType) {
                //如果是单类型选取，判断添加类型是否满足（照片视频不能共存）
                ArrayList<String> selectPathList = SelectionManager.getInstance().getSelectPaths();
                if (!selectPathList.isEmpty()) {
                    //判断选中集合中第一项是否为视频
                    if (!SelectionManager.isCanAddSelectionPaths(imagePath, selectPathList.get(0))) {
                        //类型不同
                        shortTip(R.string.single_type_choose);
                        return;
                    }
                }
            }
            boolean addSuccess = SelectionManager.getInstance().addImageToSelectList(imagePath);
            if (addSuccess) {
                mImagePickerAdapter.notifyItemChanged(position);
            } else {
                shortTip(R.string.select_image_max);
            }
        }
        updateCommitButton();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    /**
     * 更新确认按钮状态
     */
    private void updateCommitButton() {
        //改变确定按钮UI
        int selectCount = SelectionManager.getInstance().getSelectPaths().size();
        if (selectCount == 0) {
            mTvCommit.setEnabled(false);
            mTvCommit.setText(getString(R.string.confirm));
            return;
        }
        if (selectCount <= mMaxCount) {
            mTvCommit.setEnabled(true);
            mTvCommit.setText(getString(R.string.confirm_msg, selectCount, mMaxCount));
            return;
        }
    }

    /**
     * 跳转相机拍照
     */
    private void showCamera() {

        if (isSingleType) {
            //如果是单类型选取，判断添加类型是否满足（照片视频不能共存）
            ArrayList<String> selectPathList = SelectionManager.getInstance().getSelectPaths();
            if (!selectPathList.isEmpty()) {
                if (MediaFileUtil.isVideoFileType(selectPathList.get(0))) {
                    //如果存在视频，就不能拍照了
                    shortTip(R.string.single_type_choose);
                    return;
                }
            }
        }

        //拍照存放路径
        File fileDir = new File(Environment.getExternalStorageDirectory(), "Pictures");
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        mFilePath = fileDir.getAbsolutePath() + "/IMG_" + System.currentTimeMillis() + ".jpg";

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri uri;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(this, ImagePickerProvider.getFileProviderName(this), new File(mFilePath));
        } else {
            uri = Uri.fromFile(new File(mFilePath));
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, REQUEST_CODE_CAPTURE);
    }

    /**
     * 当图片文件夹切换时，刷新图片列表数据源
     *
     * @param view
     * @param position
     */
    @Override
    public void onImageFolderChange(View view, int position) {
        MediaFolder mediaFolder = mMediaFolderList.get(position);
        //更新当前文件夹名
        String folderName = mediaFolder.getFolderName();
        if (!TextUtils.isEmpty(folderName)) {
            mTvImageFolders.setText(folderName);
        }
        //更新图片列表数据源
        mMediaFileList.clear();
        mMediaFileList.addAll(mediaFolder.getMediaFileList());
        mImagePickerAdapter.notifyDataSetChanged();

        mImageFolderPopupWindow.dismiss();
    }

    /**
     * 拍照回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAPTURE) {
                //通知媒体库刷新
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + mFilePath)));
                //添加到选中集合
                SelectionManager.getInstance().addImageToSelectList(mFilePath);

                ArrayList<String> list = new ArrayList<>(SelectionManager.getInstance().getSelectPaths());
                Intent intent = new Intent();
                intent.putStringArrayListExtra(ImagePicker.EXTRA_SELECT_IMAGES, list);
                setResult(RESULT_OK, intent);
                SelectionManager.getInstance().removeAll();//清空选中记录
                finish();
            }

            if (requestCode == REQUEST_SELECT_IMAGES_CODE) {
                commitSelection();
            }
        }
    }

    /**
     * 选择图片完毕，返回
     */
    private void commitSelection() {
        ArrayList<String> list = new ArrayList<>(SelectionManager.getInstance().getSelectPaths());
        Intent intent = new Intent();
        intent.putStringArrayListExtra(ImagePicker.EXTRA_SELECT_IMAGES, list);
        setResult(RESULT_OK, intent);
        SelectionManager.getInstance().removeAll();//清空选中记录
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mImagePickerAdapter.notifyDataSetChanged();
        updateCommitButton();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            ConfigManager.getInstance().getImageLoader().clearMemoryCache();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
