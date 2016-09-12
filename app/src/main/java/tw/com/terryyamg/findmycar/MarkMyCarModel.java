package tw.com.terryyamg.findmycar;

import android.database.sqlite.SQLiteDatabase;

public interface MarkMyCarModel {
    void getEnableLocationData(SQLiteDatabase db);
}
