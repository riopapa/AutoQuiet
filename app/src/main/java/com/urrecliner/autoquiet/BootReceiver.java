package com.urrecliner.autoquiet;

import static com.urrecliner.autoquiet.Vars.STATE_BOOT;
import static com.urrecliner.autoquiet.Vars.mContext;
import static com.urrecliner.autoquiet.Vars.stateCode;
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
        stateCode = STATE_BOOT;
        Log.e("Booted",stateCode);

        Intent i = new Intent(context, MainActivity.class);
        i.putExtra("stateCode", stateCode);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(i);

    }
}