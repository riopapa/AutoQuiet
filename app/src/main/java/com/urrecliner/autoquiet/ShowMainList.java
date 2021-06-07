package com.urrecliner.autoquiet;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.urrecliner.autoquiet.utility.MyItemTouchHelper;
import com.urrecliner.autoquiet.utility.VerticalSpacingItemDecorator;

import static com.urrecliner.autoquiet.Vars.mActivity;
import static com.urrecliner.autoquiet.Vars.mContext;
import static com.urrecliner.autoquiet.Vars.mainRecycleViewAdapter;

public class ShowMainList {
    public ShowMainList() {
        RecyclerView mainRecyclerView;

        mainRecyclerView = mActivity.findViewById(R.id.mainRecycler);
        LinearLayoutManager mainLinearLayoutManager = new LinearLayoutManager(mContext);
        mainRecyclerView.setLayoutManager(mainLinearLayoutManager);

        VerticalSpacingItemDecorator mainItemDecorator = new VerticalSpacingItemDecorator(14);
        mainRecyclerView.addItemDecoration(mainItemDecorator);

        mainRecycleViewAdapter = new MainRecycleViewAdapter();
        ItemTouchHelper.Callback mainCallback = new MyItemTouchHelper(mainRecycleViewAdapter, mContext);
        ItemTouchHelper mainItemTouchHelper = new ItemTouchHelper(mainCallback);
        mainRecycleViewAdapter.setTouchHelper(mainItemTouchHelper);
        mainItemTouchHelper.attachToRecyclerView(mainRecyclerView);
        mainRecyclerView.setAdapter(mainRecycleViewAdapter);
    }
}
