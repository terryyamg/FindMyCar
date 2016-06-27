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
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static tw.com.terryyamg.findmycar.SetInfo.STATE_INFO;

public class MainActivity extends Activity {
    private Function funHelper = new Function(this);
    private SQLiteDatabase db;
    private DBManager dbHelper;
    private Handler handler = new Handler();

    private List<ListItem> listItem;
    private RecyclerView lvLocation;
    private CustomRecyclerViewAdapter adapter;

    private TextView tvMyCarLocation, tvState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvMyCarLocation = (TextView) findViewById(R.id.tvMyCarLocation);
        tvState = (TextView) findViewById(R.id.tvState);
        ImageButton ibMark = (ImageButton) findViewById(R.id.ibMark);
        Button btMap = (Button) findViewById(R.id.btMap);
        Button btAdded = (Button) findViewById(R.id.btAdded);
        lvLocation = (RecyclerView) findViewById(R.id.lvLoaction);

        dbHelper = new DBManager(this);
        dbHelper.openDatabase();
        db = dbHelper.getDatabase();

        tvMyCarLocation.setText(funHelper.getString("locationName"));

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || !locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(this, "前往開啟GPS", Toast.LENGTH_SHORT).show();
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
//                getCoordinatesGPS();
                LocationGPS locationGPS = new LocationGPS(MainActivity.this, listItem, tvMyCarLocation);
                locationGPS.startConnect();
                STATE_INFO = "定位中。。。";
            }
        });

		/* 地圖位置 */
        btMap.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapMyCar.class);
                intent.putExtra("toMapMyCar", 0);
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

        // 更新資訊
        handler.removeCallbacks(updateTimer);
        handler.postDelayed(updateTimer, 5000);

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

        adapter.notifyDataSetChanged();

    }

    private void showLoactionDialog(final int position) {
        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_item);

        final Switch swEnable = (Switch) dialog.findViewById(R.id.swEnable);
        final Button btUpdate = (Button) dialog.findViewById(R.id.btUpdate);
        final Button btDelete = (Button) dialog.findViewById(R.id.btDelete);

        // 啟動或關閉
        switch (listItem.get(position).getState()) {
            case "Enable":
                swEnable.setChecked(true);
                break;
            case "Disable":
                swEnable.setChecked(false);
                break;
            default:
                break;
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
                        R.string.ConfirmDelete));

                alertDialog.setPositiveButton(R.string.confirmYes,
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
                alertDialog.setNegativeButton(R.string.confirmNo,
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

    // 更新資訊
    private Runnable updateTimer = new Runnable() {
        public void run() {
            tvState.setText(STATE_INFO);
            handler.postDelayed(this, 1000);
        }
    };

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
}