package tw.com.terryyamg.findmycar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient googleClient;
    private Function funHelper = new Function(this);
    private SQLiteDatabase db;
    private DBManager dbHelper;

    private List<ListItem> listItem, listItemEnable;
    private RecyclerView lvLocation;
    private CustomRecyclerViewAdapter adapter;

    private TextView tvMyCarLocation, tvState;
    private StringBuilder message;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ad
        AdView mAdView = (AdView) findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("43418FAE080D9E74F91D761B77604321").build();
        mAdView.loadAd(adRequest);

        tvMyCarLocation = (TextView) findViewById(R.id.tvMyCarLocation);
        tvState = (TextView) findViewById(R.id.tvState);
        ImageButton ibMark = (ImageButton) findViewById(R.id.ibMark);
        Button btMap = (Button) findViewById(R.id.btMap);
        Button btTransport = (Button) findViewById(R.id.btTransport);
        Button btAdded = (Button) findViewById(R.id.btAdded);
        lvLocation = (RecyclerView) findViewById(R.id.lvLoaction);

        //wear
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        dbHelper = new DBManager(this);
        dbHelper.openDatabase();
        db = dbHelper.getDatabase();

        tvMyCarLocation.setText(funHelper.getString("locationName"));

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || !locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(this, getResources().getString(R.string.gps_close), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

		/* 列出地點 */
        allLocation();

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lvLocation.setLayoutManager(lm);
        lvLocation.setHasFixedSize(true); //當RecyclerView大小沒改變時最佳化
        lvLocation.setItemAnimator(new DefaultItemAnimator()); //預設動畫效果
        lvLocation.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL)); //分隔線
        lvLocation.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), lvLocation, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                showLoactionDialog(position);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

		/* 標計車子位置 */
        ibMark.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                tvState.setText(getResources().getString(R.string.positioning));
                LocationGPS locationGPS = new LocationGPS(MainActivity.this, getEnableLocation(), tvMyCarLocation, tvState);
                locationGPS.startConnect();
            }
        });

		/* 地圖位置 */
        btMap.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("toMapMyCar", 0);
                startActivity(intent);
            }
        });

        /* 傳送資料至腕錶*/
        btTransport.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                //取得資料
                message = new StringBuilder();
                for (int i = 0; i < listItem.size(); i++) {
                    if (i == 0) {
                        message.append("[{");
                    }

                    String id = Integer.toString(listItem.get(i).getLocationID());
                    String name = listItem.get(i).getLocationName();
                    String lat = Double.toString(listItem.get(i).getLatitude());
                    String lon = Double.toString(listItem.get(i).getLongitude());
                    String state = Integer.toString(listItem.get(i).getState());
                    // [{"id":"0","name":"xxx"},{"id":"1","name":"ooo"}]
                    message.append("\"id\":\"" + id + "\"");
                    message.append(",");
                    message.append("\"name\":\"" + name + "\"");
                    message.append(",");
                    message.append("\"lat\":\"" + lat + "\"");
                    message.append(",");
                    message.append("\"lon\":\"" + lon + "\"");
                    message.append(",");
                    message.append("\"state\":\"" + state + "\"");

                    if (i == listItem.size() - 1) {
                        message.append("}]");
                    } else {
                        message.append("},{");
                    }
                }

                Log.i("message", message + "");
                googleClient.connect();
            }
        });

        /* 新增地點 */
        btAdded.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddLocation.class);
                intent.putExtra("toAddLocation", 0);
                startActivity(intent);
            }
        });

    }

    /* 列出地點 */
    private void allLocation() {

        listItem = new ArrayList<>();
        adapter = new CustomRecyclerViewAdapter(this, listItem);
        lvLocation.setAdapter(adapter);

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
                li.setState(cursor.getInt(4));// 狀態
                listItem.add(li);

            } while (cursor.moveToNext());

        } catch (Exception e) {

        } finally {
            cursor.close();
        }

        adapter.notifyDataSetChanged();

    }

    /*取出啟動地點*/
    private List<ListItem> getEnableLocation() {
        listItemEnable = new ArrayList<>();
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
                listItemEnable.add(li);

            } while (cursor.moveToNext());

        } catch (Exception e) {

        } finally {
            cursor.close();
        }
        return listItemEnable;
    }

    private void showLoactionDialog(final int position) {
        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_item);
        final TextView tvTitle = (TextView) dialog.findViewById(R.id.tvTitle);
        final Switch swEnable = (Switch) dialog.findViewById(R.id.swEnable);
        final Button btUpdate = (Button) dialog.findViewById(R.id.btUpdate);
        final Button btDelete = (Button) dialog.findViewById(R.id.btDelete);

        tvTitle.setText(listItem.get(position).getLocationName());
        // 啟動或關閉
        if (listItem.get(position).getState() == 1) {
            swEnable.setChecked(true);
        } else {
            swEnable.setChecked(false);
        }

        swEnable.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                String update;
                if (isChecked) { // 開啟
                    update = "UPDATE location SET state = '1' WHERE id = '"
                            + listItem.get(position).getLocationID() + "'";
                } else { // 關閉
                    update = "UPDATE location SET state = '0' WHERE id = '"
                            + listItem.get(position).getLocationID() + "'";
                }
                db.execSQL(update);
                allLocation();
            }
        });

        // 更新
        btUpdate.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddLocation.class);
                intent.putExtra("toAddLocation", 1);
                intent.putExtra("locationID", listItem.get(position).getLocationID());
                intent.putExtra("locationName", listItem.get(position).getLocationName());
                intent.putExtra("latitude", listItem.get(position).getLatitude());
                intent.putExtra("longitude", listItem.get(position).getLongitude());
                startActivity(intent);

            }

        });

        // 刪除
        btDelete.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View v) {
                // 再次確認是否刪除
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                        MainActivity.this);
                alertDialog.setMessage(getResources().getString(
                        R.string.confirm_delete));

                alertDialog.setPositiveButton(R.string.confirm_yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialoginterface, int i) {

                                String delete = "DELETE FROM location WHERE id = '"
                                        + listItem.get(position).getLocationID() + "'";
                                db.execSQL(delete);
                                allLocation();
                                dialog.dismiss();
                            }
                        });
                alertDialog.setNegativeButton(R.string.confirm_no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialoginterface, int i) {

                            }
                        });

                alertDialog.show();

            }

        });

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        allLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.closeDatabase();
        // android.os.Process.killProcess(android.os.Process.myPid());

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("onConnected","onConnected");
        new WearConnect("/db_data", message.toString(), googleClient).start();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != googleClient && googleClient.isConnected()) {
            googleClient.disconnect();
        }
        Log.i("onStop", "onStop");
    }
}