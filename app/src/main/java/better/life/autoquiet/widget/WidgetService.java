package better.life.autoquiet.widget;

import static better.life.autoquiet.activity.ActivityAddEdit.alarmIcons;
import static better.life.autoquiet.activity.ActivityMain.ACTION_NORM;
import static better.life.autoquiet.activity.ActivityMain.nextTasks;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import androidx.core.content.ContextCompat;

import better.life.autoquiet.R;
import better.life.autoquiet.Sub.ContextProvider;
import better.life.autoquiet.nexttasks.ScheduleNextTask;
import better.life.autoquiet.models.NextTask;

public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Context context = this.getApplicationContext();
        ContextProvider.init(context);
        return new ListRemoteViewsFactory(context, intent);
    }
}

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    Context wContext;
    public ListRemoteViewsFactory(Context wContext, Intent intent) {
            this.wContext = wContext;
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
        if (nextTasks == null) {
            if (wContext == null) {
                wContext = ContextProvider.get();
                Log.e("Context"," // is null // "+this.getClass().getName());
            }
            new ScheduleNextTask("getCount Zero");
        }
        return nextTasks.size();
    }

    String time;
    @Override
    public RemoteViews getViewAt(int position) {
        if (position >= nextTasks.size())
            return null;
        RemoteViews views = new RemoteViews(wContext.getPackageName(), R.layout.widget_line);
        NextTask nt = nextTasks.get(position);
        views.setTextViewText(R.id.wSubject, nt.subject);
        time = buildHourMin(nt.hour, nt.min) + " " + nt.suffix;
        views.setTextViewText(R.id.wTime, time);
        int colorU = (position % 2) == 0 ?
                ContextCompat.getColor(wContext,R.color.widget_line0)
                : ContextCompat.getColor(wContext,R.color.widget_line1);
        Intent fIntent = new Intent();
        fIntent.setAction(ACTION_NORM);
        views.setOnClickFillInIntent(R.id.wLine, fIntent);

        views.setImageViewResource(R.id.wType, alarmIcons[nt.alarmType]);
        views.setInt(R.id.wType, "setColorFilter",(ContextCompat.getColor(wContext, R.color.white)));

        views.setInt(R.id.lineUp, "setBackgroundColor", colorU);
        views.setInt(R.id.wSubject, "setBackgroundColor", colorU);
        views.setInt(R.id.wTime, "setBackgroundColor", colorU);
        views.setInt(R.id.wType, "setBackgroundColor", colorU);

        return views;
    }

    String buildHourMin(int hour, int min) { return int2NN(hour)+":"+int2NN(min); }
    String int2NN (int nbr) {
        return (String.valueOf(100 + nbr)).substring(1);
    }

    @Override
    public RemoteViews getLoadingView() {
        // Return a RemoteViews for the loading R_M.
        return new RemoteViews(wContext.getPackageName(), R.layout.widget_frame);
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