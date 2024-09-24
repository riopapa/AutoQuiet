package better.life.autoquiet.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import better.life.autoquiet.R;
import better.life.autoquiet.Sub.VerticalSpacingItemDecorator;
import better.life.autoquiet.adapter.GCalRecycleAdapter;

public class ActivityGCalShow extends AppCompatActivity  {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gcal);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        RecyclerView agendaRecyclerView = this.findViewById(R.id.gCalRecycler);
        LinearLayoutManager gCalLinearLayoutManager = new LinearLayoutManager(this);
        agendaRecyclerView.setLayoutManager(gCalLinearLayoutManager);
        VerticalSpacingItemDecorator gCalItemDecorator = new VerticalSpacingItemDecorator(10);
        agendaRecyclerView.addItemDecoration(gCalItemDecorator);
        GCalRecycleAdapter gCalRecyclerViewAdapter = new GCalRecycleAdapter();
        agendaRecyclerView.setAdapter(gCalRecyclerViewAdapter);
    }
}