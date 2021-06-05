package com.urrecliner.letmequiet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import com.urrecliner.letmequiet.models.Agenda;
import com.urrecliner.letmequiet.models.QuietTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class Vars {
    static int xSize; // width for each week in AddUpdateActivity
    static String[] weekName = {"주", "월", "화", "수", "목", "금", "토"};
    public static Utils utils = null;

    public static boolean addNewQuiet = false;
    static MainActivity mActivity;
    public static Context mContext;
    static RecycleViewAdapter recycleViewAdapter;

    static String stateCode;

    static SharedPreferences sharedPref;
    static String sharedTimeShort, sharedTimeLong, sharedTimeInit;
    static boolean sharedManner = true;

    static final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    static final SimpleDateFormat sdfDateTime = new SimpleDateFormat("MM-dd HH:mm", Locale.US);
    static final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.US);
    static final SimpleDateFormat sdfLogTime = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US);
    static QuietTask quietTask;
    public static ArrayList<Agenda> agendas;
    public static ArrayList<QuietTask> quietTasks;
    static int quietUniq = 123456;

    static final String STATE_BLANK = "BLANK";
    static final String STATE_ALARM = "Alarm";
    static final String STATE_ONETIME = "OneTime";
    static final String STATE_BOOT = "Boot";
    static final String STATE_ADD_UPDATE = "AddUpdate";
    static Handler actionHandler;
    static final int INVOKE_ONETIME = 100;
    static final int STOP_SPEAK = 10022;
    static boolean notScheduled = true;

}
