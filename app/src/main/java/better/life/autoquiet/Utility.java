package better.life.autoquiet;

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
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import better.life.autoquiet.Sub.ContextProvider;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility {

    private final String PREFIX = "log_";
    private File packageDir;

    public Utility() {
        this.packageDir = getPackageDirectory();
    }
    private File getPackageDirectory() {

        Context context = ContextProvider.get();
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
    final SimpleDateFormat sdfLogTime = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US);

    public void log(String tag, String text) {
        final StackTraceElement[] traces = Thread.currentThread().getStackTrace();
        StringBuilder log = new StringBuilder();
        for (int i = 5; i > 2; i--) {
//            Log.w("traces "+i, traces[i].getClassName());
//            Log.w("traces "+i, traces[i].getMethodName());
//            Log.w("traces "+i, String.valueOf(traces[i].getLineNumber()));
            if (traces.length > i) {
                String omitStr = omitStr(getLastClass(traces[i].getClassName()));
                if (omitStr.isEmpty()) {
//                    omitStr = "{" + i + "} ";
//                    log.append(omitStr);
                } else {
                    log.append(omitStr)
                        .append("_ ")
                        .append(traces[i].getMethodName())
                        .append("#")
                        .append(traces[i].getLineNumber())
                        .append(" ");
                }
            }
        }
        log.append(" {").append(tag).append("} ").append(text);
        new Thread(() -> {
            String logFile = packageDir + "/" + PREFIX + sdfDate.format(System.currentTimeMillis())+".txt";
            append2file(logFile, sdfLogTime.format(System.currentTimeMillis())+" " +log);
            Log.w(tag, String.valueOf(log));
        }).start();
    }
    private static final Set<String> OMITS_SET = new HashSet<>(Arrays.asList(
            "Activity", "better.life.autoquiet", "Thread", "$$ExternalSyntheticLambda",
            "callActivityOnResume", "access$",
            "onNotificationPosted", "NotificationListener", "performCreate",
            "handleReceiver", "handleMessage", "dispatchKeyEvent", "onBindViewHolder"
    ));

    private String omitStr(String s) {
        for (String o : OMITS_SET) { // Iteration order is not guaranteed and often "random"
            if (s.contains(o)) {
                return "";
            }
        }
        return s + "> ";
    }

    private String getLastClass(String s) {
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