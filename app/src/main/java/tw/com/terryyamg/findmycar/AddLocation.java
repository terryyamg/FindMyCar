package tw.com.terryyamg.findmycar;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import static tw.com.terryyamg.findmycar.SetInfo.ADD_LATITUDE;
import static tw.com.terryyamg.findmycar.SetInfo.ADD_LONGITUDE;

public class AddLocation extends Activity implements
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener,
		com.google.android.gms.location.LocationListener {
	private SQLiteDatabase db;
	private DBManager dbHelper;

	private GoogleApiClient mGoogleApiClient;

	private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
	private long FASTEST_INTERVAL = 2000; /* 2 sec */

	private EditText etLocationName, etLatitude, etLongitude;

	private double lat, lon;
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
		final Button btConfirm = (Button) findViewById(R.id.btConfirm);

		switch (from) {
			case 1: // 更新
				locationID = intent.getIntExtra("locationID", 0);
				String locationName = intent.getStringExtra("locationName");
				double latitude = intent.getDoubleExtra("latitude", 0);
				double longitude = intent.getDoubleExtra("longitude", 0);

				ADD_LATITUDE = Double.toString(latitude);
				ADD_LONGITUDE = Double.toString(longitude);

				Log.i("latitude", latitude + "");
				etLocationName.setText(locationName);
				etLatitude.setText(ADD_LATITUDE);
				etLongitude.setText(ADD_LONGITUDE);

				break;

			default:
				break;
		}

		/* 定位取得座標 */
		btGPS.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
//				getCoordinatesGPS();
				mGoogleApiClient = new GoogleApiClient.Builder(AddLocation.this)
						.addApi(LocationServices.API)
						.addConnectionCallbacks(AddLocation.this)
						.addOnConnectionFailedListener(AddLocation.this).build();
				mGoogleApiClient.connect();
			}
		});

		/* 地圖取得座標 */
		btMap.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(AddLocation.this, MapsActivity.class);
				intent.putExtra("toMapMyCar", 1);
				startActivity(intent);
			}
		});

		/* 確認輸入 */
		btConfirm.setOnClickListener(new Button.OnClickListener() {
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
	@Override
	public void onConnected(@Nullable Bundle bundle) {
	// Get last known recent location.
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}
		Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		// Note that this can be NULL if last location isn't already known.
		if (mCurrentLocation != null) {
			// Print current location if not null
			Log.d("DEBUG", "current location: " + mCurrentLocation.toString());
			LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
		}
		// Begin polling for new location updates.
		startLocationUpdates();
	}

	@Override
	public void onConnectionSuspended(int i) {
		if (i == CAUSE_SERVICE_DISCONNECTED) {
			Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
		} else if (i == CAUSE_NETWORK_LOST) {
			Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
		}
	}

	// Trigger new location updates at interval
	protected void startLocationUpdates() {
		// Create the location request
		LocationRequest mLocationRequest = LocationRequest.create()
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
				.setInterval(UPDATE_INTERVAL)
				.setFastestInterval(FASTEST_INTERVAL);
		// Request location updates
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}
		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
				mLocationRequest, this);
	}

	@Override
	public void onLocationChanged(Location location) {
		Location dest = new Location(location); // 取得現在位置

		lat = dest.getLatitude();
		lon = dest.getLongitude();

		etLatitude.setText(lat + "");
		etLongitude.setText(lon + "");

		//關閉
		LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

		if (mGoogleApiClient != null) {
			mGoogleApiClient.disconnect();
		}
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}


	@Override
	protected void onResume() {
		super.onResume();
		etLatitude.setText(ADD_LATITUDE);
		etLongitude.setText(ADD_LONGITUDE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dbHelper.closeDatabase();
	}

}
