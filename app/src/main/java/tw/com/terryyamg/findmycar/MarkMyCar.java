package tw.com.terryyamg.findmycar;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MarkMyCar extends Service {
	private Function funHelper = new Function(this);
	private SQLiteDatabase db;
	private DBManager dbHelper;

	private LocationManager locationManager;
	private LocationListener locationListener;

	private List<ListItem> listItem;

	@Override
	public void onCreate() {
		dbHelper = new DBManager(this);
		dbHelper.openDatabase();
		db = dbHelper.getDatabase();

		allLocation();
		LocationGPS locationGPS = new LocationGPS(this,listItem);
		locationGPS.startConnect();
//		getCoordinates();

	}

	@Override
	public void onStart(Intent intent, int startId) {
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

	/* 取得座標 */
//	private void getCoordinates() {
//		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//		locationListener = new getLocationListener();
//		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//			return;
//		}
//		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//				5000, 10, locationListener);
//		locationManager.requestLocationUpdates(
//				LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);
//
//	}
//
//	private class getLocationListener implements LocationListener {
//
//		public void onLocationChanged(Location current) {
//			if (current == null) {
//				return;
//			}
//
//			Location dest = new Location(current); // 取得現在位置
//
//			// 找車
//			float distance, min = 0;
//			int minNumber = 0;
//			for (int i = 0; i < length; i++) {
//				dest.setLatitude(latitude[i]);
//				dest.setLongitude(longitude[i]);
//				// 計算現在與所有地點距離 取出最近的
//				distance = current.distanceTo(dest);
//				if (i == 0) {
//					min = distance;
//				}
//				if (min >= distance) {
//					min = distance;
//					minNumber = i;
//				}
//			}
//
//			funHelper.setString("locationName", locationName[minNumber]);
//
//			if (ActivityCompat.checkSelfPermission(MarkMyCar.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MarkMyCar.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//				return;
//			}
//			locationManager.removeUpdates(locationListener); // 關閉gps
//			Intent intent1 = new Intent(MarkMyCar.this, MarkMyCar.class);
//			// 關閉服務
//			stopService(intent1);
//
//		}
//
//		public void onProviderDisabled(String provider) {
//		}
//
//		public void onProviderEnabled(String provider) {
//		}
//
//		public void onStatusChanged(String provider, int status, Bundle extras) {
//		}
//	}

	/* 列出地點 */
	private void allLocation() {
		listItem = new ArrayList<>();
		String select = "SELECT * FROM location";
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
