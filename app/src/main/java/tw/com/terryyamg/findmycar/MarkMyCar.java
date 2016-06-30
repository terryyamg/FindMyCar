package tw.com.terryyamg.findmycar;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.ArrayList;
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

		getEnableLocation();
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
			Toast.makeText(this, "GPS沒開唷", Toast.LENGTH_SHORT).show();
		}

		ComponentName thisWidget = new ComponentName(this, LittleWidget.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(this);
		RemoteViews remoteViews = new RemoteViews(getPackageName(),
				R.layout.little_widget);

		CharSequence showLocationName = funHelper.getString("locationName");
		remoteViews.setTextViewText(R.id.tvMyCarLocation, showLocationName);

		manager.updateAppWidget(thisWidget, remoteViews);

	}

	/* 列出地點 */
	private void getEnableLocation() {
		listItem = new ArrayList<>();
		String select = "SELECT * FROM location WHERE state = '1'";
		Cursor cursor = db.rawQuery(select, null);
		cursor.moveToFirst();
		try {
			do {
				ListItem li = new ListItem();
				li.setLocationID(cursor.getInt(0));// 地點id
				li.setLocationName(cursor.getString(1));// 地點名稱
				li.setLatitude(cursor.getDouble(2));// 緯度
				li.setLongitude(cursor.getDouble(3));// 經度
				String state = "";
				switch (cursor.getInt(4)) {
					case 1:
						state = getResources().getString(R.string.stateEnalbe);
						break;
					case 0:
						state = getResources().getString(R.string.stateDisalbe);
						break;
					default:
						break;
				}
				li.setState(state);// 狀態
				listItem.add(li);
			} while (cursor.moveToNext());

		} catch (Exception e) {

		} finally {
			cursor.close();
		}

	}

	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

}
