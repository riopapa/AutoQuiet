package com.urrecliner.autoquiet;

import static com.urrecliner.autoquiet.MainActivity.vars;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.urrecliner.autoquiet.databinding.ActivityAddEditBinding;
import com.urrecliner.autoquiet.models.QuietTask;
import com.urrecliner.autoquiet.Sub.NameColor;
import com.urrecliner.autoquiet.Sub.QuietTaskDefault;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AddUpdateActivity extends AppCompatActivity {

    private String subject;
    private int startHour, startMin, finishHour, finishMin;
    private boolean active;
    private int sRepeatCount, fRepeatCount;
    private boolean[] week = new boolean[7];
    private TextView[] weekView = new TextView[7];
    private boolean vibrate, agenda;
    private QuietTask quietTask;
    private ArrayList<QuietTask> quietTasks;
    private int currIdx;
    private ActivityAddEditBinding binding;
    private int colorOn, colorOnBack, colorOffBack, BGColorOn, BGColorOff;
    private Context context;
    private int xSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        context = this;
        binding = ActivityAddEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        quietTasks = new QuietTaskGetPut().get(this);
        Intent intent = getIntent();
        currIdx = intent.getExtras().getInt("idx",-1);
        if (vars.addNewQuiet)
            quietTask = new QuietTaskDefault().get();
        else
            quietTask = quietTasks.get(currIdx);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        xSize = metrics.widthPixels / 9;

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle((vars.addNewQuiet) ? R.string.add_table :R.string.update_table);
        actionBar.setIcon(R.mipmap.let_me_quiet_mini);
//        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowHomeEnabled(true);

        weekView[0] = binding.avWeek0; weekView[1] = binding.avWeek1;
        weekView[2] = binding.avWeek2; weekView[3] = binding.avWeek3;
        weekView[4] = binding.avWeek4; weekView[5] = binding.avWeek5;
        weekView[6] = binding.avWeek6;
        colorOn = ContextCompat.getColor(context, R.color.colorOn);
        colorOnBack = ContextCompat.getColor(context, R.color.colorOnBack);
        colorOffBack = ContextCompat.getColor(context, R.color.itemNormalFill);
        BGColorOff = ContextCompat.getColor(context, R.color.BackGroundActiveOff);
        BGColorOn = ContextCompat.getColor(context, R.color.BackGroundActiveOn);
        build_QuietTask();

    }

    void build_QuietTask() {

        final String[] weekName = {"주", "월", "화", "수", "목", "금", "토"};
        subject = quietTask.subject;
        startHour = quietTask.startHour;
        startMin = quietTask.startMin;
        finishHour = quietTask.finishHour;
        finishMin = quietTask.finishMin;
        active = quietTask.active;
        sRepeatCount = quietTask.sRepeatCount;
        fRepeatCount = quietTask.fRepeatCount;
        week = quietTask.week;
        vibrate = quietTask.vibrate;
        agenda = quietTask.agenda;

        binding.gCal.setImageResource((agenda)? R.drawable.calendar:R.mipmap.speaking_noactive);
        binding.timePickerStart.setIs24HourView(true);
        binding.timePickerStart.setHour(startHour); binding.timePickerStart.setMinute(startMin);
        binding.timePickerFinish.setIs24HourView(true);
        binding.timePickerFinish.setHour(finishHour); binding.timePickerFinish.setMinute(finishMin);

        if (subject == null)
            subject = getString(R.string.no_subject);
        binding.etSubject.setText(subject);
        binding.etSubject.setBackgroundColor(NameColor.get(quietTask.calName, context));
        if (agenda)
            binding.weekFlag.setVisibility(View.GONE);
        else {
            binding.weekFlag.setVisibility(View.VISIBLE);
            for (int i = 0; i < 7; i++) {
                weekView[i].setId(i);
                weekView[i].setWidth(xSize);
                weekView[i].setGravity(Gravity.CENTER);
                weekView[i].setTextColor(colorOn);
                weekView[i].setBackgroundColor((week[i]) ? colorOnBack : colorOffBack);
                weekView[i].setTypeface(null, (week[i]) ? Typeface.BOLD : Typeface.NORMAL);
                weekView[i].setText(weekName[i]);
            }
        }
        binding.iVVibrate.setImageResource((vibrate)? R.drawable.phone_vibrate : R.drawable.phone_off);
        binding.tVVibrate.setOnClickListener(v -> {
            vibrate = !vibrate;
            binding.iVVibrate.setImageResource((vibrate)? R.drawable.phone_vibrate : R.drawable.phone_off);
            v.invalidate();
        });
        binding.iVVibrate.setOnClickListener(v -> {
            vibrate = !vibrate;
            binding.iVVibrate.setImageResource((vibrate)? R.drawable.phone_vibrate : R.drawable.phone_off);
            v.invalidate();
        });

        if (agenda)
            binding.swActive.setVisibility(View.GONE);
        else {
            binding.swActive.setChecked(active);
            binding.swActive.setOnClickListener(v -> {
                active = !active;
                binding.addUpdate.setBackgroundColor(active? BGColorOn : BGColorOff);
                binding.swActive.setChecked(active);
                v.invalidate();
            });
            binding.addUpdate.setBackgroundColor(active? BGColorOn : BGColorOff);
        }

        binding.tVStartRepeat.setOnClickListener(v -> {
            if (sRepeatCount == 0)
                sRepeatCount = 1;
            else if (sRepeatCount == 1)
                sRepeatCount = 11;
            else
                sRepeatCount = 0;
            binding.iVStartRepeat.setImageResource((sRepeatCount == 0)? R.drawable.speak_off: (sRepeatCount == 1)? R.drawable.speak_on : R.mipmap.speak_repeat);
            v.invalidate();
        });
        binding.iVStartRepeat.setImageResource((sRepeatCount == 0)? R.drawable.speak_off: (sRepeatCount == 1)? R.drawable.speak_on : R.mipmap.speak_repeat);
        binding.iVStartRepeat.setOnClickListener(v -> {
            if (sRepeatCount == 0)
                sRepeatCount = 1;
            else if (sRepeatCount == 1)
                sRepeatCount = 11;
            else
                sRepeatCount = 0;
            binding.iVStartRepeat.setImageResource((sRepeatCount == 0)? R.drawable.speak_off: (sRepeatCount == 1)? R.drawable.speak_on : R.mipmap.speak_repeat);
            v.invalidate();
        });

        binding.tVFinishRepeat.setOnClickListener(v -> {
            if (fRepeatCount == 0)
                fRepeatCount = 1;
            else if (fRepeatCount == 1)
                fRepeatCount = 11;
            else
                fRepeatCount = 0;
            binding.iVFinishRepeat.setImageResource((fRepeatCount == 0)? R.drawable.speak_off: (fRepeatCount == 1)? R.drawable.speak_on : R.mipmap.speak_repeat);
            v.invalidate();
        });
        binding.iVFinishRepeat.setImageResource((fRepeatCount == 0)? R.drawable.speak_off: (fRepeatCount == 1)? R.drawable.speak_on : R.mipmap.speak_repeat);
        binding.iVFinishRepeat.setOnClickListener(v -> {
            if (fRepeatCount == 0)
                fRepeatCount = 1;
            else if (fRepeatCount == 1)
                fRepeatCount = 11;
            else
                fRepeatCount = 0;
            binding.iVFinishRepeat.setImageResource((fRepeatCount == 0)? R.drawable.speak_off: (fRepeatCount == 1)? R.drawable.speak_on : R.mipmap.speak_repeat);
            v.invalidate();
        });

        TextView tv = findViewById(R.id.dateDesc);
        if (agenda) {
            SimpleDateFormat sdfDate = new SimpleDateFormat("MM-dd(EEE)", Locale.getDefault());
            String s = sdfDate.format(quietTask.calStartDate);
            if (!quietTask.calLocation.equals(""))
                s += "\n" + quietTask.calLocation;
            if (!quietTask.calDesc.equals(""))
                s += "\n" + quietTask.calDesc;
            tv.setText(s);
        } else
            tv.setText("");
    }

    public void toggleWeek(View v) {
        int i = v.getId();
        week[i] ^= true;
        weekView[i].setBackgroundColor((week[i]) ? colorOnBack:colorOffBack);
        weekView[i].setTypeface(null, (week[i]) ? Typeface.BOLD: Typeface.NORMAL);
        v.invalidate();
    }

    private void save_QuietTask() {

        boolean any = false;
        for (int i = 0; i < 7; i++) {
            any |= week[i];
        }
        if (!any) {
            Toast.makeText(getBaseContext(), R.string.at_least_one_day_selected, Toast.LENGTH_LONG).show();
            return;
        }

        subject = binding.etSubject.getText().toString();
        if (subject.length() == 0)
            subject = getString(R.string.no_subject);
        startHour = binding.timePickerStart.getHour(); startMin = binding.timePickerStart.getMinute();
        finishHour = binding.timePickerFinish.getHour(); finishMin = binding.timePickerFinish.getMinute();

        if (agenda) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(quietTask.calStartDate);
            c.set(Calendar.HOUR_OF_DAY, startHour);
            c.set(Calendar.MINUTE, startMin);
            long startDate = c.getTimeInMillis();
            c.set(Calendar.HOUR_OF_DAY, finishHour);
            c.set(Calendar.MINUTE, finishMin);
            long finishDate = c.getTimeInMillis();
            QuietTask quietNew = new QuietTask(subject, startDate, finishDate,
                    quietTask.calId, quietTask.calName, quietTask.calDesc, quietTask.calLocation,
                    true, vibrate, sRepeatCount, fRepeatCount, true);
            quietTasks.set(currIdx, quietNew);
        } else {
            quietTask = new QuietTask(subject, startHour, startMin, finishHour, finishMin,
                    week, active, vibrate, sRepeatCount, fRepeatCount);
            if (vars.addNewQuiet)
                quietTasks.add(quietTask);
            else
                quietTasks.set(currIdx, quietTask);
        }
        new QuietTaskGetPut().put(quietTasks, context, "Add/Update");

        finish();
//        new NextTask(context, ((vars.addNewQuiet) ? "Added" : "Updated"));
        }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_add, menu);
        if (vars.addNewQuiet) {
            menu.findItem(R.id.action_delete).setEnabled(false);
            menu.findItem(R.id.action_delete).getIcon().setAlpha(80);
        }
        else {
            menu.findItem(R.id.action_delete).setEnabled(true);
            menu.findItem(R.id.action_delete).getIcon().setAlpha(255);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_save) {
            save_QuietTask();
        } else if (id == R.id.action_delete) {
            new QuietTaskGetPut().put(quietTasks, context, "del "+quietTask.subject);
            quietTasks.remove(currIdx);
            cancel_QuietTask();
        } else if (id == R.id.action_delete_multi) {
            if (agenda) {
                int delId = quietTask.calId;
                for (int i = 0; i < quietTasks.size(); ) {
                    if (quietTasks.get(i).calId == delId) {
                        quietTasks.remove(i);
                    }
                    else
                        i++;
                }
            } else {
                quietTasks.remove(currIdx);
            }
            new QuietTaskGetPut().put(quietTasks, context, "del "+subject);
            cancel_QuietTask();
        }
        finish();
        return false;
    }

    private void cancel_QuietTask() {

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        assert alarmManager != null;
        Intent intent = new Intent(this, com.urrecliner.autoquiet.AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(com.urrecliner.autoquiet.AddUpdateActivity.this, 23456, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    protected void onPause() {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.hideSoftInputFromWindow(binding.etSubject.getWindowToken(), 0);
        super.onPause();
    }
}