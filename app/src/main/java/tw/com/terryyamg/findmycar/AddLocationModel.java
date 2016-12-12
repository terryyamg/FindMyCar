package tw.com.terryyamg.findmycar;

import android.database.sqlite.SQLiteDatabase;

public interface AddLocationModel {
    void insertLocation(SQLiteDatabase db,String name, String latitude, String longitude);

    void updateLocation(SQLiteDatabase db,String name, String latitude, String longitude,int locationID);
}
