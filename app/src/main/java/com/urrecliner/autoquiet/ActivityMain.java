package com.urrecliner.autoquiet;

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

import com.urrecliner.autoquiet.Sub.ClearAllTasks;
import com.urrecliner.autoquiet.Sub.MyItemTouchHelper;
import com.urrecliner.autoquiet.Sub.Permission;
import com.urrecliner.autoquiet.Sub.SharedPrefer;
import com.urrecliner.autoquiet.Sub.VarsGetPut;

import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityMain extends AppCompatActivity  {

    public static Context pContext;
    public static Activity pActivity;
    public static Vars vars;
    public static MainRecycleAdapter mainRecycleAdapter;
    int count;
    public static int currIdx;

    Timer timer = null;
    TimerTask timerTask = null;
    long timeSaved;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pContext = this;
        pActivity = this;
        vars = new Vars();
        new SharedPrefer().get(vars);

        new Utils(pContext).deleteOldLogFiles();
        setContentView(R.layout.activity_main);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getApplicationContext()
                    .getPackageName(), PackageManager.GET_PERMISSIONS);
            Permission.ask(this, this, info);
        } catch (Exception e) {
            Log.e("Permission", "No Permission "+e);
        }

// If you have access to the external storage, do whatever you need
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()){
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Log.w("Permission","Required for READ_CALENDAR");
        }
        Log.w("autoQuiet","onCreated ----- ");
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (!notificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }

        new VarsGetPut().put(vars, pContext);

        new NotificationStart(pContext);

    }

    @Override
    public void onResume() {

        vars = new VarsGetPut().get(pContext);
        Log.w("Main", "onResume is called");
        new Utils(pContext).deleteOldLogFiles();
        showMainList();
//        count = 0;
//
//        timeSaved = System.currentTimeMillis();
//        if (timer != null)
//            timer.cancel();
//        if (timerTask != null)
//            timerTask.cancel();
//        timer = new Timer();
//        timerTask = new TimerTask() {
//            @Override
//            public void run () {
//                Log.w("Main", count++ + ") timeGap = "+((System.currentTimeMillis() - timeSaved)/1000)+" secs");
//                timeSaved = System.currentTimeMillis();
//            }
//        };
//        timer.schedule(timerTask, 27*60000, 27*60000);
        super.onResume();
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
            new VarsGetPut().put(vars, pContext);
            intent = new Intent(this, ActivityAddEdit.class);
            intent.putExtra("idx", -1);
            startActivityForResult(intent, 11);
            return true;

        } else if (menuItem == R.id.action_calendar) {
            new VarsGetPut().put(vars, pContext);
            intent = new Intent(this, ActivityGCalShow.class);
            startActivityForResult(intent, 22);
            return true;

        } else if (menuItem == R.id.action_sort) {
            mainRecycleAdapter.sort();
            mainRecycleAdapter.notifyDataSetChanged();
            return true;

        } else if (menuItem == R.id.action_setting) {
            new VarsGetPut().put(vars, pContext);
            startActivityForResult(new Intent(this, ActivityPrefer.class),33);
            return true;

        } else if (menuItem == R.id.action_reset) {
            new VarsGetPut().put(vars, pContext);
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

        RecyclerView mainRecyclerView = findViewById(R.id.mainRecycler);

        mainRecycleAdapter = new MainRecycleAdapter();
        ItemTouchHelper.Callback mainCallback = new MyItemTouchHelper(mainRecycleAdapter, pContext);
        ItemTouchHelper mainItemTouchHelper = new ItemTouchHelper(mainCallback);
        mainRecycleAdapter.setTouchHelper(mainItemTouchHelper);
        new VarsGetPut().put(vars, pContext);
        mainItemTouchHelper.attachToRecyclerView(mainRecyclerView);
        mainRecyclerView.setAdapter(mainRecycleAdapter);
        mainRecyclerView.setLayoutManager(new LinearLayoutManager( this));
    }

//    @Override
//    public void onBackPressed() {
//        new SetUpComingTask(pContext, new QuietTaskGetPut().get(pContext),"next is ");
//        super.onBackPressed();
//    }

    @Override
    protected void onStop() {
        new SetUpComingTask(pContext, new QuietTaskGetPut().get(pContext),"onStop ");
        super.onStop();
    }
}