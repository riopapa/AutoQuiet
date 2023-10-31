package com.riopapa.autoquiet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.riopapa.autoquiet.models.QuietTask;

import java.util.ArrayList;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

                @Override public void run() {
                    new SetUpComingTask(context, "After Boot");
                }
            }, 10000);
        }
    }

}