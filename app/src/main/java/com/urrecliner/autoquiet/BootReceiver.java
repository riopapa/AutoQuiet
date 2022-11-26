package com.urrecliner.autoquiet;

import static com.urrecliner.autoquiet.Vars.SHARED_CODE;
import static com.urrecliner.autoquiet.Vars.STATE_BOOT;
import static com.urrecliner.autoquiet.Vars.STATE_LOOP;
import static com.urrecliner.autoquiet.Vars.mContext;
import static com.urrecliner.autoquiet.Vars.sharedCode;
import static com.urrecliner.autoquiet.Vars.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        utils = new Utils(context);
        mContext = context;
        utils.log(STATE_BOOT, "Activated  BOOT ------------- " + intent.getAction());
        sharedCode = STATE_BOOT;
        Log.e("Booted", sharedCode);

        Intent i = new Intent(context, MainActivity.class);
        i.putExtra(SHARED_CODE, sharedCode);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);

    }
}