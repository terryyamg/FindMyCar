package tw.com.terryyamg.findmycar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import static tw.com.terryyamg.findmycar.SetInfo.ADD_LATITUDE;
import static tw.com.terryyamg.findmycar.SetInfo.ADD_LONGITUDE;

public class MapMyCar extends FragmentActivity implements OnMarkerClickListener {
	private Function funHelper = new Function(this);
	private double lat, lon;
	private GoogleMap map;
	LatLng latLng;
	LatLng p1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_my_car);

		Button btMove = (Button) findViewById(R.id.btMove);
		// init
		Intent intent = getIntent();
		int from = intent.getIntExtra("toMapMyCar", 0);

		String latString = funHelper.getString("latitude");
		String lonString = funHelper.getString("longitude");

		if (latString.length() <= 0 || lonString.length() <= 0) {
			latString = "0.0";
			lonString = "0.0";
		}

		lat = Double.parseDouble(latString);
		lon = Double.parseDouble(lonString);

		Log.i("LATITUDE", "lat" + lat);
		Log.i("LONGITUDE", "lon" + lon);
		p1 = new LatLng(lat, lon);

		// 移至車位置
		btMove.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				map.moveCamera(CameraUpdateFactory.newLatLng(p1));
			}
		});

		// google map
		try {
			map = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();

			if (map != null) {
				// google location
				setUpMap();
			}

		} catch (NullPointerException e) {
			// Log.i("map", "NullPointException");
		}


		switch (from) {
			case 0:
				// 畫線
				try {
					map.addPolyline(new PolylineOptions().add(p1, latLng).width(5)
							.color(Color.RED));
				} catch (Exception e) {
					Log.i("e", e + "");
				}

				// add car marker
				map.addMarker(new MarkerOptions().position(p1)
						.title(getResources().getString(R.string.here))
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
				break;
			case 1:
				btMove.setVisibility(View.GONE);
				map.setOnMapClickListener(new OnMapClickListener() {

					public void onMapClick(LatLng latLng) {

						MarkerOptions markerOptions = new MarkerOptions();
						markerOptions.position(latLng);
						markerOptions.title(latLng.latitude + " : "
								+ latLng.longitude);
						map.clear();
						map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
						map.addMarker(markerOptions);
						ADD_LATITUDE = Double.toString(latLng.latitude);
						ADD_LONGITUDE = Double.toString(latLng.longitude);

					}
				});
				break;

			default:
				break;
		}

	}

	private void setUpMap() {
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
		map.setMyLocationEnabled(true);
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		String provider = locationManager.getBestProvider(criteria, true);
		try {

			Location myLocation = locationManager
					.getLastKnownLocation(provider);
			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

			double latitude = myLocation.getLatitude();
			double longitude = myLocation.getLongitude();
			latLng = new LatLng(latitude, longitude);
			map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
			map.animateCamera(CameraUpdateFactory.zoomTo(18));

		} catch (NullPointerException e) {
		}
	}

	// map Marker點擊
	public boolean onMarkerClick(Marker arg0) {
		return false;
	}

}
