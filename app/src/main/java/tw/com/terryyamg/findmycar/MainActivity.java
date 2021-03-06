package tw.com.terryyamg.findmycar;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import java.util.List;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,MainActivityView {
    private GoogleApiClient googleClient;
    private Function funHelper = new Function(this);
    private SQLiteDatabase db;
    private DBManager dbHelper;
    private MainActivityPresenter map;

    private List<ListItem> listItem;
    private RecyclerView lvLocation;

    private TextView tvMyCarLocation, tvState;
    private StringBuilder message;
    final private int ACCESS_COARSE_LOCATION_PERMISSIONS_REQUEST_READ_CONTACTS = 123;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ad
        AdView mAdView = (AdView) findViewById(R.id.adView);
        // AdRequest adRequest = new AdRequest.Builder().build();
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("43418FAE080D9E74F91D761B77604321").build();
//        AdRequest adRequest = new AdRequest.Builder().addTestDevice("AF069AF27F8B630A0A87F78D7304C879").build();
        mAdView.loadAd(adRequest);

        //shortcut
        Intent shortcutIntent = new Intent(getApplicationContext(), MainActivity.class);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getResources().getString(R.string.app_name));
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_launcher));
        addIntent.putExtra("duplicate", false);
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        getApplicationContext().sendBroadcast(addIntent);

        tvMyCarLocation = (TextView) findViewById(R.id.tvMyCarLocation);
        tvState = (TextView) findViewById(R.id.tvState);
        ImageButton ibMark = (ImageButton) findViewById(R.id.ibMark);
        Button btMap = (Button) findViewById(R.id.btMap);
        Button btTransport = (Button) findViewById(R.id.btTransport);
        Button btIntroduction = (Button) findViewById(R.id.btIntroduction);
        Button btAdded = (Button) findViewById(R.id.btAdded);
        lvLocation = (RecyclerView) findViewById(R.id.lvLocation);

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
                showLocationDialog(position);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

		/* 標計車子位置 */
        ibMark.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                tvState.setText(getResources().getString(R.string.positioning));
                /*取出啟動地點*/
                map.selectEnableLocationData();
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
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle(getResources().getString(R.string.transport_to_wear))
                        .setMessage(getResources().getString(R.string.transport_to_wear_title))
                        .setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int j) {
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
                                    message.append("\"id\":\"");
                                    message.append(id);
                                    message.append("\",");
                                    message.append("\"name\":\"");
                                    message.append(name);
                                    message.append("\",");
                                    message.append("\"lat\":\"");
                                    message.append(lat);
                                    message.append("\",");
                                    message.append("\"lon\":\"");
                                    message.append(lon);
                                    message.append("\",");
                                    message.append("\"state\":\"");
                                    message.append(state);
                                    message.append("\"");
                                    if (i == listItem.size() - 1) {
                                        message.append("}]");
                                    } else {
                                        message.append("},{");
                                    }
                                }

                                Log.i("message", message + "");
                                googleClient.connect();
                            }
                        }).show();

            }
        });

        /*說明*/
        btIntroduction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Introduction.class);
                startActivity(intent);
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

    }

    /* 列出地點 */
    private void allLocation() {
        map = new MainActivityPresenterImpl(this,db);
        map.selectLocationData();

    }

    private void showLocationDialog(final int position) {
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
                if (isChecked) { // 開啟
                    map.selectUpdateEnableState(1,listItem.get(position).getLocationID());
                } else { // 關閉
                    map.selectUpdateEnableState(0,listItem.get(position).getLocationID());
                }
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
                                map.selectDeleteLocation(listItem.get(position).getLocationID());
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
        Log.i("onConnected", "onConnected");
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
    public void setLocationData(List<ListItem> data) {
        listItem = data;
        CustomRecyclerViewAdapter adapter = new CustomRecyclerViewAdapter(this, listItem);
        lvLocation.setAdapter(adapter);
    }

    @Override
    public void setEnableLocationData(List<ListItem> data) {
        LocationGPS locationGPS = new LocationGPS(MainActivity.this, data, tvMyCarLocation, tvState);
        locationGPS.startConnect();
    }
}