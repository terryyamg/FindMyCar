package tw.com.terryyamg.findmycar;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class LocationGPS implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private Context context;
    private List<ListItem> listItem;
    private TextView tvState;
    private Button btMyCarLocation;
    private GoogleApiClient mGoogleApiClient;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    public LocationGPS(Context context, List<ListItem> listItem, TextView tvState, Button btMyCarLocation) {
        this.context = context;
        this.listItem = listItem;
        this.tvState = tvState;
        this.btMyCarLocation = btMyCarLocation;
    }

    public void startConnect() {
        Log.i("startConnect","startConnect");
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
    }

    public void stopConnect() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("onConnected","onConnected");
    // Get last known recent location.

        Log.i("mCurrentLocation","mCurrentLocation");
        Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        // Note that this can be NULL if last location isn't already known.
        Log.i("mCurrentLocation","mCurrentLocation:"+mCurrentLocation);
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
            Toast.makeText(context, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(context, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

    // Trigger new location updates at interval
    protected void startLocationUpdates() {
        Log.i("startLocationUpdates","startLocationUpdates");
        // Create the location request
        LocationRequest mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("onLocationChanged","onLocationChanged");
        Location dest = new Location(location); // 取得現在位置

        float distance, min = 0;
        int minNumber = 0;
        for (int i = 0; i < listItem.size(); i++) {
            Log.i("Latitude", listItem.get(i).getLatitude() + "");
            Log.i("Longitude", listItem.get(i).getLongitude() + "");
            dest.setLatitude(listItem.get(i).getLatitude());
            dest.setLongitude(listItem.get(i).getLongitude());
            // 計算現在與所有地點距離 取出最近的
            distance = location.distanceTo(dest);
            Log.i("distance", distance + "");
            if (i == 0) {
                min = distance;
            }
            if (min >= distance) {
                min = distance;
                minNumber = i;
            }
            Log.i("minNumber", minNumber + "");
        }
        Log.i("minName", listItem.get(minNumber).getLocationName());
        tvState.setText(context.getResources().getString(R.string.record_finish));
        btMyCarLocation.setText(listItem.get(minNumber).getLocationName());

        Function funHelper = new Function(context);
        funHelper.setString("locationName", listItem.get(minNumber).getLocationName());
        funHelper.setString("latitude", Double.toString(dest.getLatitude()));
        funHelper.setString("longitude", Double.toString(dest.getLongitude()));

        //關閉
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
