package tw.com.terryyamg.findmycar;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.List;

public class MarkMyCar extends Service {
	private Function funHelper = new Function(this);
	private SQLiteDatabase db;
	private DBManager dbHelper;

	private List<ListItem> listItem;

	@Override
	public void onCreate() {
		Log.i("onCreate","onCreate");
		dbHelper = new DBManager(this);
		dbHelper.openDatabase();
		db = dbHelper.getDatabase();

		/* 列出地點 */
		MarkMyCarPresenter mmcp = new MarkMyCarPresenterImpl(db);
		mmcp.selectEnableLocationData();

		LocationGPS locationGPS = new LocationGPS(this,listItem);
		locationGPS.startConnect();
		Log.i("startConnect","startConnect");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.i("onStart","onStart");
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				|| !locationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			Toast.makeText(this, getResources().getString(R.string.gps_close), Toast.LENGTH_SHORT).show();
		}

		ComponentName thisWidget = new ComponentName(this, LittleWidget.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(this);
		RemoteViews remoteViews = new RemoteViews(getPackageName(),
				R.layout.little_widget);

		CharSequence showLocationName = funHelper.getString("locationName");
		remoteViews.setTextViewText(R.id.tvMyCarLocation, showLocationName);

		manager.updateAppWidget(thisWidget, remoteViews);

	}

	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

}
