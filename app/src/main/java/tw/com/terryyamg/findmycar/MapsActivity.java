package tw.com.terryyamg.findmycar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import static tw.com.terryyamg.findmycar.SetInfo.ADD_LATITUDE;
import static tw.com.terryyamg.findmycar.SetInfo.ADD_LONGITUDE;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private Function funHelper = new Function(this);

    private GoogleApiClient mGoogleApiClient;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    private GoogleMap mMap;
    private LatLng p1;
    private Button btMove;
    private int from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();

        btMove = (Button) findViewById(R.id.btMove);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        from = intent.getIntExtra("toMapMyCar", 0);

        String latString = funHelper.getString("latitude");
        String lonString = funHelper.getString("longitude");

        if (latString.length() <= 0 || lonString.length() <= 0) {
            latString = "0.0";
            lonString = "0.0";
        }

        double lat = Double.parseDouble(latString);
        double lon = Double.parseDouble(lonString);

        Log.i("LATITUDE", "lat" + lat);
        Log.i("LONGITUDE", "lon" + lon);
        p1 = new LatLng(lat, lon);

        // 移至車位置
        btMove.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(p1));
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(p1));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));

        switch (from) {
            case 0:
                // add car marker
                mMap.addMarker(new MarkerOptions().position(p1)
                        .title(getResources().getString(R.string.here))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                break;
            case 1:
                btMove.setVisibility(View.GONE);
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                    public void onMapClick(LatLng latLng) {

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title(latLng.latitude + " : " + latLng.longitude);
                        mMap.clear();
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.addMarker(markerOptions);
                        ADD_LATITUDE = Double.toString(latLng.latitude);
                        ADD_LONGITUDE = Double.toString(latLng.longitude);

                    }
                });
                break;

            default:
                break;
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("onConnected", "onConnected");
        // Get last known recent location.

        Log.i("mCurrentLocation", "mCurrentLocation");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        // Note that this can be NULL if last location isn't already known.
        Log.i("mCurrentLocation", "mCurrentLocation:" + mCurrentLocation);
        if (mCurrentLocation != null) {
            // Print current location if not null
            Log.d("DEBUG", "current location: " + mCurrentLocation.toString());
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

    protected void startLocationUpdates() {
        Log.i("startLocationUpdates", "startLocationUpdates");
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
        if (from == 0) {
            mMap.clear();
            Location dest = new Location(location); // 取得現在位置
            Log.i("lat", dest.getLatitude() + "");
            Log.i("lon", dest.getLongitude() + "");
            LatLng myLatLng = new LatLng(dest.getLatitude(), dest.getLongitude());
            // 畫線
            try {
                mMap.addPolyline(new PolylineOptions().add(p1, myLatLng).width(5).color(Color.RED));
            } catch (Exception e) {
                Log.i("e", e + "");
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }
}
