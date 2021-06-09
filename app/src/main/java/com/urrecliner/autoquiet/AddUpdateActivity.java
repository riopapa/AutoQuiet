package com.urrecliner.autoquiet;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.urrecliner.autoquiet.databinding.ActivityAddEditBinding;
import com.urrecliner.autoquiet.models.QuietTask;
import com.urrecliner.autoquiet.utility.NameColor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.urrecliner.autoquiet.Vars.addNewQuiet;
import static com.urrecliner.autoquiet.Vars.mContext;
import static com.urrecliner.autoquiet.Vars.mainRecycleViewAdapter;
import static com.urrecliner.autoquiet.Vars.quietTasks;
import static com.urrecliner.autoquiet.Vars.quietUniq;
import static com.urrecliner.autoquiet.Vars.sdfDateTime;
import static com.urrecliner.autoquiet.Vars.utils;
import static com.urrecliner.autoquiet.Vars.weekName;
import static com.urrecliner.autoquiet.Vars.xSize;

public class AddUpdateActivity extends AppCompatActivity {

    private String subject;
    private int startHour, startMin, finishHour, finishMin;
    private boolean active;
    private int sRepeatCount, fRepeatCount;
    private boolean[] week = new boolean[7];
    private TextView[] weekView = new TextView[7];
    private boolean vibrate, agenda;
    private QuietTask quietTask;
    private int currIdx;
    private ActivityAddEditBinding binding;
    private int colorOn, colorOnBack, colorOffBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityAddEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        currIdx = intent.getExtras().getInt("idx",-1);
        if (addNewQuiet)
            quietTask = getQuietTaskDefault();
        else
            quietTask = quietTasks.get(currIdx);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle((addNewQuiet) ? R.string.add_table :R.string.update_table);
        actionBar.setIcon(R.mipmap.let_me_quiet);
//        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowHomeEnabled(true);

        weekView[0] = binding.avWeek0; weekView[1] = binding.avWeek1; weekView[2] = binding.avWeek2;
        weekView[3] = binding.avWeek3; weekView[4] = binding.avWeek4; weekView[5] = binding.avWeek5;
        weekView[6] = binding.avWeek6;
        colorOn = ResourcesCompat.getColor(mContext.getResources(), R.color.colorOn, null);
        colorOnBack = ResourcesCompat.getColor(mContext.getResources(), R.color.colorOnBack, null);
        colorOffBack = ResourcesCompat.getColor(mContext.getResources(), R.color.itemNormalFill, null);

        build_QuietTask();
    }

    void build_QuietTask() {

        subject = quietTask.getSubject();
        startHour = quietTask.getStartHour();
        startMin = quietTask.getStartMin();
        finishHour = quietTask.getFinishHour();
        finishMin = quietTask.getFinishMin();
        active = quietTask.isActive();
        sRepeatCount = quietTask.getsRepeatCount();
        fRepeatCount = quietTask.getfRepeatCount();
        week = quietTask.getWeek();
        vibrate = quietTask.isVibrate();
        agenda = quietTask.agenda;

        binding.gCal.setImageResource((agenda)? R.mipmap.calendar:R.mipmap.transparent);
        binding.timePickerStart.setIs24HourView(true);
        binding.timePickerStart.setHour(startHour); binding.timePickerStart.setMinute(startMin);
        binding.timePickerFinish.setIs24HourView(true);
        binding.timePickerFinish.setHour(finishHour); binding.timePickerFinish.setMinute(finishMin);

        if (subject == null)
            subject = getString(R.string.no_subject);
        binding.etSubject.setText(subject);
        binding.etSubject.setBackgroundColor(NameColor.get(quietTask.calName));
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
        binding.avVibrate.setImageResource((vibrate)? R.mipmap.phone_vibrate :R.mipmap.phone_quiet);
        binding.avVibrate.setOnClickListener(v -> {
            vibrate ^= true;
            binding.avVibrate.setImageResource((vibrate)? R.mipmap.phone_vibrate :R.mipmap.phone_quiet);
            v.invalidate();
        });

        if (agenda)
            binding.swActive.setVisibility(View.GONE);
        else {
            binding.swActive.setChecked(active);
            binding.swActive.setOnClickListener(v -> {
                active ^= true;
                binding.swActive.setChecked(active);
                v.invalidate();
            });
        }
        binding.iVstartRepeat.setImageResource((sRepeatCount == 0)? R.mipmap.speaking_off: (sRepeatCount == 1)? R.mipmap.speaking_on : R.mipmap.speak_repeat);
        binding.iVstartRepeat.setOnClickListener(v -> {
            if (sRepeatCount == 0)
                sRepeatCount = 1;
            else if (sRepeatCount == 1)
                sRepeatCount = 11;
            else
                sRepeatCount = 0;
            binding.iVstartRepeat.setImageResource((sRepeatCount == 0)? R.mipmap.speaking_off: (sRepeatCount == 1)? R.mipmap.speaking_on : R.mipmap.speak_repeat);
            v.invalidate();
        });

        binding.iVFinishRepeat.setImageResource((fRepeatCount == 0)? R.mipmap.speaking_off: (fRepeatCount == 1)? R.mipmap.speaking_on : R.mipmap.speak_repeat);
        binding.iVFinishRepeat.setOnClickListener(v -> {
            if (fRepeatCount == 0)
                fRepeatCount = 1;
            else if (fRepeatCount == 1)
                fRepeatCount = 11;
            else
                fRepeatCount = 0;
            binding.iVFinishRepeat.setImageResource((fRepeatCount == 0)? R.mipmap.speaking_off: (fRepeatCount == 1)? R.mipmap.speaking_on : R.mipmap.speak_repeat);
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
                week, true, true, 1, 0);
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
            if (addNewQuiet)
                quietTasks.add(quietTask);
            else
                quietTasks.set(currIdx, quietTask);
        }
        utils.saveQuietTasksToShared();

        finish();
        new ScheduleNextTask("Schedule "+ ((addNewQuiet) ? "Added" : "Updated"));
        mainRecycleViewAdapter.notifyItemChanged(currIdx, quietTask);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_add, menu);
        if (addNewQuiet) {
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
        switch (id) {
            case R.id.action_save:
                save_QuietTask();
                break;
            case R.id.action_delete:
                quietTasks.remove(currIdx);
                utils.saveQuietTasksToShared();
                mainRecycleViewAdapter.notifyDataSetChanged();
                cancel_QuietTask();
                break;
            case R.id.action_delete_multi:
                if (agenda) {
                    int delId = quietTask.calId;
                    for (int i = 0; i < quietTasks.size(); ) {
                        if (quietTasks.get(i).calId == delId)
                            quietTasks.remove(i);
                        else
                            i++;
                    }
                } else
                    quietTasks.remove(currIdx);
                utils.saveQuietTasksToShared();
                mainRecycleViewAdapter.notifyDataSetChanged();
                cancel_QuietTask();
                break;
            }
        finish();
        return false;
    }

    private void cancel_QuietTask() {

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        assert alarmManager != null;
        Intent intent = new Intent(this, com.urrecliner.autoquiet.AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(com.urrecliner.autoquiet.AddUpdateActivity.this, quietUniq, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
//        pendingIntent = PendingIntent.getBroadcast(com.urrecliner.autoquiet.AddUpdateActivity.this, quietUniq +1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//        alarmManager.cancel(pendingIntent);
    }

    @Override
    protected void onPause() {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.hideSoftInputFromWindow(binding.etSubject.getWindowToken(), 0);
        super.onPause();
    }
}
