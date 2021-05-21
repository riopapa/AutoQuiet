package com.urrecliner.letmequiet;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.urrecliner.letmequiet.databinding.ActivitySettingBinding;

import static com.urrecliner.letmequiet.Vars.beepManner;
import static com.urrecliner.letmequiet.Vars.default_Duration;
import static com.urrecliner.letmequiet.Vars.editor;
import static com.urrecliner.letmequiet.Vars.interval_Long;
import static com.urrecliner.letmequiet.Vars.interval_Short;
import static com.urrecliner.letmequiet.Vars.sharedPref;
import static com.urrecliner.letmequiet.Vars.utils;

public class SettingActivity extends AppCompatActivity {

    private ActivitySettingBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        showSoundSetting();
    }

    public void adjustSetting(View v) {
        editor = sharedPref.edit();
        switch (v.getId()) {
            case R.id.setIntervalShortMinus:
                if (interval_Short > 1) {
                    interval_Short -= 1;
                    editor.putInt("interval_Short", interval_Short).apply();
                }
                break;
            case R.id.setIntervalShortPlus:
                interval_Short += 1;
                editor.putInt("interval_Short", interval_Short).apply();
                break;
            case R.id.setIntervalLongMinus:
                if (interval_Long > 20) {
                    interval_Long -= 10;
                    editor.putInt("interval_Long", interval_Long).apply();
                }
                break;
            case R.id.setIntervalLongPlus:
                interval_Long += 10;
                editor.putInt("interval_Long", interval_Long).apply();
                break;
            case R.id.setDefaultDurationMinus:
                if (default_Duration > 20) {
                    default_Duration -= 10;
                    editor.putInt("default_Duration", default_Duration).apply();
                }
                break;
            case R.id.setDefaultDurationPlus:
                default_Duration += 10;
                editor.putInt("default_Duration", default_Duration).apply();
                break;
            case R.id.setSound:
                beepManner ^= true;
                editor.putBoolean("beepManner", beepManner).apply();
                break;
            default:
                utils.log("setting","click ID : "+v.getId());
        }
        showSoundSetting();
    }
    private void showSoundSetting() {
        String text;
        text = (beepManner) ? "끔" : "켬"; binding.setSound.setText(text);
        text = getString(R.string.sound_when_manner_changed) + ((beepManner) ? "남" : "안 남"); binding.setSoundText.setText(text);
        text = ""+interval_Short; binding.setIntervalShort.setText(text);
        text = ""+interval_Long; binding.setIntervalLong.setText(text);
        text = ""+default_Duration; binding.setDefaultDuration.setText(text);
    }
}
