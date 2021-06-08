package com.urrecliner.autoquiet;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.urrecliner.autoquiet.models.GCal;
import com.urrecliner.autoquiet.utility.GetAgenda;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import static com.urrecliner.autoquiet.Vars.gCals;
import static com.urrecliner.autoquiet.Vars.mContext;
import static com.urrecliner.autoquiet.Vars.quietTasks;
import static com.urrecliner.autoquiet.Vars.utils;

public class GCalShowActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gcal);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        gCals = new ArrayList<>();
        GetAgenda.get(this);
        gCals.sort((arg0, arg1) -> Long.compare(arg0.startTime, arg1.startTime));
        new ShowGCalList(this, this);
    }
}