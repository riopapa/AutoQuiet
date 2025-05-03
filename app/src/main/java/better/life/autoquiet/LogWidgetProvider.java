package better.life.autoquiet;

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

import better.life.autoquiet.activity.ActivityMain;

public class LogWidgetProvider extends AppWidgetProvider {

    int appWidgetId;
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        if (ACTION_CLOCK.equals(action) || ACTION_NORM.equals(action)) {
            appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            Intent intentStock = new Intent(context, ActivityMain.class);
            intentStock.setAction(action);
            intentStock.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentStock);
//        } else if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
            onUpdate(context, AppWidgetManager.getInstance(context), intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS));
        }
//        update_All_Widgets(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Update each widget
        for (int appWidgetId : appWidgetIds) {
            updateWidgetStatus(context, appWidgetManager, appWidgetId);
        }
    }

    public static void update_All_Widgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, LogWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        // Loop through all widgets and update each one
        for (int widgetId : appWidgetIds) {
            updateWidgetStatus(context, appWidgetManager, widgetId);
        }
    }

    public static void updateWidgetStatus(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_frame);

        Intent intent = new Intent(context, LogWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        // When using setRemoteAdapter, it's recommended to set data like this for uniqueness
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))); // Add unique data URN
        views.setRemoteAdapter(R.id.widget_lines, intent);
        views.setScrollPosition(R.id.widget_lines, 0);

        // Set pending intent template for list items (when clicked)
        Intent lineIntent = new Intent(context, ActivityMain.class);
        lineIntent.setAction(ACTION_NORM); // This action will be received by ActivityMain
        // Put necessary extras here that apply to a list item click if needed
        // Don't put widget ID here, it will be added by the system along with position

        PendingIntent clickPendingIntentTemplate =
                PendingIntent.getActivity(context, 0, lineIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setPendingIntentTemplate(R.id.widget_lines, clickPendingIntentTemplate);


        // Set click listener for the start_clock button (assuming it's not part of the list items)
        Intent sGroupIntent = new Intent(context, ActivityMain.class); // Or a BroadcastReceiver if you prefer
        sGroupIntent.setAction(ACTION_CLOCK); // This action will be received by ActivityMain
        sGroupIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        // Use a different request code than 0 if you have multiple different pending intents
        PendingIntent sGroupPIntent = PendingIntent.getActivity(context, appWidgetId, // Using getActivity as onReceive starts Activity
                sGroupIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.start_clock, sGroupPIntent);

        // *** IMPORTANT: Apply the views to the widget ***
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }





}