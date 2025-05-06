package better.life.autoquiet.widget;

import static better.life.autoquiet.activity.ActivityMain.ACTION_CLOCK;
import static better.life.autoquiet.activity.ActivityMain.ACTION_NORM;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import better.life.autoquiet.R;
import better.life.autoquiet.activity.ActivityMain;
import better.life.autoquiet.common.FloatingClockService;

public class WidgetProvider extends AppWidgetProvider {

    int appWidgetId;
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        if (ACTION_CLOCK.equals(action)) {
            Intent serviceIntent = new Intent(context, FloatingClockService.class);
            context.startService(serviceIntent);
        } else if (ACTION_NORM.equals(action)) {
            appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            Intent intentA = new Intent(context, ActivityMain.class);
            intentA.setAction(action);
            intentA.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentA);
        }
        update_All_Widgets(context);
    }

    public static void update_All_Widgets(Context context) {

        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        ComponentName cn = new ComponentName(context, WidgetProvider.class);
        mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.widget_lines);

        int[] appWidgetIds = mgr.getAppWidgetIds(cn);
        for (int appWidgetId : appWidgetIds) {
            updateWidgetStatus(context, mgr, appWidgetId);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Update each widget
        for (int appWidgetId : appWidgetIds) {
            updateWidgetStatus(context, appWidgetManager, appWidgetId);
        }
    }

    public static void updateWidgetStatus(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_frame);

        Intent intent = new Intent(context, WidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        // When using setRemoteAdapter, it's recommended to set data like this for uniqueness
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))); // Add unique data URN
        views.setRemoteAdapter(R.id.widget_lines, intent);
        views.setScrollPosition(R.id.widget_lines, 0);

        // Set pending intent template for list items (when clicked)
        Intent mInt = new Intent(context, ActivityMain.class);
        mInt.setAction(ACTION_NORM);
        PendingIntent mPInt = PendingIntent.getActivity(context, 0, mInt,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setPendingIntentTemplate(R.id.widget_lines, mPInt);

        Intent cInt = new Intent(context, WidgetProvider.class);
        cInt.setAction(ACTION_CLOCK);
        cInt.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent mediaPIntent = PendingIntent.getBroadcast(context, appWidgetId,
                cInt, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.start_clock, mediaPIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

}