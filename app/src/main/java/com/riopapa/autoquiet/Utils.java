package com.riopapa.autoquiet;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    private final String PREFIX = "log_";
    private File packageDir;
    private final Context context;

    public Utils(Context context) {
        this.context = context;
        this.packageDir = getPackageDirectory();
    }
    private File getPackageDirectory() {

        String applicationName = getAppLabel(context);
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

    private String getAppLabel(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(context.getApplicationInfo().packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            Log.e("getAppLabel","name error");
        }
        return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "Unknown");
    }

    final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    final SimpleDateFormat sdfLogTime = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US);

    public void log(String tag, String text) {

        String log = logTrace() + " {"+ tag + "} " + text;
        Log.w(tag , log);
        String logFile = packageDir + "/" + PREFIX + sdfDate.format(new Date())+".txt";
        append2file(logFile, sdfLogTime.format(new Date())+" " +log);
    }

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

    public void deleteOldLogFiles() {     // remove older than 5 days

        String oldDate = PREFIX + sdfDate.format(System.currentTimeMillis() - 4*24*60*60*1000L);
        if (packageDir == null) packageDir = getPackageDirectory();
        File[] files = packageDir.listFiles();
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

}