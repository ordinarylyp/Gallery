package com.sunmi.commmonlib.view;

/**
 * Description:
 *
 * @author linyuanpeng on 2019-09-14.
 */
public interface BaseView {

    /**
     * 显示加载中
     */
    void showLoadingDialog();

    void showLoadingDialog(final String text);

    /**
     * 隐藏加载
     */
    void hideLoadingDialog();

    /**
     * toast提示
     */
    void shortTip(int resId);

    void shortTip(String tip);
}
