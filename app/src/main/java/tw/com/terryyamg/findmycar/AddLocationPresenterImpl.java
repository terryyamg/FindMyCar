package tw.com.terryyamg.findmycar;

import android.database.sqlite.SQLiteDatabase;

public class AddLocationPresenterImpl implements AddLocationPresenter{
    private SQLiteDatabase mDB;
    private AddLocationModel mModel;

    public AddLocationPresenterImpl(SQLiteDatabase db){
        this.mDB = db;
        this.mModel = new AddLocationModelImpl();
    }

    @Override
    public void selectInsertLocation(String name, String latitude, String longitude) {
        mModel.insertLocation(mDB,name,latitude,longitude);
    }

    @Override
    public void selectUpdateLocation(String name, String latitude, String longitude, int locationID) {
        mModel.updateLocation(mDB,name,latitude,longitude,locationID);
    }
}
