package com.urrecliner.letmequiet;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;

import com.urrecliner.letmequiet.databinding.ActivityMainBinding;
import com.urrecliner.letmequiet.models.QuietTask;
import com.urrecliner.letmequiet.utility.CalculateNext;
import com.urrecliner.letmequiet.utility.MyItemTouchHelper;
import com.urrecliner.letmequiet.utility.NextAlarm;
import com.urrecliner.letmequiet.utility.VerticalSpacingItemDecorator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Display;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import static com.urrecliner.letmequiet.Vars.STATE_ADD_UPDATE;
import static com.urrecliner.letmequiet.Vars.STATE_ALARM;
import static com.urrecliner.letmequiet.Vars.STATE_BLANK;
import static com.urrecliner.letmequiet.Vars.STATE_BOOT;
import static com.urrecliner.letmequiet.Vars.STATE_ONETIME;
import static com.urrecliner.letmequiet.Vars.actionHandler;
import static com.urrecliner.letmequiet.Vars.addNewQuiet;
import static com.urrecliner.letmequiet.Vars.beepManner;
import static com.urrecliner.letmequiet.Vars.default_Duration;
import static com.urrecliner.letmequiet.Vars.interval_Long;
import static com.urrecliner.letmequiet.Vars.interval_Short;
import static com.urrecliner.letmequiet.Vars.mActivity;
import static com.urrecliner.letmequiet.Vars.mContext;
import static com.urrecliner.letmequiet.Vars.recycleViewAdapter;
import static com.urrecliner.letmequiet.Vars.sdfDateTime;
import static com.urrecliner.letmequiet.Vars.sdfTime;
import static com.urrecliner.letmequiet.Vars.sharedPreferences;
import static com.urrecliner.letmequiet.Vars.quietTask;
import static com.urrecliner.letmequiet.Vars.quietTasks;
import static com.urrecliner.letmequiet.Vars.stateCode;
import static com.urrecliner.letmequiet.Vars.utils;
import static com.urrecliner.letmequiet.Vars.weekName;
import static com.urrecliner.letmequiet.Vars.xSize;

public class MainActivity extends AppCompatActivity  {

    private static String logID = "Main";
    private ActivityMainBinding binding;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        mContext = this.getApplicationContext();
        if (utils == null)
            utils = new Utils();
        utils.log(logID, "Main start ");
        askPermission();

        setContentView(R.layout.activity_main);

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
        new Timer().schedule(new TimerTask() {
            public void run () {
                updateNotificationBar("xx:xx","not activated yet","S");
            }
        }, 100);
        actionHandler = new Handler() { public void handleMessage(Message msg) { actOnStateCode(); }};

    }

    void updateNotificationBar(String dateTime, String subject, String startFinish) {
        Intent updateIntent = new Intent(MainActivity.this, NotificationService.class);
        updateIntent.putExtra("isUpdate", true);
        updateIntent.putExtra("dateTime", dateTime);
        updateIntent.putExtra("subject", subject);
        updateIntent.putExtra("startFinish", startFinish);
        startService(updateIntent);
    }

    void setVariables() {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        beepManner = sharedPreferences.getBoolean("beepManner", true);
        interval_Short = sharedPreferences.getInt("interval_Short", 5);
        interval_Long = sharedPreferences.getInt("interval_Long", 30);
        default_Duration = sharedPreferences.getInt("default_Duration", 60);

        quietTasks = utils.readSharedPrefTables();
        if (quietTasks.size() == 0)
            initiate_QuietTasks();

        weekName[0] = getResources().getString(R.string.week_0);    weekName[1] = getResources().getString(R.string.week_1);    weekName[2] = getResources().getString(R.string.week_2);    weekName[3] = getResources().getString(R.string.week_3);
        weekName[4] = getResources().getString(R.string.week_4);    weekName[5] = getResources().getString(R.string.week_5);    weekName[6] = getResources().getString(R.string.week_6);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        xSize = size.x / 9;    //  (7 week + 2)

        // get permission for silent mode
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (!notificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(
                    android.provider.Settings
                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
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
                scheduleNextTask("Next Alarm Settled ");
                finish();
                break;

            case STATE_BOOT:  // it means from receiver
                stateCode = "@" + stateCode;
                scheduleNextTask("Boot triggered new Alarm ");
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
        showArrayLists();
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
        switch (item.getItemId()) {
            case R.id.action_add:
                addNewQuiet = true;
                intent = new Intent(MainActivity.this, AddUpdateActivity.class);
                intent.putExtra("idx",-1);
                startActivity(intent);
                return true;
            case R.id.action_setting:
                intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_reset:
                new AlertDialog.Builder(this)
                        .setTitle("데이터 초기화")
                        .setMessage("이미 설정되어 있는 테이블을 다 삭제합니다")
                        .setIcon(R.mipmap.alert)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                initiate_QuietTasks();
                                showArrayLists();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showArrayLists() {

        recyclerView = findViewById(R.id.mainRecycler);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLinearLayoutManager);

        VerticalSpacingItemDecorator itemDecorator = new VerticalSpacingItemDecorator(14);
        recyclerView.addItemDecoration(itemDecorator);

        recycleViewAdapter = new RecycleViewAdapter();
        ItemTouchHelper.Callback callback = new MyItemTouchHelper(recycleViewAdapter, mContext);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        recycleViewAdapter.setTouchHelper(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(recycleViewAdapter);
    }

    void scheduleNextTask(String headInfo) {
        long nextTime = System.currentTimeMillis() + (long)240*60*60*1000;
        int saveIdx = 0;
        String StartFinish = "S";
        boolean[] week;
        for (int idx = 0; idx < quietTasks.size(); idx++) {
            QuietTask quietTask1 = quietTasks.get(idx);
            if (quietTask1.isActive()) {
                week = quietTask1.getWeek();
                long nextStart = CalculateNext.calc(false, quietTask1.getStartHour(), quietTask1.getStartMin(), week, 0);
                if (nextStart < nextTime) {
                    nextTime = nextStart;
                    saveIdx = idx;
                    StartFinish = "S";
                }

                long nextFinish = CalculateNext.calc(true, quietTask1.getFinishHour(), quietTask1.getFinishMin(), week, (quietTask1.getStartHour()> quietTask1.getFinishHour()) ? (long)24*60*60*1000 : 0);
                if (nextFinish < nextTime) {
                    nextTime = nextFinish;
                    saveIdx = idx;
                    StartFinish = (idx == 0) ? "O":"F";
                }
            }
        }
        quietTask = quietTasks.get(saveIdx);
        NextAlarm.request(quietTask, nextTime, StartFinish, getApplicationContext());
        String msg = headInfo + "\n" + quietTask.getSubject() + "\n" + sdfDateTime.format(nextTime) + " " + StartFinish;
//        utils.log(logID, msg);
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        utils.log(logID,sdfDateTime.format(nextTime) + " " + StartFinish + " " + quietTask.getSubject());
        updateNotificationBar (sdfTime.format(nextTime), quietTask.getSubject(), StartFinish);
        if (stateCode.equals("@back") && StartFinish.equals("F")) {
            MannerMode.turnOn(getApplicationContext(), quietTask.getSubject(), quietTask.isVibrate());
        }
    }

    private void initiate_QuietTasks() {
        quietTasks.clear();
        quietTask = getQuietTaskOneTime();
        quietTasks.add(quietTask);
        quietTask = getQuietTaskDefault();
        quietTasks.add(quietTask);
        boolean [] week = new boolean[]{true, false, false, false, false, false, false};
        quietTask = new QuietTask(getString(R.string.Sunday_Church), 9, 30, 16, 30,
                week, true, true, true);
        quietTasks.add(quietTask);
        week = new boolean[]{true, false, false, false, false, false, true};
        quietTask = new QuietTask(getString(R.string.WeekEnd_Night), 22, 30, 10, 30,
                week, true, false, true);
        quietTasks.add(quietTask);
        utils.saveSharedPrefTables();
    }

    QuietTask getQuietTaskDefault() {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, 10);
        int hStart = calendar.get(Calendar.HOUR_OF_DAY);
        int mStart = calendar.get(Calendar.MINUTE);
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        int hFinish = calendar.get(Calendar.HOUR_OF_DAY);
        boolean [] week = new boolean[]{false, true, true, true, true, true, false};
        return new QuietTask(getString(R.string.action_add), hStart, mStart, hFinish, mStart,
                week, true, true, true);
    }

    QuietTask getQuietTaskOneTime() {

        boolean [] week = new boolean[]{false, false, false, false, false, false, false};
        return new QuietTask(getString(R.string.Quiet_Once), 1,2,3,4,
                week, false, true, false);
    }


//    @Override
//    protected void onResume() {
//        super.onResume();
//        utils.log(logID, "RESUME " + stateCode);
//        setVariables();
//        actOnStateCode();
//    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        stateCode = "@back";
        scheduleNextTask("Activate Silent Time ");
        super.onBackPressed();
    }

    // ↓ ↓ ↓ P E R M I S S I O N    RELATED /////// ↓ ↓ ↓ ↓
    ArrayList<String> permissions = new ArrayList<>();
    private final static int ALL_PERMISSIONS_RESULT = 101;
    ArrayList<String> permissionsToRequest;
    ArrayList<String> permissionsRejected = new ArrayList<>();

    private void askPermission() {
        permissions.add(Manifest.permission.READ_PHONE_STATE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.VIBRATE);
        permissions.add(Manifest.permission.ACCESS_NOTIFICATION_POLICY);
        permissions.add(Manifest.permission.RECEIVE_BOOT_COMPLETED);
        permissionsToRequest = findUnAskedPermissions(permissions);
        if (permissionsToRequest.size() != 0) {
            requestPermissions(permissionsToRequest.toArray(new String[0]),
//            requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                    ALL_PERMISSIONS_RESULT);
        }
        // get permission for silent mode
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !notificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(
                    android.provider.Settings
                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    private ArrayList findUnAskedPermissions(@NonNull ArrayList<String> wanted) {
        ArrayList <String> result = new ArrayList<String>();
        for (String perm : wanted) if (hasPermission(perm)) result.add(perm);
        return result;
    }
    private boolean hasPermission(@NonNull String permission) {
        return (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ALL_PERMISSIONS_RESULT) {
            for (String perms : permissionsToRequest) {
                if (hasPermission(perms)) {
                    permissionsRejected.add(perms);
                }
            }
            if (permissionsRejected.size() > 0) {
                if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                    String msg = "These permissions are mandatory for the application. Please allow access.";
                    showDialog(msg);
                }
            }
            else
                Toast.makeText(mContext, "Permissions not granted.", Toast.LENGTH_LONG).show();
        }
    }
    private void showDialog(String msg) {
        showMessageOKCancel(msg,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(permissionsRejected.toArray(
                                new String[0]), ALL_PERMISSIONS_RESULT);
                    }
                });
    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(mActivity)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


// ↑ ↑ ↑ ↑ P E R M I S S I O N    RELATED /////// ↑ ↑ ↑

}