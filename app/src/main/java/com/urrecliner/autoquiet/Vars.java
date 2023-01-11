package com.urrecliner.autoquiet;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Insets;
import android.view.WindowInsets;
import android.view.WindowMetrics;

import com.urrecliner.autoquiet.models.GCal;
import com.urrecliner.autoquiet.models.QuietTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class Vars {

    public int xSize; // width for each week in AddUpdateActivity
    public boolean addNewQuiet = false;

    public String sharedTimeShort, sharedTimeLong, sharedTimeInit,
            sharedTimeBefore, sharedTimeAfter;
    public boolean sharedManner = true;
    public int quietUnique = 123456;

    public final String STATE_BLANK = "BLANK";
    public final String STATE_ALARM = "Alarm";
    public final String STATE_ONETIME = "OneTime";
    public final String STATE_LOOP = "Loop";
    public final String STATE_BOOT = "Boot";
    public final int INVOKE_ONETIME = 100;
    public final int STOP_SPEAK = 1022;

    final String logID = "vars";

}