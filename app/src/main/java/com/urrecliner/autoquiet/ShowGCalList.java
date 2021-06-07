package com.urrecliner.autoquiet;

import android.app.Activity;
import android.content.Context;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.urrecliner.autoquiet.utility.MyItemTouchHelper;
import com.urrecliner.autoquiet.utility.VerticalSpacingItemDecorator;

import static com.urrecliner.autoquiet.Vars.gCalRecyclerViewAdapter;
import static com.urrecliner.autoquiet.Vars.mActivity;
import static com.urrecliner.autoquiet.Vars.mContext;

public class ShowGCalList {
    public ShowGCalList(Context context, Activity activity) {
        RecyclerView agendaRecyclerView;

        agendaRecyclerView = activity.findViewById(R.id.gCalRecycler);
        LinearLayoutManager gCalLinearLayoutManager = new LinearLayoutManager(context);
        agendaRecyclerView.setLayoutManager(gCalLinearLayoutManager);

        VerticalSpacingItemDecorator gCalItemDecorator = new VerticalSpacingItemDecorator(10);
        agendaRecyclerView.addItemDecoration(gCalItemDecorator);

        gCalRecyclerViewAdapter = new GCalRecycleViewAdapter();
        agendaRecyclerView.setAdapter(gCalRecyclerViewAdapter);
    }
}
