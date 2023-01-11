package com.urrecliner.autoquiet;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.urrecliner.autoquiet.databinding.ActivityOneTimeBinding;
import com.urrecliner.autoquiet.models.QuietTask;
import com.urrecliner.autoquiet.utility.VarsGetPut;

import java.util.ArrayList;
import java.util.Calendar;

public class OneTimeActivity extends AppCompatActivity {

    QuietTask quietTask;
    ArrayList<QuietTask> quietTasks;
    private String subject;
    private int startHour, startMin, finishHour, finishMin, fRepeatCount;
    private boolean vibrate;
    private int durationMin = 0;       // in minutes
    Calendar calendar;
    boolean timePicker_UpDown = false;
    ActivityOneTimeBinding binding;
    int shortInterval, longInterval;
    Vars vars;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        vars = new VarsGetPut().get(this);
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
        quietTasks = new QuietTaskGetPut().get(this);
        quietTask = quietTasks.get(0);
        subject = quietTask.getSubject();
        vibrate = quietTask.isVibrate();
        fRepeatCount = quietTask.getfRepeatCount();
        durationMin = Integer.parseInt(vars.sharedTimeInit);
        shortInterval = Integer.parseInt(vars.sharedTimeShort);
        longInterval = Integer.parseInt(vars.sharedTimeLong);
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
        text = "▼"+vars.sharedTimeShort+"분▼"; binding.minus10Min.setText(text);
        text = "▲"+vars.sharedTimeShort+"분▲"; binding.plus10Min.setText(text);
        text = "▼"+vars.sharedTimeLong+"분▼"; binding.minus30Min.setText(text);
        text = "▲"+vars.sharedTimeLong+"분▲"; binding.plus30Min.setText(text);
    }

    void buttonSetting() {
        binding.oneVibrate.setImageResource((vibrate)? R.drawable.phone_normal :R.drawable.phone_off);
        binding.oneVibrate.setOnClickListener(v -> {
            vibrate ^= true;
            binding.oneVibrate.setImageResource((vibrate)? R.drawable.phone_normal :R.drawable.phone_off);
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

        binding.oneFinishRepeat.setImageResource((fRepeatCount == 0)? R.drawable.speak_off: (fRepeatCount == 1)? R.drawable.speak_on : R.mipmap.speak_repeat);
        binding.oneFinishRepeat.setOnClickListener(v -> {
            if (fRepeatCount == 0)
                fRepeatCount = 1;
            else if (fRepeatCount == 1)
                fRepeatCount = 11;
            else
                fRepeatCount = 0;
            binding.oneFinishRepeat.setImageResource((fRepeatCount == 0)? R.drawable.speak_off : (fRepeatCount == 1)? R.drawable.speak_on : R.mipmap.speak_repeat);
            v.invalidate();
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
                week, true, vibrate, 0, fRepeatCount);    // onetime repeat is 0

        quietTasks.set(0, quietTask);
        MainActivity.mainRecycleAdapter.notifyItemChanged(0);

        new QuietTaskGetPut().put(quietTasks);
        MannerMode.turn2Quiet(this, vars.sharedManner, vibrate);
        new NextTask(this,"One Time");
        new VarsGetPut().put(vars);
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