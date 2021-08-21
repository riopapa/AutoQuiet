package com.urrecliner.autoquiet;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import static com.urrecliner.autoquiet.Vars.sharedManner;
import static com.urrecliner.autoquiet.Vars.sharedTimeAfter;
import static com.urrecliner.autoquiet.Vars.sharedTimeBefore;
import static com.urrecliner.autoquiet.Vars.sharedTimeInit;
import static com.urrecliner.autoquiet.Vars.sharedTimeLong;
import static com.urrecliner.autoquiet.Vars.sharedTimeShort;
import static com.urrecliner.autoquiet.Vars.utils;

public class PreferActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new PreferFragment())
                    .commit();
        }
    }

    @Override
    protected void onDestroy() {
        utils.getPreference();
        super.onDestroy();
    }

    public static class PreferFragment extends PreferenceFragmentCompat  {

        final String TIME_SHORT = "timeShort", TIME_LONG = "timeLong", TIME_INIT = "timeInit",
                MANNER_BEEP = "manner_beep", TIME_BEFORE = "timeBefore", TIME_AFTER = "timeAfter";
        final String fTimeInit = "초기 바로 조용히 시간은  %s(분) 입니다";
        final String fTimeLong = "긴 변경 단위는 %s(분) 입니다";
        final String fTimeShort = "짧은 변경 단위 %s(분) 입니다";
        final String fTimeBefore = "일정 시작하는 시각 %s분 에 알립니다";
        final String fTimeAfter = "일정 끝나는 시각 %s분 에 알립니다";

        Preference pTimeShort, pTimeLong, pMannerBeep, pTimeInit, pTimeBefore, pTimeAfter;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_set, rootKey);

            pTimeShort = findPreference(TIME_SHORT);
            assert pTimeShort != null;
            pTimeShort.setSummary(String.format(fTimeShort, sharedTimeShort));
            pTimeShort.setOnPreferenceChangeListener((preference, newValue) -> {
                sharedTimeShort = newValue.toString();
                pTimeShort.setSummary(String.format(fTimeShort, sharedTimeShort));
                return true;
            });

            pTimeLong = findPreference(TIME_LONG);
            assert pTimeLong != null;
            pTimeLong.setSummary(String.format(fTimeShort, sharedTimeLong));
            pTimeLong.setOnPreferenceChangeListener((preference, newValue) -> {
                sharedTimeLong = newValue.toString();
                pTimeLong.setSummary(String.format(fTimeLong, sharedTimeLong));
                return true;
            });

            pMannerBeep = findPreference(MANNER_BEEP);
            assert pMannerBeep != null;
            pMannerBeep.setSummary(sharedManner ? "[조용히 하기]가 끝나면 소리가 남 " : "조용히 하기가 끝나도 소라기 나지 않음");
            pMannerBeep.setOnPreferenceChangeListener((preference, newValue) -> {
                sharedManner = Boolean.parseBoolean(newValue.toString());
                pMannerBeep.setSummary(sharedManner ? "[조용히 하기]가 끝나면 소리가 남 " : "조용히 하기가 끝나도 소라기 나지 않음");
                return true;
            });

            pTimeInit = findPreference(TIME_INIT);
            assert pTimeInit != null;
            pTimeInit.setSummary(String.format(fTimeInit, sharedTimeInit));
            pTimeInit.setOnPreferenceChangeListener((preference, newValue) -> {
                sharedTimeInit = newValue.toString();
                pTimeInit.setSummary(String.format(fTimeInit, sharedTimeInit));
                return true;
            });

            pTimeBefore = findPreference(TIME_BEFORE);
            assert pTimeBefore != null;
            pTimeBefore.setSummary(String.format(fTimeBefore, sharedTimeBefore));
            pTimeBefore.setOnPreferenceChangeListener((preference, newValue) -> {
                sharedTimeBefore = newValue.toString();
                pTimeBefore.setSummary(String.format(fTimeBefore, sharedTimeBefore));
                return true;
            });

            pTimeAfter = findPreference(TIME_AFTER);
            assert pTimeAfter != null;
            pTimeAfter.setSummary(String.format(fTimeAfter, sharedTimeAfter));
            pTimeAfter.setOnPreferenceChangeListener((preference, newValue) -> {
                sharedTimeAfter = newValue.toString();
                pTimeAfter.setSummary(String.format(fTimeAfter, sharedTimeAfter));
                return true;
            });
        }
    }
}
