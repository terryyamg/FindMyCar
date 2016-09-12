package tw.com.terryyamg.findmycar;

import android.database.sqlite.SQLiteDatabase;

public class AddLocationModelImpl implements AddLocationModel{
    @Override
    public void insertLocation(SQLiteDatabase db, String name, String latitude, String longitude) {
        String insert = "INSERT INTO location(name, latitude, longitude) VALUES ('"
                + name + "','" + latitude + "','" + longitude + "')";
        db.execSQL(insert);
    }

    @Override
    public void updateLocation(SQLiteDatabase db, String name, String latitude, String longitude,int locationID) {
        String update = "UPDATE location SET name = '" + name
                + "',latitude = '" + latitude + "',longitude = '" + longitude
                + "' WHERE id = '" + locationID + "'";
        db.execSQL(update);
    }
}
