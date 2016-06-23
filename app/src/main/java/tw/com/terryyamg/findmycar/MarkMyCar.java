package tw.com.terryyamg.findmycar;

import android.Manifest;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

public class MarkMyCar extends Service {
	private Function funHelper = new Function(this);
	private SQLiteDatabase db;
	private DBManager dbHelper;

	private LocationManager locationManager;
	private LocationListener locationListener;

	private int[] locationState;
	private String[] locationName;
	private double[] latitude, longitude;

	private int length;

	@Override
	public void onCreate() {
		dbHelper = new DBManager(this);
		dbHelper.openDatabase();
		db = dbHelper.getDatabase();

		allLocation();
		getCoordinates();

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
	private void getCoordinates() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationListener = new getLocationListener();
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				5000, 10, locationListener);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);

	}

	private class getLocationListener implements LocationListener {

		public void onLocationChanged(Location current) {
			if (current == null) {
				return;
			}

			Location dest = new Location(current); // 取得現在位置

			// 找車
			float distance, min = 0;
			int minNumber = 0;
			for (int i = 0; i < length; i++) {
				dest.setLatitude(latitude[i]);
				dest.setLongitude(longitude[i]);
				// 計算現在與所有地點距離 取出最近的
				distance = current.distanceTo(dest);
				if (i == 0) {
					min = distance;
				}
				if (min >= distance) {
					min = distance;
					minNumber = i;
				}
			}

			funHelper.setString("locationName", locationName[minNumber]);

			if (ActivityCompat.checkSelfPermission(MarkMyCar.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MarkMyCar.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				// TODO: Consider calling
				//    ActivityCompat#requestPermissions
				// here to request the missing permissions, and then overriding
				//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
				//                                          int[] grantResults)
				// to handle the case where the user grants the permission. See the documentation
				// for ActivityCompat#requestPermissions for more details.
				return;
			}
			locationManager.removeUpdates(locationListener); // 關閉gps
			Intent intent1 = new Intent(MarkMyCar.this, MarkMyCar.class);
			// 關閉服務
			stopService(intent1);

		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

	/* 列出地點 */
	private void allLocation() {
		String select = "SELECT * FROM location";
		Cursor cursor = db.rawQuery(select, null);
		cursor.moveToFirst();
		try {
			int count = 0;
			length = cursor.getCount();
			locationName = new String[length]; // 地點名稱
			latitude = new double[length]; // 緯度
			longitude = new double[length]; // 經度
			locationState = new int[length]; // 狀態
			do {
				locationName[count] = cursor.getString(1);
				latitude[count] = cursor.getDouble(2);
				longitude[count] = cursor.getDouble(3);
				locationState[count] = cursor.getInt(4);
				count++;
			} while (cursor.moveToNext());

		} catch (Exception e) {

		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

	}

	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

}
