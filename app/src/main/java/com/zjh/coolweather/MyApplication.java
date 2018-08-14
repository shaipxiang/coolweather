package com.zjh.coolweather;

import android.app.Application;

import org.litepal.LitePal;

/**
 * Author：Created by zhaojh on 2018/8/14 16:48.
 * Description:
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
    }
}
