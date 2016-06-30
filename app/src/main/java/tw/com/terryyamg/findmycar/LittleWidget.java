package tw.com.terryyamg.findmycar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

public class LittleWidget extends AppWidgetProvider {

	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		final int IDs = appWidgetIds.length;
		
		for (int i = 0; i < IDs; i++) {
			int appWidgetId = appWidgetIds[i];
			updateAppWidget(context, appWidgetManager, appWidgetId);
		}
	}

	public void onDeleted(Context context, int[] appWidgetIds) {

	}

	private void updateAppWidget(Context context,
			AppWidgetManager appWidgetManager, int appWidgetId) {
		Log.i("updateAppWidget","updateAppWidget");
		// 綁定Clock_widget注意androidmanifest receiver需註冊action
		final Intent refreshIntent = new Intent(context, LittleWidget.class);
		// set action字串
		refreshIntent.setAction("click");
		final PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(
				context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.little_widget);
		views.setOnClickPendingIntent(R.id.ibMark, refreshPendingIntent);

		SharedPreferences preferencesGet = context.getApplicationContext()
				.getSharedPreferences("fmc",
						android.content.Context.MODE_PRIVATE);
		CharSequence showLocationName = preferencesGet.getString("locationName", "");
		views.setTextViewText(R.id.tvMyCarLocation, showLocationName);

		appWidgetManager.updateAppWidget(appWidgetId, views);

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		Log.i("onReceive","onReceive");
		// 綁定service
		Intent intent1 = new Intent(context, MarkMyCar.class);
		// 啟動服務
		context.startService(intent1);

	}

}
