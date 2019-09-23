package com.sunmi.commmonlib.view;

/**
 * Description:
 *
 * @author linyuanpeng on 2019-09-14.
 */
public abstract class BaseMvpActivity<T extends BasePresenter> extends BaseActivity
        implements BaseView {
    protected T mPresenter;

    @Override
    protected void onDestroy() {
        if (mPresenter != null) {
            mPresenter.detachView();
        }
        super.onDestroy();
    }

}
