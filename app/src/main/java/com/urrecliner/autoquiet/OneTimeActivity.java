package com.urrecliner.autoquiet;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.urrecliner.autoquiet.databinding.ActivityOneTimeBinding;
import com.urrecliner.autoquiet.models.QuietTask;

import java.util.Calendar;

import static com.urrecliner.autoquiet.Vars.STATE_ONETIME;
import static com.urrecliner.autoquiet.Vars.mActivity;
import static com.urrecliner.autoquiet.Vars.quietTasks;
import static com.urrecliner.autoquiet.Vars.mainRecycleViewAdapter;
import static com.urrecliner.autoquiet.Vars.sharedTimeInit;
import static com.urrecliner.autoquiet.Vars.sharedTimeLong;
import static com.urrecliner.autoquiet.Vars.sharedTimeShort;
import static com.urrecliner.autoquiet.Vars.stateCode;
import static com.urrecliner.autoquiet.Vars.utils;

public class OneTimeActivity extends AppCompatActivity {

    QuietTask quietTask;
    private String subject;
    private int startHour, startMin, finishHour, finishMin;
    private boolean vibrate;
    private int durationMin = 0;       // in minutes
    Calendar calendar;
    boolean timePicker_UpDown = false;
    ActivityOneTimeBinding binding;
    int shortInterval, longInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent closeIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        getApplicationContext().sendBroadcast(closeIntent);
        super.onCreate(savedInstanceState);
        binding = ActivityOneTimeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ActionBar actionBar;
        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(getResources().getString(R.string.Quiet_Once));
        actionBar.setIcon(R.mipmap.quiet_right_now) ;
        actionBar.setDisplayUseLogoEnabled(true) ;
        actionBar.setDisplayShowHomeEnabled(true) ;
//        Bundle data = getIntent().getExtras();
//        assert data != null;
//        silentInfo = (SilentInfo) data.getSerializable("silentInfo");
        quietTask = quietTasks.get(0);
        subject = quietTask.getSubject();
        vibrate = quietTask.isVibrate();
        durationMin = Integer.parseInt(sharedTimeInit);
        shortInterval = Integer.parseInt(sharedTimeShort);
        longInterval = Integer.parseInt(sharedTimeLong);
        calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND,0);
        startHour = calendar.get(Calendar.HOUR_OF_DAY);
        startMin = calendar.get(Calendar.MINUTE);
        calendar.add(Calendar.MINUTE, durationMin);
        finishHour = calendar.get(Calendar.HOUR_OF_DAY);
        finishMin = calendar.get(Calendar.MINUTE);
        binding.oneTimePicker.setIs24HourView(true);
        binding.oneTimePicker.setOnTimeChangedListener((timePicker, hour, min) -> {
            if (timePicker_UpDown)
                return;
            finishHour = hour; finishMin = min;
            durationMin = (finishHour - startHour) * 60 + finishMin - startMin;
            binding.oneDuration.setText(durationText());
        });
        buildScreen();
        buttonSetting();
        adjustTimePicker();
    }

    private String durationText() {
        String text;
        if (durationMin > 1)
            text = " "+(""+(100 + durationMin / 60)).substring(1) + "시간 \n" + (""+(100 + durationMin % 60)).substring(1)+  "분후";
        else
            text = getString(R.string.already_passed_time);
        return text;
    }

    void buildScreen() {
        String text;
        text = "▼"+sharedTimeShort+"분▼"; binding.minus10Min.setText(text);
        text = "▲"+sharedTimeShort+"분▲"; binding.plus10Min.setText(text);
        text = "▼"+sharedTimeLong+"분▼"; binding.minus30Min.setText(text);
        text = "▲"+sharedTimeLong+"분▲"; binding.plus30Min.setText(text);
    }
    void buttonSetting() {
        binding.oneVibrate.setImageResource((vibrate)? R.mipmap.phone_vibrate :R.mipmap.phone_quiet);
        binding.oneVibrate.setOnClickListener(v -> {
            vibrate ^= true;
            binding.oneVibrate.setImageResource((vibrate)? R.mipmap.phone_vibrate :R.mipmap.phone_quiet);
            v.invalidate();
        });
        binding.minus10Min.setOnClickListener(v -> {
            if (durationMin > shortInterval) {
                durationMin -= shortInterval;
                adjustTimePicker();
            }
        });

        binding.plus10Min.setOnClickListener(v -> {
            durationMin += shortInterval;
            adjustTimePicker();
        });

        binding.minus30Min.setOnClickListener(v -> {
            if (durationMin > longInterval) {
                durationMin -= longInterval;
                adjustTimePicker();
            }
        });

        binding.plus30Min.setOnClickListener(v -> {
            durationMin += longInterval;
            adjustTimePicker();
        });
    }

    void adjustTimePicker() {
        int time = startHour * 60 + startMin + durationMin;
        finishHour = time / 60; finishMin = time % 60;
        if (finishHour >= 24)
            finishHour -= 24;
        timePicker_UpDown = true;  // to prevent double TimeChanged action
        binding.oneTimePicker.setHour(finishHour);
        binding.oneTimePicker.setMinute(finishMin);
        binding.oneDuration.setText(durationText());
        timePicker_UpDown = false;
        binding.oneTimePicker.invalidate();
    }

    private void saveOneTime() {

        boolean [] week = new boolean[]{true, true, true, true, true, true, true};
        quietTask = new QuietTask(subject, startHour, startMin, finishHour, finishMin,
                week, true, vibrate, 0, 0);    // onetime repeat is 0
        quietTasks.set(0, quietTask);
        mainRecycleViewAdapter.notifyItemChanged(0);

        utils.saveQuietTasksToShared();
        MannerMode.turn2Quiet(getApplicationContext(), vibrate);
        stateCode = STATE_ONETIME;
        if (mActivity == null)
            mActivity = new MainActivity();
        new ScheduleNextTask("One Time");
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_onetime, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            saveOneTime();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
