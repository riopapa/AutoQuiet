package com.urrecliner.letmequiet.utility;

public interface ItemTouchHelperAdapter {

    void onItemMove(int fromPosition, int toPosition);

    void onItemSwiped(int position);
}