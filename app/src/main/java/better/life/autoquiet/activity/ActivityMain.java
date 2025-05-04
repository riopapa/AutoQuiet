package better.life.autoquiet.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import better.life.autoquiet.NotificationService;
import better.life.autoquiet.R;
import better.life.autoquiet.ScheduleNextTask;
import better.life.autoquiet.common.FloatingClockService;
import better.life.autoquiet.models.NextTask;
import better.life.autoquiet.quiettask.QuietTaskNew;
import better.life.autoquiet.Sub.MyItemTouchHelper;
import better.life.autoquiet.common.Permission;
import better.life.autoquiet.common.SharedPrefer;
import better.life.autoquiet.Sub.ShowNotification;
import better.life.autoquiet.Sub.VarsGetPut;
import better.life.autoquiet.Utils;
import better.life.autoquiet.Vars;
import better.life.autoquiet.adapter.MainRecycleAdapter;
import better.life.autoquiet.models.QuietTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ActivityMain extends AppCompatActivity {

    public static Context mContext;
    public static Activity pActivity;
    public static Vars vars;
    public static MainRecycleAdapter mainRecycleAdapter;
    public static int currIdx = -1;
    public static long nextAlertTime;
    RecyclerView mainRecyclerView;

    public static final String ACTION_CLOCK = "c";
    public static final String ACTION_NORM = "n";
    public final static String WIDGET_CALL = "widget";

    public static ArrayList<QuietTask> quietTasks;
    public static ArrayList<NextTask> nextTasks;

    String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        pActivity = this;
        vars = new Vars();
        new SharedPrefer().get(vars);

        new Utils(mContext).deleteOldLogFiles();
        setContentView(R.layout.activity_main);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getApplicationContext()
                    .getPackageName(), PackageManager.GET_PERMISSIONS);
            Permission.ask(this, this, info);
        } catch (Exception e) {
            Log.e("Permission", "No Permission " + e);
        }

// If you have access to the external storage, do whatever you need
        if (!Environment.isExternalStorageManager()) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", this.getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Log.w("Permission", "Required for READ_CALENDAR");
        }
//        Log.w("autoQuiet","onCreated ----- ");
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (!notificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }

        new VarsGetPut().put(vars, mContext);

//        NotificationService notificationService = new NotificationService();
//        Intent intent = new Intent(mContext, notificationService.getClass());
//        new ShowNotification().show(mContext, intent);

//        nextTasks = new ArrayList<>();
//        NextTask nt = new NextTask();
//        nt.subject = "Init";
//        nt.hour = 12; nt.min = 34;
//        nextTasks.add(nt);F.

    }

    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1234;

    @Override
    public void onResume() {

        super.onResume();
        vars = new VarsGetPut().get(mContext);
        Log.w("Main", "onResume");
        new Utils(mContext).deleteOldLogFiles();

        setUpMainAdapter();
        new VarsGetPut().put(vars, mContext);
        if (currIdx == -1)
            currIdx = mainRecycleAdapter.getItemCount() / 4;
        mainRecyclerView.scrollToPosition((currIdx > 2) ? currIdx - 1 : currIdx);
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
//            if (Settings.canDrawOverlays(this)) {
//                // Permission granted, proceed to show the overlay
////                showFloatingClock();
//            } else {
//                // Permission denied, handle accordingly (e.g., show a message)
//            }
//        }
//    }

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
            vars.addNewQuiet = true;
            new VarsGetPut().put(vars, mContext);
            intent = new Intent(this, ActivityAddEdit.class);
            intent.putExtra("idx", -1);
            startActivityForResult(intent, 11);
            return true;

        } else if (menuItem == R.id.action_calendar) {
            new VarsGetPut().put(vars, mContext);
            intent = new Intent(this, ActivityGCalShow.class);
            startActivityForResult(intent, 22);
            return true;

        } else if (menuItem == R.id.action_sort) {
            mainRecycleAdapter.sort();
            return true;

        } else if (menuItem == R.id.action_setting) {
            new VarsGetPut().put(vars, mContext);
            startActivityForResult(new Intent(this, ActivityPrefer.class), 33);
            return true;

        } else if (menuItem == R.id.action_reset) {
            new VarsGetPut().put(vars, mContext);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.reset_title)
                    .setMessage(R.string.reset_table)
                    .setIcon(R.drawable.danger)
                    .setPositiveButton(android.R.string.ok, (dialog, whichButton) -> {
                        new QuietTaskNew(getApplicationContext());
                        setUpMainAdapter();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mainRecycleAdapter.notifyDataSetChanged();
    }

    public void setUpMainAdapter() {
//
        mainRecyclerView = findViewById(R.id.mainRecycler);

        mainRecycleAdapter = new MainRecycleAdapter();
        ItemTouchHelper.Callback mainCallback = new MyItemTouchHelper(mainRecycleAdapter);
        ItemTouchHelper mainItemTouchHelper = new ItemTouchHelper(mainCallback);
        mainRecycleAdapter.setTouchHelper(mainItemTouchHelper);
        mainItemTouchHelper.attachToRecyclerView(mainRecyclerView);
        mainRecyclerView.setAdapter(mainRecycleAdapter);
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    long nextDelayInterval;
    SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm ", Locale.getDefault());

    void reLoad_Again() {
        nextDelayInterval = 180 * 60 * 1000;
        long nowTime = System.currentTimeMillis();
        long nextTime = nowTime + nextDelayInterval;
        if (nextTime > nextAlertTime - 5 * 60 * 1000 &&
                nextTime < nextAlertTime + 5 * 60 * 1000 ) {
            nextDelayInterval = nextAlertTime - 5 * 60 * 1000 - nowTime;
            while (nextDelayInterval < 0)
                nextDelayInterval += 3 * 60 * 1000;
        }
        Log.e("reload", "Next Reload Time is "+sdf.format(nowTime+nextDelayInterval));
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (nextTime < nextAlertTime - 5 * 60 * 1000 ||
                        nextTime > nextAlertTime + 5 * 60 * 1000 ) {
                    Intent intent = new Intent(ActivityMain.this, ActivityMain.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        }, nextDelayInterval);
    }
//
//    void waitLoop() {
//
//        final long LOOP_INTERVAL = 10 * 60 * 1000;
//
//        if (timerTask != null)
//            timerTask.cancel();
//        if (timer != null)
//            timer.cancel();
//
//        timer = new Timer();
//        timerTask = new TimerTask() {
//            @Override
//            public void run () {
//                Log.w("waiting..", count++ +", "+ (System.currentTimeMillis()-lastTime));
//                lastTime = System.currentTimeMillis();
//            }
//        };
//        timer.schedule(timerTask, 1000, LOOP_INTERVAL);
//    }

    @Override
    protected void onPause() {
//        mainRecycleAdapter.sort("");
        new ScheduleNextTask(mContext, "main");
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mainRecyclerView != null)
            mainRecyclerView = null;
    }
}