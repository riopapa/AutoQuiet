package com.urrecliner.autoquiet;

import static com.urrecliner.autoquiet.Vars.mContext;
import static com.urrecliner.autoquiet.Vars.quietTasks;
import static com.urrecliner.autoquiet.Vars.sdfDate;
import static com.urrecliner.autoquiet.Vars.sdfLogTime;
import static com.urrecliner.autoquiet.Vars.sharedEditor;
import static com.urrecliner.autoquiet.Vars.sharedManner;
import static com.urrecliner.autoquiet.Vars.sharedPref;
import static com.urrecliner.autoquiet.Vars.sharedTimeAfter;
import static com.urrecliner.autoquiet.Vars.sharedTimeBefore;
import static com.urrecliner.autoquiet.Vars.sharedTimeInit;
import static com.urrecliner.autoquiet.Vars.sharedTimeLong;
import static com.urrecliner.autoquiet.Vars.sharedTimeShort;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.urrecliner.autoquiet.models.QuietTask;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Utils {

    private final String PREFIX = "log_";
    private File packageDir = null;
    Context uContext;

    public Utils (Context context) {
        uContext = context;
    }

    private void append2file(String logFile, String textLine) {

        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            File file = new File(logFile);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    log("createFile", " Error");
                }
            }
            String outText = "\n"+textLine+"\n";
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write(outText);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) bw.close();
                if (fw != null) fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File getPackageDirectory() {

        String applicationName = getAppLabel(mContext);
        File directory = new File(Environment.getExternalStorageDirectory(), applicationName);
        try {
            if (!directory.exists()) {
                if(directory.mkdirs()) {
                    Log.e("mkdirs","Failed "+directory);
                }
            }
        } catch (Exception e) {
            Log.e("creating Directory error", directory + "_" + e);
        }
        return directory;
    }

    private String getAppLabel(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(context.getApplicationInfo().packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            Log.e("appl","name error");
        }
        return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "Unknown");
    }

    public void log(String tag, String text) {

        String log = logTrace() + " {"+ tag + "} " + text;
        Log.w(tag , log);
        if (packageDir == null) packageDir = getPackageDirectory();
        String logFile = packageDir + "/" + PREFIX + sdfDate.format(new Date())+".txt";
        append2file(logFile, sdfLogTime.format(new Date())+" " +log);
    }
//
//    void logE(String tag, String text) {
//        String log = logTrace() + " {"+ tag + "} " + text;
//        Log.e("<" + tag + ">" , log);
//        if (packageDir == null) packageDir = getPackageDirectory();
//        String logFile = packageDir.toString() + "/" + PREFIX + sdfDate.format(new Date())+"E.txt";
//        append2file(logFile, sdfLogTime.format(new Date())+" : " +log);
//    }

    private String logTrace () {
//        int pid = android.os.Process.myPid();
        StackTraceElement[] traces;
        traces = Thread.currentThread().getStackTrace();
        return traceName(traces[6].getMethodName()) + traceName(traces[5].getMethodName()) + traceClassName(traces[4].getClassName())+"> "+traces[4].getMethodName() + "#" + traces[4].getLineNumber();
    }

    private static final String[] omits = { "performResume", "performCreate", "callActivityOnResume", "access$",
            "handleReceiver", "handleMessage", "dispatchKeyEvent", "dispatchTransformedTouchEvent","dispatchTouchEvent"};
    private String traceName (String s) {
        for (String o : omits) {
            if (s.contains(o)) return "";
        }
        return s + "> ";
    }
    private String traceClassName(String s) {
        return s.substring(s.lastIndexOf(".")+1);
    }

    void deleteOldLogFiles() {     // remove older than 5 days

        String oldDate = PREFIX + sdfDate.format(System.currentTimeMillis() - 4*24*60*60*1000L);
        if (packageDir == null) packageDir = getPackageDirectory();
        File[] files = getCurrentFileList(packageDir);
        if (files == null)
            return;
        Collator myCollator = Collator.getInstance();
        for (File file : files) {
            String shortFileName = file.getName();
            if (myCollator.compare(shortFileName, oldDate) < 0) {
                if (!file.delete())
                    Log.e("file","Delete Error "+file);
            }
        }
    }

    private File[] getCurrentFileList(File fullPath) {
        return fullPath.listFiles();
    }

    public void saveQuietTasksToShared() {

        for (int i = 0; i < quietTasks.size(); i++) {
            QuietTask q = quietTasks.get(i);
            if (!q.agenda) {
                q.calId = 1000 + i;
                q.calStartDate = 10000 + i;
                quietTasks.set(i, q);
            }
        }
        quietTasks.sort(Comparator.comparingLong(arg0 -> arg0.calStartDate));

        sharedPref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor prefsEditor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(quietTasks);
        prefsEditor.putString("silentInfo", json);
        prefsEditor.apply();
    }

    ArrayList<QuietTask> readQuietTasksFromShared() {

        ArrayList<QuietTask> list;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        Gson gson = new Gson();
        String json = sharedPref.getString("silentInfo", "");
        if (json.isEmpty()) {
            list = new ArrayList<>();
        } else {
            Type type = new TypeToken<List<QuietTask>>() {
            }.getType();
            list = gson.fromJson(json, type);
        }
        return list;
    }

    void getPreference() {
        sharedPref = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
        sharedEditor = sharedPref.edit();
        sharedTimeBefore = sharedPref.getString("timeBefore", "");
        if (sharedTimeBefore.equals("")) {
            sharedEditor.putBoolean("mannerBeep", true);
            sharedEditor.putString("timeInit","60");
            sharedEditor.putString("timeShort", "5");
            sharedEditor.putString("timeLong", "20");
            sharedEditor.putString("timeBefore", "2");
            sharedEditor.apply();
            sharedEditor.commit();
        }
        sharedManner = sharedPref.getBoolean("mannerBeep", true);
        sharedTimeInit = sharedPref.getString("timeInit", "60");
        sharedTimeShort = sharedPref.getString("timeShort", "5");
        sharedTimeLong = sharedPref.getString("timeLong", "20");
        sharedTimeBefore = sharedPref.getString("timeBefore", "10");
        sharedTimeAfter = sharedPref.getString("timeAfter", "-9");
    }

}