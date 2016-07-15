package tw.com.terryyamg.findmycar;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBManager(this);
        dbHelper.openDatabase();
        db = dbHelper.getDatabase();

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.closeDatabase();
    }
}
