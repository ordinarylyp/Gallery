package com.sunmi.commmonlib;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.sunmi.commmonlib.utils.ToastUtils;

/**
 * Description:
 *
 * @author linyuanpeng on 2019-09-14.
 */
public abstract class BaseActivity extends AppCompatActivity {
    protected final String TAG = this.getClass().getSimpleName();
    private long lastClickTime;
    protected Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!needLandscape()) {//默认竖屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        context = this;
        if (activityLayoutId() > 0) {
            setContentView(activityLayoutId());
        }
        initView();
        BaseApplication.getInstance().addActivity(this);
    }

    protected int activityLayoutId() {
        return 0;
    }

    protected void initView() {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 子activity 通过override来设置是否允许横竖屏切换
     */
    protected boolean needLandscape() {
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    /**
     * 短提示
     *
     * @param msg 提示语
     */
    public void shortTip(final String msg) {
        ToastUtils.toastForShort(context, msg);
    }

    /**
     * 短提示
     *
     * @param resId 本地资源id
     */
    public void shortTip(final int resId) {
        ToastUtils.toastForShort(context, resId);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * 防止多次点击事件处理
     */
    public boolean isFastClick(int intervalTime) {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < intervalTime) {
            return true;
        }
        lastClickTime = time;
        return false;
    }
}
