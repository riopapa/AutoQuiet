package com.urrecliner.letmequiet;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.urrecliner.letmequiet.utility.MyItemTouchHelper;
import com.urrecliner.letmequiet.utility.VerticalSpacingItemDecorator;

import static com.urrecliner.letmequiet.Vars.calRecyclerViewAdapter;
import static com.urrecliner.letmequiet.Vars.mActivity;
import static com.urrecliner.letmequiet.Vars.mContext;
import static com.urrecliner.letmequiet.Vars.mainRecycleViewAdapter;

public class ShowList {
    public ShowList () {
        RecyclerView mainRecyclerView, calRecyclerView;

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
