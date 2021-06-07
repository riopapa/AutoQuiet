package com.urrecliner.autoquiet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.urrecliner.autoquiet.databinding.ActivityAddAgendaBinding;
import com.urrecliner.autoquiet.models.GCal;
import com.urrecliner.autoquiet.models.QuietTask;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.urrecliner.autoquiet.Vars.gCals;
import static com.urrecliner.autoquiet.Vars.mContext;
import static com.urrecliner.autoquiet.Vars.utils;

public class AddAgendaActivity extends AppCompatActivity {

    private int startHour, startMin, finishHour, finishMin;
    private int startRepeat = 1, finishRepeat = 1;
    private boolean vibrate = true;
    private QuietTask quietTask;
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
        utils.log("Activity AddAgenda ","index="+ currIdx);
        Toast.makeText(mContext, "Add Agenda "+ currIdx, Toast.LENGTH_LONG).show();
        show_OneAgenda();
    }
    SimpleDateFormat sdfDate = new SimpleDateFormat("MM-dd(EEE) ", Locale.getDefault());
    SimpleDateFormat sdfHourMin = new SimpleDateFormat(" HH:mm ", Locale.getDefault());

    void show_OneAgenda() {

        binding.aTitle.setText(gCal.title);
        String s = sdfDate.format(gCal.startTime)+ sdfHourMin.format(gCal.startTime)
                +" ~ "+ sdfHourMin.format(gCal.finishTime);
        binding.aDate.setText(s);
        binding.aLocation.setText(gCal.location);
        binding.aDesc.setText(gCal.desc);
        binding.aRepeat.setText((gCal.repeat)? gCal.rule:"");

        binding.avVibrate.setImageResource((vibrate)? R.mipmap.phone_vibrate :R.mipmap.phone_quiet);
        binding.avVibrate.setOnClickListener(v -> {
            vibrate ^= true;
            binding.avVibrate.setImageResource((vibrate)? R.mipmap.phone_vibrate :R.mipmap.phone_quiet);
            v.invalidate();
        });

        binding.aStartRepeat.setImageResource((startRepeat == 0)? R.mipmap.speaking_off: (startRepeat == 1)? R.mipmap.speaking_on : R.mipmap.speak_repeat);
        binding.aStartRepeat.setOnClickListener(v -> {
            if (startRepeat == 0)
                startRepeat = 1;
            else if (startRepeat == 1)
                startRepeat = 11;
            else
                startRepeat = 0;
            binding.aStartRepeat.setImageResource((startRepeat == 0)? R.mipmap.speaking_off: (startRepeat == 1)? R.mipmap.speaking_on : R.mipmap.speak_repeat);
            v.invalidate();
        });

        binding.aFinishRepeat.setImageResource((finishRepeat == 0)? R.mipmap.speaking_off: (finishRepeat == 1)? R.mipmap.speaking_on : R.mipmap.speak_repeat);
        binding.aFinishRepeat.setOnClickListener(v -> {
            if (finishRepeat == 0)
                finishRepeat = 1;
            else if (finishRepeat == 1)
                finishRepeat = 11;
            else
                finishRepeat = 0;
            binding.aFinishRepeat.setImageResource((finishRepeat == 0)? R.mipmap.speaking_off: (finishRepeat == 1)? R.mipmap.speaking_on : R.mipmap.speak_repeat);
            v.invalidate();
        });
    }

    @Override
    protected void onPause() {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.hideSoftInputFromWindow(binding.aTitle.getWindowToken(), 0);
        super.onPause();
    }
}
