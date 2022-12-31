package com.urrecliner.autoquiet;

import static com.urrecliner.autoquiet.Vars.SHARED_CODE;
import static com.urrecliner.autoquiet.Vars.STATE_ALARM;
import static com.urrecliner.autoquiet.Vars.STATE_BLANK;
import static com.urrecliner.autoquiet.Vars.STATE_BOOT;
import static com.urrecliner.autoquiet.Vars.STATE_ONETIME;
import static com.urrecliner.autoquiet.Vars.addNewQuiet;
import static com.urrecliner.autoquiet.Vars.mActivity;
import static com.urrecliner.autoquiet.Vars.mContext;
import static com.urrecliner.autoquiet.Vars.quietTasks;
import static com.urrecliner.autoquiet.Vars.sharedCode;
import static com.urrecliner.autoquiet.Vars.sharedEditor;
import static com.urrecliner.autoquiet.Vars.sharedPref;
import static com.urrecliner.autoquiet.Vars.sounds;
import static com.urrecliner.autoquiet.Vars.utils;
import static com.urrecliner.autoquiet.Vars.xSize;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowInsets;
import android.view.WindowMetrics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.urrecliner.autoquiet.utility.ClearQuiteTasks;
import com.urrecliner.autoquiet.utility.Permission;

public class MainActivity extends AppCompatActivity  {

    private static final String logID = "Main";
    private static Context mainContext;
    public static Context getContext() {
        return mainContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        mContext = this.getApplicationContext();
        mainContext = mContext;
        setContentView(R.layout.activity_main);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getApplicationContext()
                    .getPackageName(), PackageManager.GET_PERMISSIONS);
            Permission.ask(this, this, info);
        } catch (Exception e) {
            Log.e("Permission", "No Permission "+e);
        }

// If you have access to the external storage, do whatever you need
        if (!Environment.isExternalStorageManager()){
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", this.getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }
        sharedCode = logID;
        Log.w("autoQuiet","onCreated ----- "+sharedCode);
    }

    @Override
    public void onResume() {

        super.onResume();
        Log.w("Main","onResume is called");
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Log.w("Permission","Required for READ_CALENDAR");
        }
        setVariables();
        if (!sharedCode.equals(logID)) {
            new ScheduleNextTask("Next Alarm");
            finish();
        } else {
            new ShowMainList();
//            sharedCode = STATE_ALARM;
//            sharedEditor.putString(SHARED_CODE, sharedCode);
//            sharedEditor.apply();
        }

//            actOnStateCode();
    }

    void setVariables() {

        utils = new Utils(mContext);
        utils.log(logID, "setVariables started stateCode=" + sharedCode);
        utils.getPreference();
        if (!sharedCode.equals(logID))
            sharedCode = sharedPref.getString("sharedCode", "BLANK");
        sounds = new Sounds();
        quietTasks = utils.readQuietTasksFromShared();
        if (quietTasks.size() == 0)
            new ClearQuiteTasks();

        WindowMetrics windowMetrics = mActivity.getWindowManager().getCurrentWindowMetrics();
        Insets insets = windowMetrics.getWindowInsets()
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
        xSize = (windowMetrics.getBounds().width() - insets.left - insets.right) / 9;

        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (!notificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
        utils.deleteOldLogFiles();
    }

    void actOnStateCode() {

        switch (sharedCode) {

            case STATE_ONETIME:
                finish();
                break;

            case STATE_BOOT:
                new ScheduleNextTask("Next Alarm");
                break;

            case STATE_ALARM:
                new ScheduleNextTask("Next Alarm");
                finish();
                break;

            case STATE_BLANK:
                break;

            default:
                utils.log(logID,"Invalid statCode>"+ sharedCode);
                break;
        }
        if (!sharedCode.equals(STATE_ALARM) && !sharedCode.equals(STATE_BOOT))
            new ShowMainList();
        sharedCode = STATE_ALARM;
        sharedEditor.putString(SHARED_CODE, sharedCode);
        sharedEditor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        int menuItem = item.getItemId();
        if (menuItem == R.id.action_add) {
            addNewQuiet = true;
            intent = new Intent(MainActivity.this, AddUpdateActivity.class);
            intent.putExtra("idx", -1);
            startActivity(intent);
            return true;
        }
        else if (menuItem == R.id.action_calendar) {
            startActivity(new Intent(MainActivity.this, GCalShowActivity.class));
            return true;
        } else if (menuItem == R.id.action_setting) {
            startActivity(new Intent(this, PreferActivity.class));
            return true;
        } else if (menuItem == R.id.action_reset) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.reset_title)
                    .setMessage(R.string.reset_table)
                    .setIcon(R.drawable.danger)
                    .setPositiveButton(android.R.string.ok, (dialog, whichButton) -> {
                        new ClearQuiteTasks();
                        new ShowMainList();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new ScheduleNextTask("back ");
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

//    @Override
//    protected void onUserLeaveHint() {
//        new ScheduleNextTask("Hint ");
//        super.onUserLeaveHint();
//    }
}