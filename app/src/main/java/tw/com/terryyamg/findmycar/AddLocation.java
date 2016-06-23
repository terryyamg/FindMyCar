package tw.com.terryyamg.findmycar;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static tw.com.terryyamg.findmycar.SetInfo.ADD_LATITUDE;
import static tw.com.terryyamg.findmycar.SetInfo.ADD_LONGITUDE;

public class AddLocation extends Activity {
	private SQLiteDatabase db;
	private DBManager dbHelper;

	private LocationManager locationManager;
	private LocationListener locationListener;

	private EditText etLocationName, etLatitude, etLongitude;

	private double lat, lon;
	;
	private int from, locationID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_location);

		dbHelper = new DBManager(this);
		dbHelper.openDatabase();
		db = dbHelper.getDatabase();

		Intent intent = getIntent();
		from = intent.getIntExtra("toAddLocation", 0); // 0.新增 1.更新

		etLocationName = (EditText) findViewById(R.id.etLocationName);
		etLatitude = (EditText) findViewById(R.id.etLatitude);
		etLongitude = (EditText) findViewById(R.id.etLongitude);
		final Button btGPS = (Button) findViewById(R.id.btGPS);
		final Button btMap = (Button) findViewById(R.id.btMap);
		final Button btConfitm = (Button) findViewById(R.id.btConfirm);

		switch (from) {
			case 1: // 更新
				locationID = intent.getIntExtra("locationID", 0);
				String locationName = intent.getStringExtra("locationName");
				double latitude = intent.getDoubleExtra("latitude", 0);
				double longitude = intent.getDoubleExtra("longitude", 0);

				ADD_LONGITUDE = Double.toString(latitude);
				ADD_LATITUDE = Double.toString(longitude);

				Log.i("latitude", latitude + "");
				etLocationName.setText(locationName);
				etLatitude.setText(ADD_LONGITUDE);
				etLongitude.setText(ADD_LATITUDE);

				break;

			default:
				break;
		}

		/* 定位取得座標 */
		btGPS.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				getCoordinatesGPS();

			}
		});

		/* 地圖取得座標 */
		btMap.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(AddLocation.this, MapMyCar.class);
				intent.putExtra("toMapMyCar", 1);
				startActivity(intent);
			}
		});

		/* 確認輸入 */
		btConfitm.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				String locationName = etLocationName.getText().toString();
				if (locationName.length() == 0) {
					Toast.makeText(AddLocation.this, getResources().getString(R.string.location_name_empty), Toast.LENGTH_SHORT).show();

					return;
				}
				addNewLocation(locationName, etLatitude.getText()
						.toString(), etLongitude.getText().toString());
				finish();
			}
		});

	}

	/* 輸入db */
	private void addNewLocation(String name, String s1, String s2) {

		switch (from) {
			case 0:
				String insert = "INSERT INTO location(name, latitude, longitude) VALUES ('"
						+ name + "','" + s1 + "','" + s2 + "')";
				db.execSQL(insert);
				break;

			case 1:
				String update = "UPDATE location SET name = '" + name
						+ "',latitude = '" + s1 + "',longitude = '" + s2
						+ "' WHERE id = '" + locationID + "'";
				db.execSQL(update);
				break;
			default:
				break;
		}

	}

	/* 取得座標 */
	private void getCoordinatesGPS() {
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

			lat = dest.getLatitude();
			lon = dest.getLongitude();

			etLatitude.setText(lat + "");
			etLongitude.setText(lon + "");

			if (ActivityCompat.checkSelfPermission(AddLocation.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AddLocation.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		etLatitude.setText(ADD_LONGITUDE);
		etLongitude.setText(ADD_LATITUDE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dbHelper.closeDatabase();
	}

}
