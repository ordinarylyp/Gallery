package com.sunmi.commmonlib;

import android.app.Activity;
import android.app.Application;

import java.util.LinkedList;
import java.util.List;

/**
 * Description:
 *
 * @author linyuanpeng on 2019-09-14.
 */
public class BaseApplication extends Application {
    private static BaseApplication instance = null;
    private List<Activity> activityList = new LinkedList<>();

    public synchronized static BaseApplication getInstance() {
        if (instance == null) {
            instance = new BaseApplication();
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public void addActivity(Activity activity) {
        if (!activityList.contains(activity))
            activityList.add(activity);
    }

    public void finishActivities() {
        for (Activity activity : activityList) {
            if (activity != null)
                activity.finish();
        }
        activityList.clear();
    }

    /**
     * 在每个activity的onCreate方法中调用addActivity方法，在应用程序退出时调用exit方法，就可以完全退出
     */
    public void quit() {
        try {
            finishActivities();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}
