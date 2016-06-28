package tw.com.terryyamg.findmycar;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

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
                        tvState.setText("紀錄中。。。");
                        LocationGPS locationGPS = new LocationGPS(MainActivity.this,listItem,tvState,btMyCarLocation);
                        locationGPS.startConnect();
                    }
                });
                btMyCarLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this,MapsActivity.class);
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
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.closeDatabase();
    }
}
