package better.life.autoquiet.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import better.life.autoquiet.MyAccessibilityService;
import better.life.autoquiet.R;
import better.life.autoquiet.Sub.BtConnectedNames;
import better.life.autoquiet.Sub.ContextProvider;
import better.life.autoquiet.Sub.Sounds;
import better.life.autoquiet.Sub.VarsGetPut;
import better.life.autoquiet.ScheduleNextTask;
import better.life.autoquiet.models.NextTask;
import better.life.autoquiet.QuietTaskGetPut;
import better.life.autoquiet.Sub.MyItemTouchHelper;
import better.life.autoquiet.Sub.Permission;
import better.life.autoquiet.Utils;
import better.life.autoquiet.Vars;
import better.life.autoquiet.adapter.MainRecycleAdapter;
import better.life.autoquiet.models.QuietTask;

import java.util.ArrayList;
import java.util.List;

public class ActivityMain extends AppCompatActivity {

    public static Vars vars;
    public static MainRecycleAdapter mainRecycleAdapter;
    public static int currIdx = -1;
    public static Sounds sounds = null;
    public static final String ACTION_CLOCK = "c";
    public static final String ACTION_NORM = "n";

    public static ArrayList<QuietTask> quietTasks;
    public static ArrayList<NextTask> nextTasks;
    RecyclerView mainRecyclerView;
    Context context;
    private final String TAG = "MAIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        ContextProvider.init(context);
        VarsGetPut.get(this);

        new Utils().deleteOldLogFiles();
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
        if (!isAccessibilityServiceEnabled(this, MyAccessibilityService.class)) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
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
        VarsGetPut.put(vars, context);
        checkAndGetConnectedDevices();

    }

    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1234;

    @Override
    public void onResume() {

        super.onResume();
        if (sounds == null)
            sounds = new Sounds(this);
        VarsGetPut.get(context);
//        Log.w("Main", "onResume");
        new Utils().deleteOldLogFiles();

        setUpMainAdapter();
        VarsGetPut.put(vars, context);
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
            VarsGetPut.put(vars, context);
            intent = new Intent(this, ActivityAddEdit.class);
            intent.putExtra("idx", -1);
            startActivityForResult(intent, 11);

            return true;

        } else if (menuItem == R.id.action_calendar) {
            VarsGetPut.put(vars, context);
            intent = new Intent(this, ActivityGCalShow.class);
            startActivityForResult(intent, 22);
            return true;

        } else if (menuItem == R.id.action_sort) {
            mainRecycleAdapter.sort();
            return true;

        } else if (menuItem == R.id.action_setting) {
            VarsGetPut.put(vars, context);
            startActivityForResult(new Intent(this, ActivityPrefer.class), 33);
            return true;

        } else if (menuItem == R.id.action_reset) {
            VarsGetPut.put(vars, context);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.reset_title)
                    .setMessage(R.string.reset_table)
                    .setIcon(R.drawable.danger)
                    .setPositiveButton(android.R.string.ok, (dialog, whichButton) -> {
                        QuietTaskGetPut.init();
                        setUpMainAdapter();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void movePointer(float begX, float begY, float endX, float endY, long durationMs) {
        MyAccessibilityService service = MyAccessibilityService.instance;
        if (service != null) {
            service.movePointer(100f, 200f, 300f, 400f, 500, new Runnable() {
                @Override
                public void run() {
                    Log.d("MainActivity", "Pointer move completed");
                }
            });
        } else {
            Log.w("MainActivity", "AccessibilityService instance is null");
        }
    }

    private static void inputText(String text) {
        MyAccessibilityService service = MyAccessibilityService.instance;
        if (service != null) {
            service.inputText(text);
        } else {
            Log.w("MainActivity", "AccessibilityService instance is null");
        }
    }

    private static void getCurrPos() {
        MyAccessibilityService service = MyAccessibilityService.instance;
        if (service != null) {
            service.getCurrPos(new MyAccessibilityService.OnTouchPositionListener() {
                @Override
                public void onTouch(int x, int y) {
                    // Got the touch coordinates here
                    Log.d("MainActivity", "Touch at: " + x + ", " + y);
                    // You can do more here, like UI update or next action
                }
            });
        } else {
            Log.w("MainActivity", "AccessibilityService instance is null");
        }
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


    public static boolean isAccessibilityServiceEnabled(
            Context context,
            Class<? extends android.accessibilityservice.AccessibilityService> serviceClass) {

        ComponentName expectedComponentName = new ComponentName(context, serviceClass);
        String enabledServicesSetting = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );

        if (enabledServicesSetting == null) {
            return false;
        }

        Log.d("AccessibilityCheck", "Expected: " + expectedComponentName.flattenToString());
        Log.d("AccessibilityCheck", "Enabled list: " + enabledServicesSetting);

        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
        colonSplitter.setString(enabledServicesSetting);

        for (String enabledService : colonSplitter) {
            if (enabledService.equalsIgnoreCase(expectedComponentName.flattenToString())) {
                return true;
            }
        }
        return false;
    }

    private void checkAndGetConnectedDevices() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 (API 31) and higher
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "BLUETOOTH_CONNECT permission already granted.");
                // Permission is already granted, proceed to get devices
                getConnectedBluetoothDevices();
            } else {
                Log.d(TAG, "Requesting BLUETOOTH_CONNECT permission.");
                // Request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        BLUETOOTH_PERMISSION_REQUEST_CODE);
            }
        } else {
            // This part is for older Android versions
            Log.d(TAG, "Running on an older Android version. Assuming permissions are in manifest.");
            getConnectedBluetoothDevices();
        }
    }

    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 1001;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted by the user
                Log.d(TAG, "BLUETOOTH_CONNECT permission granted.");
                getConnectedBluetoothDevices();
            } else {
                // Permission denied by the user
                Log.d(TAG, "BLUETOOTH_CONNECT permission denied.");
                Toast.makeText(this, "Bluetooth permission is required to find connected devices.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getConnectedBluetoothDevices() {
        // You can now safely call your utility function
        List<String> deviceNames = BtConnectedNames.getConnectedBluetoothDeviceNames(this);
        if (deviceNames.isEmpty()) {
            Log.w(TAG, "No Bluetooth devices are currently connected.");
            Toast.makeText(this, "No connected Bluetooth devices found.", Toast.LENGTH_SHORT).show();
        } else {
            Log.w(TAG + deviceNames.size(), "Connected devices: " + deviceNames.toString());
            Toast.makeText(this, "Connected devices: " + deviceNames.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        ScheduleNextTask.request("main");
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