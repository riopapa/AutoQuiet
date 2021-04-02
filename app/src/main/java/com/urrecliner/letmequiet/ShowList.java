package com.urrecliner.letmequiet;

import android.app.Activity;
import android.content.Context;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.urrecliner.letmequiet.utility.MyItemTouchHelper;
import com.urrecliner.letmequiet.utility.VerticalSpacingItemDecorator;

import static com.urrecliner.letmequiet.Vars.mActivity;
import static com.urrecliner.letmequiet.Vars.mContext;
import static com.urrecliner.letmequiet.Vars.recycleViewAdapter;

public class ShowList {
    public ShowList () {
        RecyclerView recyclerView;

        recyclerView = mActivity.findViewById(R.id.mainRecycler);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(mLinearLayoutManager);

        VerticalSpacingItemDecorator itemDecorator = new VerticalSpacingItemDecorator(14);
        recyclerView.addItemDecoration(itemDecorator);

        recycleViewAdapter = new RecycleViewAdapter();
        ItemTouchHelper.Callback callback = new MyItemTouchHelper(recycleViewAdapter, mContext);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        recycleViewAdapter.setTouchHelper(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(recycleViewAdapter);

    }
}
