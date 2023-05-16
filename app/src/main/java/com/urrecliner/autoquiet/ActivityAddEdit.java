package com.urrecliner.autoquiet;

import static com.urrecliner.autoquiet.ActivityMain.mainRecycleAdapter;
import static com.urrecliner.autoquiet.ActivityMain.vars;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
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
    private int begHour, begMin, endHour, endMin, sHour;
    private boolean active, end99, am;
    private int begLoop, endLoop;
    private boolean[] week = new boolean[7];
    private final TextView[] weekView = new TextView[7];
    private boolean vibrate, agenda, sayDate;
    private QuietTask qT;
    private ArrayList<QuietTask> quietTasks;
    private int currIdx;
    private ActivityAddEditBinding binding;
    private int colorOn, colorOnBack, colorOffBack, BGColorOn, BGColorOff;
    private Context context;
    private int xSize, numPos;
    final String[] weekName = {"주", "월", "화", "수", "목", "금", "토"};

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

        subject = qT.subject;
        begHour = qT.begHour;
        begMin = qT.begMin;
        endHour = qT.endHour;
        endMin = qT.endMin;
        end99 = qT.endHour == 99;
        active = qT.active;
        begLoop = qT.begLoop;
        endLoop = qT.endLoop;
        sayDate = qT.sayDate;
        week = qT.week;
        vibrate = qT.vibrate;
        agenda = qT.agenda;
        sHour = begHour;
        am = begHour < 12;
        if (begHour > 12)
            sHour = begHour - 12;

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
            if (numPos > 1)
                numPos--;
            else
                numPos = 4;
            set_TimeForm();
        });
        findViewById(R.id.numFore).setOnClickListener(view -> {
            if (numPos < 4)
                numPos++;
            else
                numPos = 1;
            set_TimeForm();
        });

        binding.gCal.setImageResource((agenda)? R.drawable.calendar:R.drawable.transperent);
        binding.timePickerBeg.setIs24HourView(true);
        binding.timePickerEnd.setIs24HourView(true);
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

        binding.end99.setOnClickListener(v -> {
            end99 = !end99;
            binding.end99.setChecked(end99);
            numPos = 1;
            set_TimeForm();
            show_Info();
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

        binding.tvBegLoop.setOnClickListener(v -> {
            if (begLoop == 0)
                begLoop = 1;
            else if (begLoop == 1)
                begLoop = 9;
            else
                begLoop = 0;
            binding.ivBegLoop.setImageResource((begLoop == 0)? R.drawable.speak_off: (begLoop == 1)? R.drawable.alert_bell : R.drawable.speak_on);
            v.invalidate();
            show_Info();
        });

        binding.ivBegLoop.setImageResource((begLoop == 0)? R.drawable.speak_off: (begLoop == 1)? R.drawable.alert_bell : R.drawable.speak_on);
        binding.ivBegLoop.setOnClickListener(v -> {
            if (begLoop == 0)
                begLoop = 1;
            else if (begLoop == 1)
                begLoop = 11;
            else
                begLoop = 0;
            binding.ivBegLoop.setImageResource((begLoop == 0)? R.drawable.speak_off: (begLoop == 1)? R.drawable.alert_bell : R.drawable.speak_on);
            v.invalidate();
            show_Info();
        });

        binding.tvEndLoop.setOnClickListener(v -> {
            if (endLoop == 0)
                endLoop = 1;
            else if (endLoop == 1)
                endLoop = 11;
            else
                endLoop = 0;
            binding.ivEndLoop.setImageResource((endLoop == 0)? R.drawable.speak_off: (endLoop == 1)? R.drawable.alert_bell : R.drawable.speak_on);
            v.invalidate();
            show_Info();
        });
        binding.ivEndLoop.setImageResource((endLoop == 0)? R.drawable.speak_off: (endLoop == 1)? R.drawable.alert_bell : R.drawable.speak_on);
        binding.ivEndLoop.setOnClickListener(v -> {
            if (endLoop == 0)
                endLoop = 1;
            else if (endLoop == 1)
                endLoop = 11;
            else
                endLoop = 0;
            binding.ivEndLoop.setImageResource((endLoop == 0)? R.drawable.speak_off: (endLoop == 1)? R.drawable.alert_bell : R.drawable.speak_on);
            v.invalidate();
            show_Info();
        });
        binding.sayDate.setChecked(sayDate);    // sayDate is only for end Time
        binding.sayDate.setOnClickListener(v -> {
            sayDate = !sayDate;
            binding.sayDate.setChecked(sayDate);
            v.invalidate();
        });

        if (agenda) {
            SimpleDateFormat sdfDate = new SimpleDateFormat("MM-dd(EEE)", Locale.getDefault());
            String s = sdfDate.format(qT.calBegDate);
            if (!qT.calLocation.equals(""))
                s += "\n" + qT.calLocation;
            if (!qT.calDesc.equals(""))
                s += "\n" + qT.calDesc;
            binding.dateDesc.setText(s);
        } else
            show_Info();

        binding.amPm.setOnClickListener(v -> {
            am = !am;
            show_ResultTime();
        });

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

    private void show_Info() {
            /*
        loop    begLoop     endLoop     action
        0                   0           beep only
        0                   1           say task ended
        1-3                 1           say task once
        1-3                 11          say task info loop times
     */
        String s = "";
        if (end99) {
            if (endLoop == 1)
                s = "한 번만 알려주고 비활성화됨";
            else if (endLoop > 1)
                s = "여러번 알려줌";
            else
                s = "삐이 소리만 남";
        }
        binding.dateDesc.setText(s);
    }
    private void set_TimeForm() {
        binding.end99.setChecked(end99);
        if (!end99) {    // normal beg, end
            if (endHour == 99)
                endHour = begHour;
            binding.timePickerBeg.setVisibility(View.VISIBLE);
            binding.timePickerEnd.setVisibility(View.VISIBLE);
            binding.timePickerBeg.setHour(begHour);
            binding.timePickerBeg.setMinute(begMin);
            binding.numDateTime.setVisibility(View.GONE);
            binding.timePickerEnd.setHour(endHour);
            binding.timePickerEnd.setMinute(endMin);
            binding.timePickerEnd.setHour(endHour); binding.timePickerEnd.setMinute(endMin);
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
            binding.timePickerBeg.setVisibility(View.GONE);
            binding.timePickerEnd.setVisibility(View.GONE);
            binding.iVVibrate.setImageResource(R.drawable.alarm);
            show_ResultTime();
        }
    }

    private void show_ResultTime() {
        binding.amPm.setText(am ? "오전":"오후");
        String s = (sHour > 9) ? ""+sHour: "0"+sHour;
        String s1 = s.substring(0,1); String s2 = s.substring(1);
        binding.numHH1.setText(s1); binding.numHH1.setBackgroundColor(0x00bbbbbb);
        binding.numHH2.setText(s2); binding.numHH2.setBackgroundColor(0x00bbbbbb);
        s = (begMin > 9) ? ""+ begMin : "0"+ begMin;
        s1 = s.substring(0,1); s2 = s.substring(1);
        binding.numMM1.setText(s1); binding.numMM1.setBackgroundColor(0x00bbbbbb);
        binding.numMM2.setText(s2); binding.numMM2.setBackgroundColor(0x00bbbbbb);

        TextView tv;
        Animation anim = new AlphaAnimation(0.2f, 1.0f);
        anim.setDuration(160);
        anim.setStartOffset(160);
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
            if (num > 1) {
                sHour = num;
                numPos = 2;
            } else
                sHour = num * 10 + Integer.parseInt(binding.numHH2.getText().toString());
        } else if (numPos == 2) {
            sHour = Integer.parseInt(binding.numHH1.getText().toString()) * 10 + num;
        } else if (numPos == 3) {
            int min = num * 10 + Integer.parseInt(binding.numMM2.getText().toString());
            if (min < 60)
                begMin = min;
        } else {
            begMin = Integer.parseInt(binding.numMM1.getText().toString()) * 10 + num;
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
        if (!end99) {
            begHour = binding.timePickerBeg.getHour();
            begMin = binding.timePickerBeg.getMinute();
            endHour = (end99) ? 99 : binding.timePickerEnd.getHour();
            endMin = binding.timePickerEnd.getMinute();
        } else {
            save_AlarmTask();
        }

        if (agenda) {
            save_AgendaTask();
        } else {
            qT = new QuietTask(subject, begHour, begMin, endHour, endMin,
                    week, active, vibrate, begLoop, endLoop, sayDate);
            if (currIdx == -1)
                quietTasks.add(qT);
            else
                quietTasks.set(currIdx, qT);
        }
        new QuietTaskGetPut().put(quietTasks);
    }

    private void save_AlarmTask() {
        begHour =  Integer.parseInt(binding.numHH1.getText().toString()) * 10
                + Integer.parseInt(binding.numHH2.getText().toString());
        if (!am && begHour < 12)
            begHour += 12;
        begMin =  Integer.parseInt(binding.numMM1.getText().toString()) * 10
                + Integer.parseInt(binding.numMM2.getText().toString());

        long nowTime = System.currentTimeMillis();
        long nextTime = CalculateNext.calc(false, begHour, begMin, week, 0);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(nowTime);
        int nowDays = c.get(Calendar.DAY_OF_YEAR);
        int nowHour = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        c.setTimeInMillis(nextTime);
        int nextDays = c.get(Calendar.DAY_OF_YEAR);
        int nextHour = begHour * 60 + begMin;
        if ((nextDays - nowDays) > 5 || (nextDays == nowDays) & (nowHour > nextHour)) {
            week = new boolean[7];
            int weekDay = c.get(Calendar.DAY_OF_WEEK);
            if (weekDay > 6)
                weekDay = 0;
            week[weekDay] = true;
            Toast.makeText(context, "요일을 "+weekName[weekDay]+" 로 바꿈",Toast.LENGTH_SHORT).show();
        }
        endHour = 99;
    }

    private void save_AgendaTask() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(qT.calBegDate);
        c.set(Calendar.HOUR_OF_DAY, begHour);
        c.set(Calendar.MINUTE, begMin);
        long begDate = c.getTimeInMillis();
        c.set(Calendar.HOUR_OF_DAY, endHour);
        c.set(Calendar.MINUTE, endMin);
        long endDate = c.getTimeInMillis();
        QuietTask qAgenda = new QuietTask(subject, begDate, endDate,
                qT.calId, qT.calName, qT.calDesc, qT.calLocation,
                true, vibrate, begLoop, endLoop, true);
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
            new QuietTaskGetPut().put(quietTasks);

        } else if (id == R.id.action_copy) {
            QuietTask qtNew = new QuietTask(qT.subject,
                    qT.begHour, qT.begMin + 1, qT.endHour, qT.endMin + 1,
                    qT.week, qT.active, qT.vibrate,
                    qT.begLoop, qT.endLoop, qT.sayDate);
            quietTasks.add(currIdx, qtNew);
            new QuietTaskGetPut().put(quietTasks);
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
            new QuietTaskGetPut().put(quietTasks);
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