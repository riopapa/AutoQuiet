package better.life.autoquiet;

import static better.life.autoquiet.activity.ActivityMain.ACTION_NORM;
import static better.life.autoquiet.activity.ActivityMain.nextTasks;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import better.life.autoquiet.models.NextTask;

public class LogWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    Context context;
    public ListRemoteViewsFactory(Context context, Intent intent) {
        this.context = context;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {}

    @Override
    public void onDestroy() {}

    @Override
    public int getCount() {
        return nextTasks.size();
    }

    String time;
    @Override
    public RemoteViews getViewAt(int position) {
        if (position >= nextTasks.size())
            return null;
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_line);
        NextTask nt = nextTasks.get(position);
        views.setTextViewText(R.id.wSubject, nt.subject);

        time = buildHourMin(nt.hour, nt.min);
        views.setTextViewText(R.id.wTime, time);
        Intent fIntent = new Intent();
        fIntent.setAction(ACTION_NORM);
        views.setOnClickFillInIntent(R.id.wLine, fIntent);
        return views;
    }

    String buildHourMin(int hour, int min) { return int2NN(hour)+":"+int2NN(min); }
    String int2NN (int nbr) {
        return (String.valueOf(100 + nbr)).substring(1);
    }

    @Override
    public RemoteViews getLoadingView() {
        // Return a RemoteViews for the loading R_M.
        return new RemoteViews(context.getPackageName(), R.layout.widget_frame);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position; // Or a unique ID for each item if you have one
    }

    @Override
    public boolean hasStableIds() {
        return true; // Return true if the same item always has the same ID
    }
}