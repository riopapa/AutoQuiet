package com.urrecliner.autoquiet;

import static com.urrecliner.autoquiet.Vars.gCals;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.urrecliner.autoquiet.utility.GetAgenda;

import java.util.ArrayList;

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