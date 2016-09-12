package tw.com.terryyamg.findmycar;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivityModelImpl implements MainActivityModel {
    @Override
    public void getLocationData(MainActivityView view, SQLiteDatabase db) {
        List<ListItem> listItem = new ArrayList<>();
        selectData(view,db,listItem,"SELECT * FROM location");
        view.setLocationData(listItem);
    }

    @Override
    public void getEnableLocationData(MainActivityView view, SQLiteDatabase db) {
        List<ListItem> listItem = new ArrayList<>();
        selectData(view,db,listItem,"SELECT * FROM location WHERE state = '1'");
        view.setEnableLocationData(listItem);
    }

    @Override
    public void updateEnableState(SQLiteDatabase db, int state, int locationID) {
        String update = "UPDATE location SET state = '"+state+"' WHERE id = '" + locationID + "'";
        db.execSQL(update);
    }

    @Override
    public void deleteLocation(SQLiteDatabase db, int locationID) {
        String delete = "DELETE FROM location WHERE id = '" + locationID + "'";
        db.execSQL(delete);
    }

    private void selectData(MainActivityView view, SQLiteDatabase db,List<ListItem> listItem,String sql){

        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        if (cursor.getCount() != 0) {
            do {
                ListItem li = new ListItem();
                li.setLocationID(cursor.getInt(0));// 地點id
                li.setLocationName(cursor.getString(1));// 地點名稱
                li.setLatitude(cursor.getDouble(2));// 緯度
                li.setLongitude(cursor.getDouble(3));// 經度
                li.setState(cursor.getInt(4));// 狀態
                listItem.add(li);

            } while (cursor.moveToNext());

        }
        cursor.close();
    }
}
