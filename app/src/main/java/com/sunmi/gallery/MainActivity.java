package com.sunmi.gallery;

import android.content.Intent;
import android.widget.TextView;

import com.sunmi.commmonlib.view.BaseActivity;
import com.sunmi.imagepicker.ImagePicker;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

@EActivity(R.layout.activity_main)
public class MainActivity extends BaseActivity {

    @ViewById(R.id.tv_select_images)
    TextView tvSelectImg;

    private ArrayList<String> imagePaths = new ArrayList<>();

    @AfterViews
    void init() {

    }

    @Click(R.id.bt_select_images)
    void selectImgClick() {
        ImagePicker.getInstance()
                .setTitle("标题")//设置标题
                .showCamera(false)//设置是否显示拍照按钮
                .showImage(true)//设置是否展示图片
                .showVideo(true)//设置是否展示视频
                .setMaxCount(9)//设置最大选择图片数目(默认为1，单选)
                .setSingleType(true)//设置图片视频不能同时选择
                .setImagePaths(imagePaths)//设置历史选择记录
                .setImageLoader(new GlideLoader())//设置自定义图片加载器
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            imagePaths = data.getStringArrayListExtra(ImagePicker.EXTRA_SELECT_IMAGES);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("当前选中图片路径：\n\n");
            for (int i = 0; i < imagePaths.size(); i++) {
                stringBuilder.append(imagePaths.get(i) + "\n\n");
            }
            tvSelectImg.setText(stringBuilder.toString());
        } else {
            imagePaths.clear();
            tvSelectImg.setText("");
        }
    }

}
