package com.urrecliner.autoquiet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.urrecliner.autoquiet.databinding.ActivityAddAgendaBinding;
import com.urrecliner.autoquiet.models.GCal;
import com.urrecliner.autoquiet.models.QuietTask;
import com.urrecliner.autoquiet.utility.NameColor;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.urrecliner.autoquiet.Vars.gCals;
import static com.urrecliner.autoquiet.Vars.mContext;
import static com.urrecliner.autoquiet.Vars.quietTasks;
import static com.urrecliner.autoquiet.Vars.sharedTimeAfter;
import static com.urrecliner.autoquiet.Vars.sharedTimeBefore;
import static com.urrecliner.autoquiet.Vars.utils;

public class AddAgendaActivity extends AppCompatActivity {

    private int sRepeatTime = 1, fRepeatTime = 1;
    private boolean vibrate = true;
    private GCal gCal;
    private ActivityAddAgendaBinding binding;
    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityAddAgendaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        int currIdx = intent.getExtras().getInt("idx", -1);
        gCal = gCals.get(currIdx);
        show_OneAgenda();
    }

    void show_OneAgenda() {

        SimpleDateFormat sdfDate = new SimpleDateFormat("MM-dd(EEE) ", Locale.getDefault());
        SimpleDateFormat sdfHourMin = new SimpleDateFormat("HH:mm ", Locale.getDefault());
        binding.aTitle.setText(gCal.title);
        mTitle = gCal.title;

        binding.aTitle.setBackgroundColor(NameColor.get(gCal.calName, mContext));
        String s = sdfDate.format(gCal.startTime)+ sdfHourMin.format(gCal.startTime)
                +" ~ "+ sdfHourMin.format(gCal.finishTime);
        binding.aDate.setText(s);
        binding.acalName.setText(gCal.calName);
        binding.acalName.setBackgroundColor(NameColor.get(gCal.calName, mContext));
        binding.aLocation.setText(gCal.location);
        binding.aDesc.setText(gCal.desc);
        binding.aRepeat.setText((gCal.repeat)? gCal.rule:"");

        binding.avVibrate.setImageResource((vibrate)? R.drawable.phone_normal :R.drawable.phone_off);
        binding.avVibrate.setOnClickListener(v -> {
            vibrate ^= true;
            binding.avVibrate.setImageResource((vibrate)? R.drawable.phone_normal :R.drawable.phone_off);
            v.invalidate();
        });

        binding.aStartRepeat.setImageResource((sRepeatTime == 0)? R.drawable.speak_off: (sRepeatTime == 1)? R.drawable.speak_on : R.mipmap.speak_repeat);
        binding.aStartRepeat.setOnClickListener(v -> {
            if (sRepeatTime == 0)
                sRepeatTime = 1;
            else if (sRepeatTime == 1)
                sRepeatTime = 11;
            else
                sRepeatTime = 0;
            binding.aStartRepeat.setImageResource((sRepeatTime == 0)? R.drawable.speak_off: (sRepeatTime == 1)? R.drawable.speak_on : R.mipmap.speak_repeat);
            v.invalidate();
        });

        binding.aFinishRepeat.setImageResource((fRepeatTime == 0)? R.drawable.speak_off: (fRepeatTime == 1)? R.drawable.speak_on : R.mipmap.speak_repeat);
        binding.aFinishRepeat.setOnClickListener(v -> {
            if (fRepeatTime == 0)
                fRepeatTime = 1;
            else if (fRepeatTime == 1)
                fRepeatTime = 11;
            else
                fRepeatTime = 0;
            binding.aFinishRepeat.setImageResource((fRepeatTime == 0)? R.drawable.speak_off: (fRepeatTime == 1)? R.drawable.speak_on : R.mipmap.speak_repeat);
            v.invalidate();
        });

        String []dispName = new String[] { "10분전", "9분전", "8분전", "7분전", "6분전",
                "5분전",  "4분전", "3분전",  "2분전", "1분전",  " 정시 ", "1분후",  "2분후",  "3분후",  "4분후",
                "5분후", "6분후", "7분후", "8분후", "9분후", "10분후" };
        binding.aBefore.setMaxValue(20);
        binding.aBefore.setMinValue(0);
        binding.aBefore.setWrapSelectorWheel(false);
        binding.aBefore.setDisplayedValues(dispName);
        binding.aBefore.setValue(Integer.parseInt(sharedTimeBefore)+10);
        binding.aBefore.setTextSize(60);
        binding.aAfter.setMaxValue(20);
        binding.aAfter.setMinValue(0);
        binding.aAfter.setWrapSelectorWheel(false);
        binding.aAfter.setDisplayedValues(dispName);
        binding.aAfter.setValue(Integer.parseInt(sharedTimeAfter)+10);
        binding.aAfter.setTextSize(60);
        binding.aAdd.setOnClickListener(v -> {
            mTitle = binding.aTitle.getText().toString();
            int before = binding.aBefore.getValue() - 10;
            int after = binding.aAfter.getValue() - 10;
            addAgenda(gCal.id, before, after);
        });
    }

    @Override
    protected void onPause() {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.hideSoftInputFromWindow(binding.aTitle.getWindowToken(), 0);
        super.onPause();
    }

    void addAgenda(int id, int before, int after) {

        SimpleDateFormat sdfDateHour = new SimpleDateFormat("MM-dd(EEE) HH:mm", Locale.getDefault());
        StringBuilder sb  = new StringBuilder();
        sb.append("Following Tasks Added");
        int qId = (int) (gCal.startTime & 0x7ffffff);
        for (int i = 0; i < gCals.size(); i++) {    // if repeat item add all
            if (gCals.get(i).id == id) {
                GCal gC = gCals.get(i);
                QuietTask q = new QuietTask(mTitle, gC.startTime + (long) before * 60 * 1000, gC.finishTime + (long) after * 60 * 1000,
                        qId, gC.calName, gC.desc, gC.location, true, vibrate, sRepeatTime, fRepeatTime, gC.repeat);
                sb.append("\n").append(q.subject).append(" ").append(sdfDateHour.format(q.calStartDate));
                quietTasks.add(q);
            }
        }
        Toast.makeText(mContext, sb, Toast.LENGTH_LONG).show();
        utils.saveQuietTasksToShared();
        finish();
    }
}