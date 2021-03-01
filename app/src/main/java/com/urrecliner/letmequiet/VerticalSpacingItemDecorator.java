package com.urrecliner.letmequiet;

import android.graphics.Rect;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

public class VerticalSpacingItemDecorator extends RecyclerView.ItemDecoration{

    private final int itemSpace;

    public VerticalSpacingItemDecorator(int itemSpace) {
        this.itemSpace = itemSpace;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {

        outRect.bottom = itemSpace;
        outRect.left = itemSpace;
        outRect.right = itemSpace;

        // Add top margin only for the first item to avoid double space between items
        if(parent.getChildAdapterPosition(view) == 0) {
            outRect.top = itemSpace;
        }
    }
}
