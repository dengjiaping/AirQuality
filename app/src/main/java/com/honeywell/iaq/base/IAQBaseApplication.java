
package com.honeywell.iaq.base;

import android.app.Application;

import com.honeywell.iaq.BuildConfig;
import com.honeywell.lib.application.CrashHandler;
import com.honeywell.lib.application.LogcatHelper;
import com.honeywell.lib.utils.DebugUtil;

public class IAQBaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DebugUtil.DEBUG= BuildConfig.LOG_DEBUG;
//        CrashHandler.getInstance().init(this);
//        LogcatHelper.getInstance(this).start();

        if (DebugUtil.DEBUG) {
            LogcatHelper.getInstance(this).start();
        }
    }
}
