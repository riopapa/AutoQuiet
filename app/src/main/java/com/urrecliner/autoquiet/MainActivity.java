package com.urrecliner.autoquiet;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.urrecliner.autoquiet.utility.ClearQuiteTasks;
import com.urrecliner.autoquiet.utility.Permission;
// import com.weijiaxing.logviewer.LogcatActivity;

import static com.urrecliner.autoquiet.Vars.STATE_ADD_UPDATE;
import static com.urrecliner.autoquiet.Vars.STATE_ALARM;
import static com.urrecliner.autoquiet.Vars.STATE_BLANK;
import static com.urrecliner.autoquiet.Vars.STATE_BOOT;
import static com.urrecliner.autoquiet.Vars.STATE_ONETIME;
import static com.urrecliner.autoquiet.Vars.actionHandler;
import static com.urrecliner.autoquiet.Vars.addNewQuiet;
import static com.urrecliner.autoquiet.Vars.mActivity;
import static com.urrecliner.autoquiet.Vars.mContext;
import static com.urrecliner.autoquiet.Vars.mainRecycleViewAdapter;
import static com.urrecliner.autoquiet.Vars.quietTasks;
import static com.urrecliner.autoquiet.Vars.stateCode;
import static com.urrecliner.autoquiet.Vars.utils;
import static com.urrecliner.autoquiet.Vars.xSize;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity  {

    private static final String logID = "Main";
    private static int cnt = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        mContext = this.getApplicationContext();
        utils = new Utils();
        utils.log(logID, "Main start ");
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getApplicationContext()
                    .getPackageName(), PackageManager.GET_PERMISSIONS);
            Permission.ask(this, this, info);
        } catch (Exception e) {
            Log.e("Permission", "No Permission "+e.toString());
        }

        setContentView(R.layout.activity_main);
//        LogcatActivity.launch(MainActivity.this);

        Intent intent = getIntent();
        if (intent == null) {
            stateCode = "NULL";
        } else {
            stateCode = intent.getStringExtra("stateCode");
            if (stateCode == null)
                stateCode = STATE_BLANK;
        }
        utils.log(logID, stateCode);
        utils.deleteOldLogFiles();
        if (!stateCode.equals(STATE_BLANK))
            return;
        setVariables();
        actOnStateCode();
        actionHandler = new Handler(Looper.getMainLooper()) { public void handleMessage(Message msg) { actOnStateCode(); }};
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Log.w("Permission","Required for READ_CALENDAR");
        }
        new Timer().schedule(new TimerTask() {
            public void run () {
                Log.w("cnt","= "+cnt++);
            }
        }, 50, 60 * 60000);
    }

    void setVariables() {

        utils.getPreference();
        quietTasks = utils.readQuietTasksFromShared();
        if (quietTasks.size() == 0)
            new ClearQuiteTasks();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        xSize = size.x / 9;    //  (7 week + 2)

        // get permission for silent mode
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (!notificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
        utils.beepsInitiate();
        utils.beepOnce(0); utils.beepOnce(1);
    }

    void actOnStateCode() {

        if (!stateCode.equals(STATE_BLANK))
            utils.log(logID, "State=" + stateCode);
        switch (stateCode) {
            case STATE_ONETIME:
                stateCode = "@" + stateCode;
                finish();
                break;

            case STATE_ALARM:
                stateCode = "@" + stateCode;
                new ScheduleNextTask("Next Alarm Settled ");
                finish();
                break;

            case STATE_BOOT:  // it means from receiver
                stateCode = "@" + stateCode;
                new ScheduleNextTask("Boot triggered new Alarm ");
                finish();
                break;

            case STATE_ADD_UPDATE:
                stateCode = "@" + stateCode;
                break;
            case STATE_BLANK:
                break;
            default:
                utils.log(logID,"Invalid statCode>"+stateCode);
                break;
        }
        new ShowMainList();
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
                    .setIcon(R.mipmap.alert)
                    .setPositiveButton(android.R.string.ok, (dialog, whichButton) -> {
                        new ClearQuiteTasks();
                        new ShowMainList();
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        mainRecycleViewAdapter.notifyDataSetChanged();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onUserLeaveHint() {
        new ScheduleNextTask("Start setting Silent Time ");
        super.onUserLeaveHint();
    }
}