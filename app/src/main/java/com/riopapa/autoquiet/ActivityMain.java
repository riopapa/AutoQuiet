package com.riopapa.autoquiet;

import static com.riopapa.autoquiet.AlarmReceiver.showNotification;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.riopapa.autoquiet.Sub.ClearAllTasks;
import com.riopapa.autoquiet.Sub.MyItemTouchHelper;
import com.riopapa.autoquiet.Sub.Permission;
import com.riopapa.autoquiet.Sub.SharedPrefer;
import com.riopapa.autoquiet.Sub.ShowNotification;
import com.riopapa.autoquiet.Sub.VarsGetPut;
import com.riopapa.autoquiet.models.QuietTask;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityMain extends AppCompatActivity {

    public static Context mContext;
    public static Activity pActivity;
    public static Vars vars;
    public static MainRecycleAdapter mainRecycleAdapter;
    public static int currIdx = -1;
    RecyclerView mainRecyclerView;

    public static ArrayList<QuietTask> quietTasks;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
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

        NotificationService notificationService = new NotificationService();
        Intent intent = new Intent(mContext, notificationService.getClass());
        showNotification = new ShowNotification();
        showNotification.show(mContext, intent);
    }

    @Override
    public void onResume() {

        vars = new VarsGetPut().get(mContext);
        Log.w("Main", "onResume");
        new Utils(mContext).deleteOldLogFiles();

        showMainList();
        new VarsGetPut().put(vars, mContext);
        if (currIdx == -1)
            currIdx = mainRecycleAdapter.getItemCount() / 2;
        mainRecyclerView.scrollToPosition((currIdx > 2) ? currIdx - 1 : currIdx);
        super.onResume();

        waitLoop(); // not to be killed

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
                        new ClearAllTasks(getApplicationContext());
                        showMainList();
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

    private void showMainList() {
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

    Timer timer = new Timer();
    TimerTask timerTask = null;
    static long count = 0;
    static long lastTime;
    void waitLoop() {

        final long LOOP_INTERVAL = 20 * 60 * 1000;

        if (timerTask != null)
            timerTask.cancel();
        if (timer != null)
            timer.cancel();

        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run () {
                Log.w("waiting..", count++ +", "+ (System.currentTimeMillis()-lastTime));
                lastTime = System.currentTimeMillis();
            }
        };
        timer.schedule(timerTask, 1000, LOOP_INTERVAL);
    }

    @Override
    protected void onPause() {
        mainRecycleAdapter.sort();
        new ScheduleNextTask(mContext, "onPause");
        super.onPause();
    }
}