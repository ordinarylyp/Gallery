package com.sunmi.commmonlib.view;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.sunmi.commmonlib.BaseApplication;
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
    protected LoadingDialog loadingDialog;

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
        initDialog();
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
        hideLoadingDialog();    //防止窗口泄漏
        loadingDialog = null;
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


    private void initDialog() {
        if (loadingDialog == null) {
            synchronized (this) {
                if (loadingDialog == null) {
                    loadingDialog = new LoadingDialog(this);
                    loadingDialog.setCanceledOnTouchOutside(false);
                }
            }
        }
    }

    /**
     * 显示加载框
     */
    public void showLoadingDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loadingDialog == null || loadingDialog.isShowing()) {
                    return;
                }
                loadingDialog.setLoadingContent(null);
                loadingDialog.show();
            }
        });
    }

    /**
     * 显示加载框,content为null是不可点击消失
     */
    public void showLoadingDialog(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loadingDialog == null || loadingDialog.isShowing()) {
                    return;
                }
                loadingDialog.setCancelable(false);
                if (!TextUtils.isEmpty(text))
                    loadingDialog.setLoadingContent(text);
                loadingDialog.show();
            }
        });
    }

    /**
     * 显示加载框,content为null是不可点击消失
     */
    public void showLoadingDialog(final String text, @ColorInt final int textColor) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loadingDialog == null || loadingDialog.isShowing()) {
                    return;
                }
                loadingDialog.setCancelable(false);
                if (!TextUtils.isEmpty(text))
                    loadingDialog.setTipColorText(text, textColor);
                loadingDialog.show();
            }
        });
    }

    /**
     * 关闭加载框
     */
    public void hideLoadingDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loadingDialog == null) {
                    return;
                }
                loadingDialog.setCancelable(true);
                loadingDialog.dismiss();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideLoadingDialog();
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
