package tw.com.terryyamg.findmycar;

import android.database.sqlite.SQLiteDatabase;

public interface MainActivityModel {
    void getLocationData(MainActivityView view, SQLiteDatabase db);

    void getEnableLocationData(MainActivityView view, SQLiteDatabase db);

    void updateEnableState(SQLiteDatabase db,int state,int locationID);

    void deleteLocation(SQLiteDatabase db,int locationID);
}
