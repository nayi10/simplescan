package com.bandughana.simplescan;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class ShareBroadcastReceiver extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public void onReceive(Context context, Intent intent){
        ComponentName clickedItem = intent.getParcelableExtra(Intent.EXTRA_CHOSEN_COMPONENT);
    }
}