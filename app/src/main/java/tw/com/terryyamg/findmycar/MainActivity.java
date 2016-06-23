package tw.com.terryyamg.findmycar;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ListView;
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

	private LocationManager locationManager;
	private LocationListener locationListener;

	private List<ListItem> listItem;
	private ListView lvLoaction;
	private CustomListAdapter adapter;

	private TextView tvMyCarLocation, tvState;

	private int[] locationID, locationState;
	private String[] locationName;
	private double[] latitude, longitude;
	private int length;
	private String showLocationName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tvMyCarLocation = (TextView) findViewById(R.id.tvMyCarLocation);
		tvState = (TextView) findViewById(R.id.tvState);
		ImageButton ibMark = (ImageButton) findViewById(R.id.ibMark);
		Button btMap = (Button) findViewById(R.id.btMap);
		Button btAdded = (Button) findViewById(R.id.btAdded);
		lvLoaction = (ListView) findViewById(R.id.lvLoaction);

		dbHelper = new DBManager(this);
		dbHelper.openDatabase();
		db = dbHelper.getDatabase();

		funHelper.getString("locationName");
		tvMyCarLocation.setText(showLocationName);

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

		lvLoaction.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				showLoactionDialog(position);

			}
		});

		/* 標計車子位置 */
		ibMark.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				getCoordinatesGPS();
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

	/* 取得座標 */
	private void getCoordinatesGPS() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationListener = new getLocationListener();
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				5000, 10, locationListener);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);

	}

	private class getLocationListener implements LocationListener {

		public void onLocationChanged(Location current) {
			if (current == null) {
				return;
			}

			Location dest = new Location(current); // 取得現在位置

			float distance, min = 0;
			int minNumber = 0;
			for (int i = 0; i < length; i++) {
				dest.setLatitude(latitude[i]);
				dest.setLongitude(longitude[i]);
				// 計算現在與所有地點距離 取出最近的
				distance = current.distanceTo(dest);
				if (i == 0) {
					min = distance;
				}
				if (min >= distance) {
					min = distance;
					minNumber = i;
				}
			}
			STATE_INFO = "定位完成";
			tvMyCarLocation.setText(locationName[minNumber]);

			funHelper.setString("locationName", locationName[minNumber]);
			funHelper.setString("latitude", Double.toString(latitude[minNumber]));
			funHelper.setString("longitude", Double.toString(longitude[minNumber]));

			if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				return;
			}
			locationManager.removeUpdates(locationListener); // 關閉gps

		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

	/* 列出地點 */
	private void allLocation() {

		listItem = new ArrayList<>();
		adapter = new CustomListAdapter(this, listItem);
		lvLoaction.setAdapter(adapter);

		String select = "SELECT * FROM location";
		Cursor cursor = db.rawQuery(select, null);
		cursor.moveToFirst();
		try {
			int count = 0;
			length = cursor.getCount();
			locationID = new int[length]; // 地點id
			locationName = new String[length]; // 地點名稱
			latitude = new double[length]; // 緯度
			longitude = new double[length]; // 經度
			locationState = new int[length]; // 狀態
			do {
				locationID[count] = cursor.getInt(0);
				locationName[count] = cursor.getString(1);
				latitude[count] = cursor.getDouble(2);
				longitude[count] = cursor.getDouble(3);
				locationState[count] = cursor.getInt(4);

				ListItem li = new ListItem();
				li.setLocationName(locationName[count]);
				String state = "";
				switch (locationState[count]) {
				case 1:
					state = getResources().getString(R.string.stateEnalbe);
					break;
				case 0:
					state = getResources().getString(R.string.stateDisalbe);
					break;
				default:
					break;
				}
				li.setState(state);

				listItem.add(li);

				count++;
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
		switch (locationState[position]) {
		case 1:
			swEnable.setChecked(true);
			break;
		default:
			swEnable.setChecked(false);
			break;
		}

		swEnable.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {

				String update;
				if (isChecked) { // 開啟
					update = "UPDATE location SET state = '1' WHERE id = '"
							+ locationID[position] + "'";
				} else { // 關閉
					update = "UPDATE location SET state = '0' WHERE id = '"
							+ locationID[position] + "'";
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
				intent.putExtra("locationID", locationID[position]);
				intent.putExtra("locationName", locationName[position]);
				intent.putExtra("latitude", latitude[position]);
				intent.putExtra("longitude", longitude[position]);
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
										+ locationID[position] + "'";
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