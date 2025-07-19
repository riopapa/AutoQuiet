package better.life.autoquiet.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
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

import better.life.autoquiet.R;
import better.life.autoquiet.Sub.ContextProvider;
import better.life.autoquiet.Sub.PhoneVibrate;
import better.life.autoquiet.Sub.Sounds;
import better.life.autoquiet.nexttasks.ScheduleNextTask;
import better.life.autoquiet.models.NextTask;
import better.life.autoquiet.quiettask.QuietTaskNew;
import better.life.autoquiet.Sub.MyItemTouchHelper;
import better.life.autoquiet.Sub.Permission;
import better.life.autoquiet.Sub.SharedPrefer;
import better.life.autoquiet.Sub.VarsGetPut;
import better.life.autoquiet.Utility;
import better.life.autoquiet.Vars;
import better.life.autoquiet.adapter.MainRecycleAdapter;
import better.life.autoquiet.models.QuietTask;

import java.util.ArrayList;

public class ActivityMain extends AppCompatActivity {

    public static Vars vars;
    public static MainRecycleAdapter mainRecycleAdapter;
    public static int currIdx = -1;
    public static Sounds sounds = null;
    public static PhoneVibrate phoneVibrate = null;
    public static final String ACTION_CLOCK = "c";
    public static final String ACTION_NORM = "n";

    public static ArrayList<QuietTask> quietTasks;
    public static ArrayList<NextTask> nextTasks;

    RecyclerView mainRecyclerView;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        ContextProvider.init(context);
        vars = new Vars();
        new SharedPrefer().get(vars);

        new Utility().deleteOldLogFiles();
        setContentView(R.layout.activity_main);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getApplicationContext()
                    .getPackageName(), PackageManager.GET_PERMISSIONS);
            Permission.ask(this, this, info);
        } catch (Exception e) {
            Log.e("Permission", "No Permission " + e);
        }

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

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (!alarmManager.canScheduleExactAlarms()) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
        }

        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (!notificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
        ContextProvider.init(this);
        phoneVibrate = new PhoneVibrate();
        new VarsGetPut().put(vars, context);
    }

    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1234;

    @Override
    public void onResume() {

        super.onResume();
        if (sounds == null)
            sounds = new Sounds(this);
        phoneVibrate = new PhoneVibrate();
        vars = new VarsGetPut().get(context);
//        Log.w("Main", "onResume");
        new Utility().deleteOldLogFiles();

        setUpMainAdapter();
        new VarsGetPut().put(vars, context);
        if (currIdx == -1)
            currIdx = mainRecycleAdapter.getItemCount() / 4;
        mainRecyclerView.scrollToPosition((currIdx > 2) ? currIdx - 1 : currIdx);
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
        }
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
            new VarsGetPut().put(vars, context);
            intent = new Intent(this, ActivityAddEdit.class);
            intent.putExtra("idx", -1);
            startActivityForResult(intent, 11);
            return true;

        } else if (menuItem == R.id.action_calendar) {
            new VarsGetPut().put(vars, context);
            intent = new Intent(this, ActivityGCalShow.class);
            startActivityForResult(intent, 22);
            return true;

        } else if (menuItem == R.id.action_sort) {
            mainRecycleAdapter.sort();
            return true;

        } else if (menuItem == R.id.action_setting) {
            new VarsGetPut().put(vars, context);
            startActivityForResult(new Intent(this, ActivityPrefer.class), 33);
            return true;

        } else if (menuItem == R.id.action_reset) {
            new VarsGetPut().put(vars, context);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.reset_title)
                    .setMessage(R.string.reset_table)
                    .setIcon(R.drawable.danger)
                    .setPositiveButton(android.R.string.ok, (dialog, whichButton) -> {
                        new QuietTaskNew();
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

        mainRecycleAdapter = new MainRecycleAdapter(this);
        ItemTouchHelper.Callback mainCallback = new MyItemTouchHelper(mainRecycleAdapter);
        ItemTouchHelper mainItemTouchHelper = new ItemTouchHelper(mainCallback);
        mainRecycleAdapter.setTouchHelper(mainItemTouchHelper);
        mainItemTouchHelper.attachToRecyclerView(mainRecyclerView);
        mainRecyclerView.setAdapter(mainRecycleAdapter);
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onPause() {
        new ScheduleNextTask("main");
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mainRecyclerView != null)
            mainRecyclerView = null;
    }
    // In your Activity or Service that uses Sounds
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}