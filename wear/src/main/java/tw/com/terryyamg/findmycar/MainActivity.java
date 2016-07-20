package tw.com.terryyamg.findmycar;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private Function funHelper = new Function(this);
    private SQLiteDatabase db;
    private DBManager dbHelper;
    private List<ListItem> listItem;
    final private int ACCESS_COARSE_LOCATION_PERMISSIONS_REQUEST_READ_CONTACTS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBManager(this);
        dbHelper.openDatabase();
        db = dbHelper.getDatabase();

        // permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                showMessageOKCancel(getResources().getString(R.string.permission_message),
                        new DialogInterface.OnClickListener() {
                            @TargetApi(Build.VERSION_CODES.M)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                                        ACCESS_COARSE_LOCATION_PERMISSIONS_REQUEST_READ_CONTACTS);
                            }
                        });
                return;
            }
        }

        // Register the local broadcast receiver
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {

            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                final ImageButton ibMark = (ImageButton) stub.findViewById(R.id.ibMark);
                final TextView tvState = (TextView) stub.findViewById(R.id.tvState);
                final Button btMyCarLocation = (Button) stub.findViewById(R.id.btMyCarLocation);

                btMyCarLocation.setText(funHelper.getString("locationName"));

                ibMark.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getEnableLocation();
                        tvState.setText(getResources().getString(R.string.positioning));
                        LocationGPS locationGPS = new LocationGPS(MainActivity.this, listItem, tvState, btMyCarLocation);
                        locationGPS.startConnect();
                    }
                });
                btMyCarLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(funHelper.getString("locationName").equals(""))
                            return;
                        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });
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
                li.setState(cursor.getInt(4));// 狀態

                listItem.add(li);
            } while (cursor.moveToNext());

        } catch (Exception e) {

        } finally {
            cursor.close();
        }

    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String message = intent.getStringExtra("message");
            Log.i("message", "message:" + message);
            JSONArray jsonArray;
            String name, lat, lon, state;
            try {
                jsonArray = new JSONArray(message);
                Log.i("length",jsonArray.length()+"");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonData = jsonArray.getJSONObject(i);
                    name = jsonData.getString("name");
                    lat = jsonData.getString("lat");
                    lon = jsonData.getString("lon");
                    state = jsonData.getString("state");
                    //刪除資料
                    String delete = "DELETE FROM location";
                    String update = "UPDATE sqlite_sequence SET seq=0 WHERE name='location'";
                    db.execSQL(delete);
                    db.execSQL(update);
                    //新增資料
                    String insert = "INSERT INTO location(name, latitude, longitude, state) VALUES ('"
                            + name + "','" + lat + "','" + lon + "','"+state+"')";
                    db.execSQL(insert);
                }

                Toast.makeText(MainActivity.this, getResources().getString(R.string.transport_data), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.i("e", e + "");
            }
            Log.v("MessageActivity", "Main activity received message: " + message);
            // Display message in UI
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case ACCESS_COARSE_LOCATION_PERMISSIONS_REQUEST_READ_CONTACTS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    Log.i("Agree","Agree");
                } else {
                    // Permission Denied
                    Log.i("Denied","Denied");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.closeDatabase();
    }
}
