package com.sunmi.gallery;

import com.sunmi.commmonlib.BaseApplication;
import com.sunmi.commmonlib.router.ModuleConfig;
import com.xiaojinzi.component.Component;
import com.xiaojinzi.component.impl.application.ModuleManager;

/**
 * Description:
 *
 * @author linyuanpeng on 2019-09-16.
 */
public class MyApplication extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Component.init(this, BuildConfig.DEBUG);


        // 装载各个业务组件
        ModuleManager.getInstance().registerArr(
                ModuleConfig.App.NAME, ModuleConfig.ImagePicker.NAME
        );

        if (BuildConfig.DEBUG) {
            ModuleManager.getInstance().check();
        }
    }
}
