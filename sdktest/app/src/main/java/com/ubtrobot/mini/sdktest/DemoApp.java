package com.ubtrobot.mini.sdktest;

import android.app.Application;

import com.ubtrobot.mini.SDKInit;
import com.ubtrobot.mini.properties.sdk.Path;
import com.ubtrobot.mini.properties.sdk.PropertiesApi;

public class DemoApp extends Application {

    public static final String DEBUG_TAG = "API_TAG";
    @Override
    public void onCreate() {
        super.onCreate();
        // 可能是用于指定应用程序的数据存储位置
        PropertiesApi.setRootPath(Path.DIR_MINI_FILES_SDCARD_ROOT);
        // 初始化了 SDK，它使用应用程序的上下文作为参数
        SDKInit.initialize(this);
    }

//    @Override
//    protected void onStartFailed(UbtSkillInfo ubtSkillInfo) {
//
//    }
//
//    @Override
//    protected void onInterrupted() {
//
//    }
}
