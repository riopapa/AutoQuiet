package com.urrecliner.autoquiet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.urrecliner.autoquiet.databinding.ActivityAddAgendaBinding;
import com.urrecliner.autoquiet.models.GCal;
import com.urrecliner.autoquiet.models.QuietTask;
import com.urrecliner.autoquiet.utility.NameColor;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.jar.Attributes;

import static com.urrecliner.autoquiet.Vars.gCals;
import static com.urrecliner.autoquiet.Vars.mContext;
import static com.urrecliner.autoquiet.Vars.quietTasks;
import static com.urrecliner.autoquiet.Vars.sharedTimeBefore;
import static com.urrecliner.autoquiet.Vars.utils;

public class AddAgendaActivity extends AppCompatActivity {

    private int sRepeatTime = 1, fRepeatTime = 1;
    private boolean vibrate = true;
    private GCal gCal;
    private ActivityAddAgendaBinding binding;

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
    SimpleDateFormat sdfDate = new SimpleDateFormat("MM-dd(EEE) ", Locale.getDefault());
    SimpleDateFormat sdfHourMin = new SimpleDateFormat("HH:mm ", Locale.getDefault());

    void show_OneAgenda() {

        binding.aTitle.setText(gCal.title);
        binding.aTitle.setBackgroundColor(NameColor.get(gCal.calName));
        String s = sdfDate.format(gCal.startTime)+ sdfHourMin.format(gCal.startTime)
                +" ~ "+ sdfHourMin.format(gCal.finishTime);
        binding.aDate.setText(s);
        binding.acalName.setText(gCal.calName);
        binding.acalName.setBackgroundColor(NameColor.get(gCal.calName));
        binding.aLocation.setText(gCal.location);
        binding.aDesc.setText(gCal.desc);
        binding.aRepeat.setText((gCal.repeat)? gCal.rule:"");

        binding.avVibrate.setImageResource((vibrate)? R.mipmap.phone_vibrate :R.mipmap.phone_quiet);
        binding.avVibrate.setOnClickListener(v -> {
            vibrate ^= true;
            binding.avVibrate.setImageResource((vibrate)? R.mipmap.phone_vibrate :R.mipmap.phone_quiet);
            v.invalidate();
        });

        binding.aStartRepeat.setImageResource((sRepeatTime == 0)? R.mipmap.speaking_off: (sRepeatTime == 1)? R.mipmap.speaking_on : R.mipmap.speak_repeat);
        binding.aStartRepeat.setOnClickListener(v -> {
            if (sRepeatTime == 0)
                sRepeatTime = 1;
            else if (sRepeatTime == 1)
                sRepeatTime = 11;
            else
                sRepeatTime = 0;
            binding.aStartRepeat.setImageResource((sRepeatTime == 0)? R.mipmap.speaking_off: (sRepeatTime == 1)? R.mipmap.speaking_on : R.mipmap.speak_repeat);
            v.invalidate();
        });

        binding.aFinishRepeat.setImageResource((fRepeatTime == 0)? R.mipmap.speaking_off: (fRepeatTime == 1)? R.mipmap.speaking_on : R.mipmap.speak_repeat);
        binding.aFinishRepeat.setOnClickListener(v -> {
            if (fRepeatTime == 0)
                fRepeatTime = 1;
            else if (fRepeatTime == 1)
                fRepeatTime = 11;
            else
                fRepeatTime = 0;
            binding.aFinishRepeat.setImageResource((fRepeatTime == 0)? R.mipmap.speaking_off: (fRepeatTime == 1)? R.mipmap.speaking_on : R.mipmap.speak_repeat);
            v.invalidate();
        });

        binding.aBefore.setText(sharedTimeBefore);

        binding.aAdd.setOnClickListener(v -> {
            EditText et = findViewById(R.id.aBefore);
            addAgenda(gCal.id, Integer.parseInt(et.getText().toString()));
        });
    }

    @Override
    protected void onPause() {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.hideSoftInputFromWindow(binding.aTitle.getWindowToken(), 0);
        super.onPause();
    }

    void addAgenda(int id, int before) {

        int count = 0;
        int newId = (int) (System.currentTimeMillis() & 0xFFFFFFFF);
        for (int i = 0; i < gCals.size(); i++) {    // if repeat item add all
            if (gCals.get(i).id == id) {
                GCal g = gCals.get(i);
                QuietTask q = new QuietTask(g.title, g.startTime - before * 60 * 1000, g.finishTime,
                        newId, g.calName, g.desc, g.location, true, vibrate, sRepeatTime, fRepeatTime, g.repeat);
                quietTasks.add(q);
                count++;
            }
        }
        Toast.makeText(mContext,"Total "+count+" agenda Added",Toast.LENGTH_LONG).show();
        utils.saveQuietTasksToShared();
        finish();
    }
}
