package better.life.autoquiet.activity;

import static better.life.autoquiet.activity.ActivityMain.currIdx;
import static better.life.autoquiet.activity.ActivityMain.mainRecycleAdapter;
import static better.life.autoquiet.activity.ActivityMain.quietTasks;
import static better.life.autoquiet.activity.ActivityMain.vars;

import android.app.Dialog;
import android.content.Context;
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

import better.life.autoquiet.quiettask.QuietTaskGetPut;
import better.life.autoquiet.R;
import better.life.autoquiet.calendar.CalcNextBegEnd;
import better.life.autoquiet.Sub.NameColor;
import better.life.autoquiet.quiettask.QuietTaskDefault;
import better.life.autoquiet.databinding.ActivityAddEditBinding;
import better.life.autoquiet.models.QuietTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ActivityAddEdit extends AppCompatActivity {

    private String subject;
    private int begHour, begMin, endHour, endMin, sHour;
    private boolean active, end99, am, vibrate;
    private int alarmType;
    private boolean[] week = new boolean[7];
    private final TextView[] weekView = new TextView[7];
    private boolean agenda, sayDate;
    private QuietTask qT;
    private ActivityAddEditBinding binding;
    private int colorOn, colorOnBack, colorOffBack, BGColorOn, BGColorOff;
    private Context context;
    private int xSize, numPos;
    private Dialog dialog;
    final String[] weekName = {"주", "월", "화", "수", "목", "금", "토"};

    final int[] alarmTypeNames = { 0,
        R.string.bell_several_time,
        R.string.bell_once_a_week,
        R.string.bell_one_time,
        R.string.phone_vibrate,
        R.string.phone_work,
        R.string.phone_off
    };
    public final static int[] alarmIcons = { 0,
            R.drawable.bell_several,
            R.drawable.bell_weekly,
            R.drawable.bell_onetime,
            R.drawable.phone_vibrate,
            R.drawable.phone_work,
            R.drawable.phone_off
    };

    public final static int BELL_SEVERAL = 1;
    public final static int BELL_WEEKLY = 2;
    public final static int BELL_ONETIME = 3;

    public final static int PHONE_VIBRATE = 4;
    public final static int PHONE_WORK = 5;
    public final static int PHONE_OFF = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        context = this;
        binding = ActivityAddEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        quietTasks = new QuietTaskGetPut().get(this);
        if (currIdx == -1)
            qT = new QuietTaskDefault().get();
        else
            qT = quietTasks.get(currIdx);
        qT.active = true;
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        xSize = metrics.widthPixels / 9;
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle((vars.addNewQuiet) ? R.string.add_table :R.string.update_table);
        actionBar.setIcon(R.drawable.auto_quite);
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
        TextView alarm_Type = findViewById(R.id.typeDesc);
        alarm_Type.setOnClickListener(v -> dialog.show());
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.alarm_case);

        int width = getResources().getDisplayMetrics().widthPixels * 9 / 10;
        int height = getResources().getDisplayMetrics().heightPixels*2/3;
        dialog.getWindow().setLayout(width, height);

    }

    public void alarmType_Selected(View view) {
        int checkedId = view.getId();
        if (checkedId == R.id.radio_bell_subject_several)
            alarmType = BELL_SEVERAL;
        else if (checkedId == R.id.radio_bell_event)
            alarmType = BELL_WEEKLY;
        else if (checkedId == R.id.radio_bee_one_time)
            alarmType = BELL_ONETIME;
        else if (checkedId == R.id.radio_vibrate_start_end)
            alarmType = PHONE_VIBRATE;
        else if (checkedId == R.id.radio_work_vibrate)
            alarmType = PHONE_WORK;
        else
            alarmType = PHONE_OFF;
        dialog.dismiss();
        end99 = alarmType < PHONE_VIBRATE;

        showTimeForm();
    }

    void build_QuietTask() {

        subject = qT.subject;
        begHour = qT.begHour;
        begMin = qT.begMin;
        endHour = qT.endHour;
        endMin = qT.endMin;
        end99 = qT.endHour == 99;
        active = true;  // let it active
        alarmType = qT.alarmType;
        sayDate = qT.sayDate;
        week = qT.week;
        agenda = qT.agenda;
        sHour = begHour;
        am = begHour < 12;
        if (begHour > 12)
            sHour = begHour - 12;
        vibrate = qT.vibrate;

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
            showTimeForm();
        });
        findViewById(R.id.numFore).setOnClickListener(view -> {
            if (numPos < 4)
                numPos++;
            else
                numPos = 1;
            showTimeForm();
        });

        binding.timePickerBeg.setIs24HourView(true);
        binding.timePickerEnd.setIs24HourView(true);
        numPos = 1;
        showTimeForm();

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

        binding.sayDate.setChecked(sayDate);    // sayDate is only for end Time
        binding.sayDate.setOnClickListener(v -> {
            sayDate = !sayDate;
            binding.sayDate.setChecked(sayDate);
            v.invalidate();
        });

        if (agenda) {
            SimpleDateFormat sdfDate = new SimpleDateFormat("MM-dd(EEE)", Locale.getDefault());
            String s = sdfDate.format(qT.calBegDate);
            if (!qT.calLocation.isEmpty())
                s += "\n" + qT.calLocation;
            if (!qT.calDesc.isEmpty())
                s += "\n" + qT.calDesc;
            binding.typeDesc.setText(s);
        } else
            showTimeForm();

        binding.amPm.setOnClickListener(v -> {
            am = !am;
            show_ResultTime();
        });

        binding.swVibrate.setChecked(vibrate);
        binding.swVibrate.setOnClickListener(v -> {
            vibrate = !vibrate;
            binding.swVibrate.setChecked(vibrate);
            v.invalidate();
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

    private void showTimeForm() {
        binding.typeDesc.setText(getResources().getString(alarmTypeNames[alarmType]));
        binding.alarmType.setImageResource((agenda)? R.drawable.calendar: alarmIcons[alarmType]);
        if (alarmType < PHONE_VIBRATE) { // end99
            end99 = true;
            endHour = 99;
        } else {
            end99 = false;
        }

        if (!end99) {    // normal beg, end
            binding.txtEnd.setVisibility(View.VISIBLE);
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

        } else {
            binding.txtEnd.setVisibility(View.GONE);
            binding.numDateTime.setVisibility(View.VISIBLE);
            binding.timePickerBeg.setVisibility(View.GONE);
            binding.timePickerEnd.setVisibility(View.GONE);
            show_ResultTime();
        }
    }

    private void show_ResultTime() {
        binding.amPm.setText(am ? "오전":"오후");
        String s = (sHour > 9) ? String.valueOf(sHour) : "0"+sHour;
        String s1 = s.substring(0,1); String s2 = s.substring(1);
        binding.numHH1.setText(s1); binding.numHH1.setBackgroundColor(0x00bbbbbb);
        binding.numHH2.setText(s2); binding.numHH2.setBackgroundColor(0x00bbbbbb);
        s = (begMin > 9) ? String.valueOf(begMin) : "0"+ begMin;
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
        if (alarmType == BELL_SEVERAL || alarmType == BELL_ONETIME) {
            for (int wk = 0; wk < 7; wk++) {
                week[wk] = i == wk;
                weekView[wk].setBackgroundColor((week[wk]) ? colorOnBack : colorOffBack);
                weekView[wk].setTypeface(null, (week[wk]) ? Typeface.BOLD : Typeface.NORMAL);
                weekView[wk].invalidate();
            }
        } else {
            week[i] ^= true;
            weekView[i].setBackgroundColor((week[i]) ? colorOnBack : colorOffBack);
            weekView[i].setTypeface(null, (week[i]) ? Typeface.BOLD : Typeface.NORMAL);
            v.invalidate();
        }
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
        showTimeForm();
    }

    private void save_QuietTask() {

        if (getScreen2Vars()) return;

        if (end99)
            save_AlarmTask();
        else if (agenda) {
            save_AgendaTask();
        } else {
            qT = new QuietTask(subject, begHour, begMin, endHour, endMin,
                    week, active, alarmType, sayDate);
            qT.vibrate = vibrate;

            if (currIdx == -1)
                quietTasks.add(qT);
            else
                quietTasks.set(currIdx, qT);
        }
        mainRecycleAdapter.sort();
    }

    private boolean getScreen2Vars() {
        int any = 0;
        for (int i = 0; i < 7; i++) {
            if (week[i]) any++;
        }
        if (any == 0) {
            Toast.makeText(getBaseContext(), R.string.at_least_one_day_selected, Toast.LENGTH_LONG).show();
            return true;
        }

        subject = binding.etSubject.getText().toString().trim();
        if (subject.isEmpty())
            subject = getString(R.string.no_subject);
        if (end99) {
            begHour =  Integer.parseInt(binding.numHH1.getText().toString()) * 10
                    + Integer.parseInt(binding.numHH2.getText().toString());
            if (!am && begHour < 12)
                begHour += 12;
            begMin =  Integer.parseInt(binding.numMM1.getText().toString()) * 10
                    + Integer.parseInt(binding.numMM2.getText().toString());
        } else {
            begHour = binding.timePickerBeg.getHour();
            begMin = binding.timePickerBeg.getMinute();
            endHour = binding.timePickerEnd.getHour();
            endMin = binding.timePickerEnd.getMinute();
        }
        return false;
    }

    private void save_AlarmTask() {

        qT = new QuietTask(subject, begHour, begMin, endHour, endMin,
                week, active, alarmType, sayDate);
        qT.vibrate = vibrate;

        CalcNextBegEnd calBE = new CalcNextBegEnd(qT);
        long nextTime = calBE.begTime;
        long nowTime = System.currentTimeMillis();

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(nowTime);
        int nowDays = c.get(Calendar.DAY_OF_YEAR);
        int nowHourMin = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        c.setTimeInMillis(nextTime);
        int nextDays = c.get(Calendar.DAY_OF_YEAR);
        int nextHourMin = begHour * 60 + begMin;
        if ((alarmType == BELL_SEVERAL  || alarmType == BELL_ONETIME) &&
                ((nextDays - nowDays) > 5 || (nextDays == nowDays) & (nowHourMin > nextHourMin))) {
            week = new boolean[7];
            int weekDay = c.get(Calendar.DAY_OF_WEEK);
            if (weekDay > 6)
                weekDay = 0;
            week[weekDay] = true;
            Toast.makeText(context, "요일을 "+weekName[weekDay]+" 로 바꿈",Toast.LENGTH_SHORT).show();
        }
        qT = new QuietTask(subject, begHour, begMin, endHour, endMin,
                week, active, alarmType, sayDate);
        qT.vibrate = vibrate;

        if (currIdx == -1)
            quietTasks.add(qT);
        else
            quietTasks.set(currIdx, qT);
        mainRecycleAdapter.notifyItemChanged(currIdx);
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
                true, 5, true); // 5: vibrate
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
            save_QuietTask();
            finish();
        } else if (id == R.id.action_delete) {
            finish();
            quietTasks.remove(currIdx);
            mainRecycleAdapter.notifyItemRemoved(currIdx);
            new QuietTaskGetPut().put(quietTasks);

        } else if (id == R.id.action_copy) {
            getScreen2Vars();
            QuietTask qtNew = new QuietTask(subject,
                    begHour, begMin, endHour, endMin,
                    week, active, alarmType, sayDate);
            qtNew.vibrate= vibrate;

            quietTasks.add(++currIdx, qtNew);
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