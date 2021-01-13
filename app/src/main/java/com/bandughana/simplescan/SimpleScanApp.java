package com.bandughana.simplescan;

import android.app.Application;

public class SimpleScanApp extends Application {
    @Override
    public void onCreate() {
        ObjectBox.init(this);
        super.onCreate();
    }
}
