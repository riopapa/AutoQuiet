package com.urrecliner.autoquiet;

import static com.urrecliner.autoquiet.ActivityMain.mainRecycleAdapter;
import static com.urrecliner.autoquiet.ActivityMain.vars;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.urrecliner.autoquiet.Sub.CalculateNext;
import com.urrecliner.autoquiet.Sub.NameColor;
import com.urrecliner.autoquiet.Sub.QuietTaskDefault;
import com.urrecliner.autoquiet.databinding.ActivityAddEditBinding;
import com.urrecliner.autoquiet.models.QuietTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ActivityAddEdit extends AppCompatActivity {

    private String subject;
    private int startHour, startMin, finishHour, finishMin;
    private boolean active, finish99;
    private int sRepeatCount, fRepeatCount;
    private boolean[] week = new boolean[7];
    private TextView[] weekView = new TextView[7];
    private boolean vibrate, agenda, sayDate;
    private QuietTask qT;
    private ArrayList<QuietTask> quietTasks;
    private int currIdx;
    private ActivityAddEditBinding binding;
    private int colorOn, colorOnBack, colorOffBack, BGColorOn, BGColorOff;
    private Context context;
    private int xSize, numPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        context = this;
        binding = ActivityAddEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        quietTasks = new QuietTaskGetPut().get(this);
        Intent intent = getIntent();
        currIdx = intent.getExtras().getInt("idx",-1);
        if (currIdx == -1)
            qT = new QuietTaskDefault().get();
        else
            qT = quietTasks.get(currIdx);
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
        subject = qT.subject;
        startHour = qT.startHour;
        startMin = qT.startMin;
        finishHour = qT.finishHour;
        finishMin = qT.finishMin;
        finish99 = qT.finishHour == 99;
        active = qT.active;
        sRepeatCount = qT.sRepeatCount;
        fRepeatCount = qT.fRepeatCount;
        sayDate = qT.sayDate;
        week = qT.week;
        vibrate = qT.vibrate;
        agenda = qT.agenda;

        findViewById(R.id.num0).setOnClickListener(this::number_Clicked);
        findViewById(R.id.num1).setOnClickListener(this::number_Clicked);
        findViewById(R.id.num2).setOnClickListener(this::number_Clicked);
        findViewById(R.id.num3).setOnClickListener(this::number_Clicked);
        findViewById(R.id.num4).setOnClickListener(this::number_Clicked);
        findViewById(R.id.num5).setOnClickListener(this::number_Clicked);
        findViewById(R.id.num6).setOnClickListener(this::number_Clicked);
        findViewById(R.id.num7).setOnClickListener(this::number_Clicked);
        findViewById(R.id.num8).setOnClickListener(this::number_Clicked);
        findViewById(R.id.num9).setOnClickListener(this::number_Clicked);
        findViewById(R.id.numBack).setOnClickListener(view -> {
            if (numPos > 1) {
                numPos--;
                set_TimeForm();
            }
        });
        findViewById(R.id.numFore).setOnClickListener(view -> {
            if (numPos < 4) {
                numPos++;
            } else
                numPos = 1;
            set_TimeForm();
        });

        binding.gCal.setImageResource((agenda)? R.drawable.calendar:R.mipmap.speaking_noactive);
        binding.timePickerStart.setIs24HourView(true);
        binding.timePickerFinish.setIs24HourView(true);
        numPos = 1;
        set_TimeForm();

        binding.etSubject.setText(subject);
        binding.etSubject.setBackgroundColor(NameColor.get(qT.calName, context));
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

        binding.finish99.setOnClickListener(v -> {
            finish99 = !finish99;
            binding.finish99.setChecked(finish99);
            numPos = 1;
            set_TimeForm();
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
                sRepeatCount = 9;
            else
                sRepeatCount = 0;
            binding.iVStartRepeat.setImageResource((sRepeatCount == 0)? R.drawable.speak_off: (sRepeatCount == 1)? R.drawable.alert_bell : R.drawable.speak_on);
            v.invalidate();
        });

        binding.iVStartRepeat.setImageResource((sRepeatCount == 0)? R.drawable.speak_off: (sRepeatCount == 1)? R.drawable.alert_bell : R.drawable.speak_on);
        binding.iVStartRepeat.setOnClickListener(v -> {
            if (sRepeatCount == 0)
                sRepeatCount = 1;
            else if (sRepeatCount == 1)
                sRepeatCount = 11;
            else
                sRepeatCount = 0;
            binding.iVStartRepeat.setImageResource((sRepeatCount == 0)? R.drawable.speak_off: (sRepeatCount == 1)? R.drawable.alert_bell : R.drawable.speak_on);
            v.invalidate();
        });

        binding.tVFinishRepeat.setOnClickListener(v -> {
            if (fRepeatCount == 0)
                fRepeatCount = 1;
            else if (fRepeatCount == 1)
                fRepeatCount = 11;
            else
                fRepeatCount = 0;
            binding.iVFinishRepeat.setImageResource((fRepeatCount == 0)? R.drawable.speak_off: (fRepeatCount == 1)? R.drawable.alert_bell : R.drawable.speak_on);
            v.invalidate();
        });
        binding.iVFinishRepeat.setImageResource((fRepeatCount == 0)? R.drawable.speak_off: (fRepeatCount == 1)? R.drawable.alert_bell : R.drawable.speak_on);
        binding.iVFinishRepeat.setOnClickListener(v -> {
            if (fRepeatCount == 0)
                fRepeatCount = 1;
            else if (fRepeatCount == 1)
                fRepeatCount = 11;
            else
                fRepeatCount = 0;
            binding.iVFinishRepeat.setImageResource((fRepeatCount == 0)? R.drawable.speak_off: (fRepeatCount == 1)? R.drawable.alert_bell : R.drawable.speak_on);
            v.invalidate();
        });
        binding.sayDate.setChecked(sayDate);    // sayDate is only for finish Time
        binding.sayDate.setOnClickListener(v -> {
            sayDate = !sayDate;
            binding.sayDate.setChecked(sayDate);
            v.invalidate();
        });

        TextView tv = findViewById(R.id.dateDesc);
        if (agenda) {
            SimpleDateFormat sdfDate = new SimpleDateFormat("MM-dd(EEE)", Locale.getDefault());
            String s = sdfDate.format(qT.calStartDate);
            if (!qT.calLocation.equals(""))
                s += "\n" + qT.calLocation;
            if (!qT.calDesc.equals(""))
                s += "\n" + qT.calDesc;
            tv.setText(s);
        } else
            tv.setText("");

        binding.numHH1.setOnClickListener(v -> {
            numPos = 1;
            show_ResultTime();
        });
        binding.numHH2.setOnClickListener(v -> {
            numPos = 2;
            show_ResultTime();
        });
        binding.numMM1.setOnClickListener(v -> {
            numPos = 3;
            show_ResultTime();
        });
        binding.numMM2.setOnClickListener(v -> {
            numPos = 4;
            show_ResultTime();
        });
    }

    private void set_TimeForm() {
        binding.finish99.setChecked(finish99);
        if (!finish99) {    // normal start, finish
            if (finishHour == 99)
                finishHour = startHour;
            binding.timePickerStart.setVisibility(View.VISIBLE);
            binding.timePickerFinish.setVisibility(View.VISIBLE);
            binding.timePickerStart.setHour(startHour);
            binding.timePickerStart.setMinute(startMin);
            binding.numDateTime.setVisibility(View.GONE);
            binding.timePickerFinish.setHour(finishHour);
            binding.timePickerFinish.setMinute(finishMin);
            binding.timePickerFinish.setHour(finishHour); binding.timePickerFinish.setMinute(finishMin);
            binding.iVVibrate.setImageResource((vibrate) ? R.drawable.phone_vibrate : R.drawable.phone_off);
            binding.iVVibrate.setOnClickListener(v -> {
                vibrate = !vibrate;
                binding.iVVibrate.setImageResource((vibrate) ? R.drawable.phone_vibrate : R.drawable.phone_off);
                v.invalidate();
            });
            binding.tVVibrate.setOnClickListener(v -> {
                vibrate = !vibrate;
                binding.iVVibrate.setImageResource((vibrate) ? R.drawable.phone_vibrate : R.drawable.phone_off);
                v.invalidate();
            });

        } else {
            binding.numDateTime.setVisibility(View.VISIBLE);
            binding.timePickerStart.setVisibility(View.GONE);
            binding.timePickerFinish.setVisibility(View.GONE);
            binding.iVVibrate.setImageResource(R.drawable.alarm);
            show_ResultTime();
        }
    }

    private void show_ResultTime() {
        String s = (startHour > 9) ? ""+startHour: "0"+startHour;
        String s1 = s.substring(0,1); String s2 = s.substring(1);
        binding.numHH1.setText(s1); binding.numHH1.setBackgroundColor(0x00bbbbbb);
        binding.numHH2.setText(s2); binding.numHH2.setBackgroundColor(0x00bbbbbb);
        s = (startMin > 9) ? ""+startMin: "0"+startMin;
        s1 = s.substring(0,1); s2 = s.substring(1);
        binding.numMM1.setText(s1); binding.numMM1.setBackgroundColor(0x00bbbbbb);
        binding.numMM2.setText(s2); binding.numMM2.setBackgroundColor(0x00bbbbbb);

        TextView tv;
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(100); //You can manage the blinking time with this parameter
        anim.setStartOffset(100);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);

        tv = binding.numHH1;
        if (numPos == 1) { tv.startAnimation(anim); tv.setBackgroundColor(0xffbbbbbb);}
        else tv.clearAnimation();
        tv = binding.numHH2;
        if (numPos == 2) { tv.startAnimation(anim); tv.setBackgroundColor(0xffbbbbbb);}
        else tv.clearAnimation();
        tv = binding.numMM1;
        if (numPos == 3) { tv.startAnimation(anim); tv.setBackgroundColor(0xffbbbbbb);}
        else tv.clearAnimation();
        tv = binding.numMM2;
        if (numPos == 4) { tv.startAnimation(anim); tv.setBackgroundColor(0xffbbbbbb);}
        else tv.clearAnimation();

    }

    public void toggleWeek(View v) {
        int i = v.getId();
        week[i] ^= true;
        weekView[i].setBackgroundColor((week[i]) ? colorOnBack:colorOffBack);
        weekView[i].setTypeface(null, (week[i]) ? Typeface.BOLD: Typeface.NORMAL);
        v.invalidate();
    }

    private void number_Clicked(View v) {
        int num = Integer.parseInt(v.getTag().toString());
        if (numPos == 1) {
            int hour = num * 10 + Integer.parseInt(binding.numHH2.getText().toString());
            if (hour < 25)
                startHour = hour;
            else
                startHour = num * 10;
        } else if (numPos == 2) {
            int hour = Integer.parseInt(binding.numHH1.getText().toString()) * 10 + num;
            if (hour < 25)
                startHour = hour;
        } else if (numPos == 3) {
            int min = num * 10 + Integer.parseInt(binding.numMM2.getText().toString());
            if (min < 60)
                startMin = min;
        } else {
            startMin = Integer.parseInt(binding.numMM1.getText().toString()) * 10 + num;
        }
        if (numPos < 4)
            numPos++;
        set_TimeForm();
    }

    private void save_QuietTask() {

        int any = 0;
        for (int i = 0; i < 7; i++) {
            if (week[i]) any++;
        }
        if (any == 0) {
            Toast.makeText(getBaseContext(), R.string.at_least_one_day_selected, Toast.LENGTH_LONG).show();
            return;
        }

        subject = binding.etSubject.getText().toString();
        if (subject.length() == 0)
            subject = getString(R.string.no_subject);
        if (!finish99) {
            startHour = binding.timePickerStart.getHour();
            startMin = binding.timePickerStart.getMinute();
            finishHour = (finish99) ? 99 : binding.timePickerFinish.getHour();
            finishMin = binding.timePickerFinish.getMinute();
        } else {
            updateOneAlert();
        }

        if (agenda) {
            updateAgenda();
        } else {
            qT = new QuietTask(subject, startHour, startMin, finishHour, finishMin,
                    week, active, vibrate, sRepeatCount, fRepeatCount, sayDate);
            if (currIdx == -1)
                quietTasks.add(qT);
            else
                quietTasks.set(currIdx, qT);
        }
        new QuietTaskGetPut().put(quietTasks, context, "Add/Update");
    }

    private void updateOneAlert() {
        startHour =  Integer.parseInt(binding.numHH1.getText().toString()) * 10
                + Integer.parseInt(binding.numHH2.getText().toString());
        startMin =  Integer.parseInt(binding.numMM1.getText().toString()) * 10
                + Integer.parseInt(binding.numMM2.getText().toString());

        long nowTime = System.currentTimeMillis();
        long nextTime = CalculateNext.calc(false, startHour, startMin, week, 0);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(nowTime);
        int nowDays = c.get(Calendar.DAY_OF_YEAR);
        int nowHour = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        c.setTimeInMillis(nextTime);
        int nextDays = c.get(Calendar.DAY_OF_YEAR);
        int nextHour = startHour * 60 + startMin;
        if ((nextDays - nowDays) > 5 || (nextDays == nowDays) & (nowHour > nextHour)) {
            week = new boolean[7];
            int weekDay = c.get(Calendar.DAY_OF_WEEK);
            if (weekDay < 7)
                week[weekDay] = true;
            else
                week[0] = true;
        }
        finishHour = 99;
    }

    private void updateAgenda() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(qT.calStartDate);
        c.set(Calendar.HOUR_OF_DAY, startHour);
        c.set(Calendar.MINUTE, startMin);
        long startDate = c.getTimeInMillis();
        c.set(Calendar.HOUR_OF_DAY, finishHour);
        c.set(Calendar.MINUTE, finishMin);
        long finishDate = c.getTimeInMillis();
        QuietTask qAgenda = new QuietTask(subject, startDate, finishDate,
                qT.calId, qT.calName, qT.calDesc, qT.calLocation,
                true, vibrate, sRepeatCount, fRepeatCount, true);
        quietTasks.set(currIdx, qAgenda);
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
            finish();
            save_QuietTask();
            mainRecycleAdapter.notifyItemChanged(currIdx);
            Toast.makeText(this, qT.subject+ ((currIdx == -1)? " Added": " Saved"), Toast.LENGTH_SHORT).show();

        } else if (id == R.id.action_delete) {
            finish();
            quietTasks.remove(currIdx);
            mainRecycleAdapter.notifyItemRemoved(currIdx);
            new QuietTaskGetPut().put(quietTasks, context, "del "+ qT.subject);

        } else if (id == R.id.action_copy) {
            QuietTask qtNew = new QuietTask(qT.subject,
                    qT.startHour, qT.startMin + 1, qT.finishHour, qT.finishMin + 1,
                    qT.week, qT.active, qT.vibrate,
                    qT.sRepeatCount, qT.fRepeatCount, qT.sayDate);
            quietTasks.add(currIdx, qtNew);
            new QuietTaskGetPut().put(quietTasks, context, "copy "+ qT.subject);
            mainRecycleAdapter.notifyItemChanged(currIdx-1);
            mainRecycleAdapter.notifyItemChanged(currIdx);
            mainRecycleAdapter.notifyItemChanged(currIdx+1);
            finish();

        } else if (id == R.id.action_delete_multi) {
            finish();
            if (agenda) {
                int delId = qT.calId;
                for (int i = 0; i < quietTasks.size(); ) {
                    if (quietTasks.get(i).calId == delId) {
                        quietTasks.remove(i);
                        mainRecycleAdapter.notifyItemRemoved(i);
                    }
                    else
                        i++;
                }
            } else {
                quietTasks.remove(currIdx);
                mainRecycleAdapter.notifyItemRemoved(currIdx);
            }
            new QuietTaskGetPut().put(quietTasks, context, "del "+subject);
        }
        return false;
    }

    @Override
    protected void onPause() {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.hideSoftInputFromWindow(binding.etSubject.getWindowToken(), 0);
        super.onPause();
    }
}