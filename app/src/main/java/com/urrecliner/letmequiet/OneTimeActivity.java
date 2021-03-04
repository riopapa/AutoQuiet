package com.urrecliner.letmequiet;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TimePicker;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.urrecliner.letmequiet.databinding.ActivityOneTimeBinding;
import com.urrecliner.letmequiet.models.QuietTask;

import java.util.Calendar;

import static com.urrecliner.letmequiet.Vars.STATE_ONETIME;
import static com.urrecliner.letmequiet.Vars.default_Duration;
import static com.urrecliner.letmequiet.Vars.interval_Long;
import static com.urrecliner.letmequiet.Vars.interval_Short;
import static com.urrecliner.letmequiet.Vars.mActivity;
import static com.urrecliner.letmequiet.Vars.quietTasks;
import static com.urrecliner.letmequiet.Vars.recycleViewAdapter;
import static com.urrecliner.letmequiet.Vars.stateCode;
import static com.urrecliner.letmequiet.Vars.utils;

public class OneTimeActivity extends AppCompatActivity {

    QuietTask quietTask;
    private String subject;
    private int startHour, startMin, finishHour, finishMin;
    private boolean vibrate, speaking;
    private int durationMin = 0;       // in minutes
    Calendar calendar;
    boolean timePicker_UpDown = false;
    ActivityOneTimeBinding binding;

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

//        Bundle data = getIntent().getExtras();
//        assert data != null;
//        silentInfo = (SilentInfo) data.getSerializable("silentInfo");
        quietTask = quietTasks.get(0);
        subject = quietTask.getSubject();
        vibrate = quietTask.isVibrate();
        speaking = quietTask.isSpeaking();
        calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND,0);
        startHour = calendar.get(Calendar.HOUR_OF_DAY);
        startMin = calendar.get(Calendar.MINUTE);
        finishHour = startHour + 1;     // default is 60 min.
        finishMin = startMin;
        durationMin = default_Duration;
        binding.oneTimePicker.setIs24HourView(true);
        binding.oneTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hour, int min) {
                if (timePicker_UpDown)
                    return;
                finishHour = hour; finishMin = min;
                durationMin = (finishHour - startHour) * 60 + finishMin - startMin;
                String text;
                if (durationMin > 1)
                    text = (""+(100 + durationMin / 60)).substring(1) + " : " + (""+(100 + durationMin % 60)).substring(1)+  " 후";
                else
                    text = getString(R.string.already_passed_time);
                binding.oneDuration.setText(text);
            }
        });
        buildScreen();
        buttonSetting();
        adjustTimePicker();
    }

    void buildScreen() {
        String text;
        text = interval_Short+"분▽"; binding.minus10Min.setText(text);
        text = interval_Short+"분△"; binding.plus10Min.setText(text);
        text = interval_Long+"분▽"; binding.minus30Min.setText(text);
        text = interval_Long+"분△"; binding.plus30Min.setText(text);
    }
    void buttonSetting() {
        binding.oneVibrate.setImageResource((vibrate)? R.mipmap.phone_vibrate :R.mipmap.phone_quiet);
        binding.oneVibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrate ^= true;
                binding.oneVibrate.setImageResource((vibrate)? R.mipmap.phone_vibrate :R.mipmap.phone_quiet);
                v.invalidate();
            }
        });
        binding.minus10Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (durationMin > interval_Short) {
                    durationMin -= interval_Short;
                    adjustTimePicker();
                }
            }
        });

        binding.plus10Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                durationMin += interval_Short;
                adjustTimePicker();
            }
        });

        binding.minus30Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (durationMin > interval_Long) {
                    durationMin -= interval_Long;
                    adjustTimePicker();
                }
            }
        });

        binding.plus30Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                durationMin += interval_Long;
                adjustTimePicker();
            }
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
        String text = (""+(100 + durationMin / 60)).substring(1) + " : " + (""+(100 + durationMin % 60)).substring(1) + " 후";
        binding.oneDuration.setText(text);
        timePicker_UpDown = false;
        binding.oneTimePicker.invalidate();
    }

    private void saveOneTime() {

        boolean [] week = new boolean[]{true, true, true, true, true, true, true};
        quietTask = new QuietTask(subject, startHour, startMin, finishHour, finishMin,
                week, true, vibrate, speaking);
        quietTasks.set(0, quietTask);
        recycleViewAdapter.notifyItemChanged(0);

        utils.saveQuietTasksToShared();
        MannerMode.turnOn(getApplicationContext(), subject, vibrate);
        stateCode = STATE_ONETIME;
        if (mActivity == null)
            mActivity = new MainActivity();
        mActivity.scheduleNextTask("One Time");
//
//        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        assert alarmManager != null;
//        Intent intentS = new Intent(this, AlarmReceiver.class);
//        Bundle args = new Bundle();
//        args.putSerializable("silentInfo", silentInfo);
//        intentS.putExtra("DATA",args);
//        intentS.putExtra("case","O");
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

    @Override
    protected void onPause() {
        super.onPause();
    }
}
