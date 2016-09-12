package tw.com.terryyamg.findmycar;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class MarkMyCarModelImpl implements MarkMyCarModel {
    @Override
    public void getEnableLocationData(SQLiteDatabase db) {
        List<ListItem> listItem = new ArrayList<>();
        String select = "SELECT * FROM location WHERE state = '1'";
        Cursor cursor = db.rawQuery(select, null);
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
