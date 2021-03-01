package com.urrecliner.letmequiet;

import android.graphics.Rect;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

public class VerticalSpacingItemDecorator extends RecyclerView.ItemDecoration{

    private final int verticalSpaceHeight;

    public VerticalSpacingItemDecorator(int verticalSpaceHeight) {
        this.verticalSpaceHeight = verticalSpaceHeight;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {

        outRect.bottom = verticalSpaceHeight;
        outRect.left = verticalSpaceHeight;
        outRect.right = verticalSpaceHeight;
        outRect.bottom = verticalSpaceHeight;

        // Add top margin only for the first item to avoid double space between items
        if(parent.getChildAdapterPosition(view) == 0) {
            outRect.top = verticalSpaceHeight;
        }
    }
}
