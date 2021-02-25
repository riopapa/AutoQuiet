package com.urrecliner.letmequiet;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.urrecliner.letmequiet.databinding.ActivityAddBinding;

import static com.urrecliner.letmequiet.Vars.STATE_ADD_UPDATE;
import static com.urrecliner.letmequiet.Vars.addNewQuiet;
import static com.urrecliner.letmequiet.Vars.colorOff;
import static com.urrecliner.letmequiet.Vars.colorOffBack;
import static com.urrecliner.letmequiet.Vars.colorOn;
import static com.urrecliner.letmequiet.Vars.colorOnBack;
import static com.urrecliner.letmequiet.Vars.mActivity;
import static com.urrecliner.letmequiet.Vars.recycleViewAdapter;
import static com.urrecliner.letmequiet.Vars.qIdx;
import static com.urrecliner.letmequiet.Vars.quietTask;
import static com.urrecliner.letmequiet.Vars.quietTasks;
import static com.urrecliner.letmequiet.Vars.quietUniq;
import static com.urrecliner.letmequiet.Vars.stateCode;
import static com.urrecliner.letmequiet.Vars.utils;
import static com.urrecliner.letmequiet.Vars.weekName;
import static com.urrecliner.letmequiet.Vars.xSize;

public class AddUpdateActivity extends AppCompatActivity {

    private String subject;
    private int startHour, startMin, finishHour, finishMin;
    private boolean active, speaking;
    private boolean[] week = new boolean[7];
    private TextView[] weekView = new TextView[7];
    private boolean vibrate;
    private final String logID = "Add,Update";
    private ActivityAddBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (addNewQuiet)
            quietTask = mActivity.getQuietTaskDefault();
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle((addNewQuiet) ? R.string.add_table :R.string.update_table);

        weekView[0] = binding.avWeek0; weekView[1] = binding.avWeek1; weekView[2] = binding.avWeek2; weekView[3] = binding.avWeek3;
        weekView[4] = binding.avWeek4; weekView[5] = binding.avWeek5; weekView[6] = binding.avWeek6;
        build_QuietTask();
    }

    void build_QuietTask() {

        subject = quietTask.subject;
        startHour = quietTask.startHour;
        startMin = quietTask.startMin;
        finishHour = quietTask.finishHour;
        finishMin = quietTask.finishMin;
        active = quietTask.active;
        speaking = quietTask.speaking;
        week = quietTask.week;
        vibrate = quietTask.vibrate;
        binding.timePickerStart.setIs24HourView(true);
        binding.timePickerStart.setHour(startHour); binding.timePickerStart.setMinute(startMin);
        binding.timePickerFinish.setIs24HourView(true);
        binding.timePickerFinish.setHour(finishHour); binding.timePickerFinish.setMinute(finishMin);

        if (subject == null)
            subject = getString(R.string.no_subject);
        binding.etSubject.setText(subject);
        for (int i=0; i < 7; i++) {
            weekView[i].setId(i);
            weekView[i].setWidth(xSize);
            weekView[i].setGravity(Gravity.CENTER);
            weekView[i].setTextColor((week[i]) ? colorOn:colorOff);
            weekView[i].setBackgroundColor((week[i]) ? colorOnBack:colorOffBack);
            weekView[i].setTypeface(null, (week[i]) ? Typeface.BOLD: Typeface.NORMAL);
            weekView[i].setText(weekName[i]);
        }

        binding.avVibrate.setImageResource((vibrate)? R.mipmap.phone_vibrate_blue:R.mipmap.phone_quiet_red);
        binding.avVibrate.setOnClickListener(v -> {
            vibrate ^= true;
            binding.avVibrate.setImageResource((vibrate)? R.mipmap.phone_vibrate_blue:R.mipmap.phone_quiet_red);
            v.invalidate();
        });

        binding.swActive.setChecked(active);
        binding.swActive.setOnClickListener(v -> {
            active ^= true;
            binding.swActive.setChecked(active);
            v.invalidate();
        });

        binding.ivSpeaking.setImageResource((speaking)? R.mipmap.speaking_on:R.mipmap.speaking_off);
        binding.ivSpeaking.setOnClickListener(v -> {
            speaking ^= true;
            binding.ivSpeaking.setImageResource((speaking)? R.mipmap.speaking_on:R.mipmap.speaking_off);
            v.invalidate();
        });
    }

   public void toggleWeek(View v) {
        int i = v.getId();
        week[i] ^= true;
        weekView[i].setTextColor((week[i]) ? colorOn:colorOff);
        weekView[i].setBackgroundColor((week[i]) ? colorOnBack:colorOffBack);
        weekView[i].setTypeface(null, (week[i]) ? Typeface.BOLD: Typeface.NORMAL);
        v.invalidate();
    }

    private void save_QuietTask() {

        boolean any = false;
        for (int i = 0; i < 7; i++) { any |= week[i]; }
        if (!any) {
            Toast.makeText(getBaseContext(), R.string.at_least_one_day_selected, Toast.LENGTH_LONG).show();
            return;
        }

        subject = binding.etSubject.getText().toString();
        if (subject.length() == 0)
            subject = "No Subject";
        startHour = binding.timePickerStart.getHour(); startMin = binding.timePickerStart.getMinute();
        finishHour = binding.timePickerFinish.getHour(); finishMin = binding.timePickerFinish.getMinute();
        quietTask = new QuietTask(subject, startHour, startMin, finishHour, finishMin,
            week, active, vibrate, speaking);
        if (addNewQuiet)
            quietTasks.add(quietTask);
        else
            quietTasks.set(qIdx, quietTask);
        utils.saveSharedPrefTables();

        stateCode = STATE_ADD_UPDATE;
        utils.log(logID, stateCode + " "+utils.hourMin(startHour,startMin));
        finish();
        recycleViewAdapter.notifyItemChanged(qIdx, quietTask);

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
                quietTasks.remove(qIdx);
                recycleViewAdapter.notifyItemChanged(qIdx);
                utils.saveSharedPrefTables();
                cancel_QuietTask();
                break;
        }
        finish();
        return false;
    }

    private void cancel_QuietTask() {

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        assert alarmManager != null;
        Intent intent = new Intent(this, com.urrecliner.letmequiet.AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(com.urrecliner.letmequiet.AddUpdateActivity.this, quietUniq, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
        pendingIntent = PendingIntent.getBroadcast(com.urrecliner.letmequiet.AddUpdateActivity.this, quietUniq +1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
        utils.log(logID, "quietTask Deleted");
    }

    @Override
    protected void onPause() {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.hideSoftInputFromWindow(binding.etSubject.getWindowToken(), 0);
        super.onPause();
    }
}
