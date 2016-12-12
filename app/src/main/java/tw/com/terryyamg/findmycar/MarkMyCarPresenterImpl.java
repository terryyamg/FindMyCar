package tw.com.terryyamg.findmycar;

import android.database.sqlite.SQLiteDatabase;

public class MarkMyCarPresenterImpl implements MarkMyCarPresenter{
    private MarkMyCarModel mModel;
    private SQLiteDatabase mDB;

    public MarkMyCarPresenterImpl(SQLiteDatabase db){
        this.mDB = db;
        this.mModel = new MarkMyCarModelImpl();
    }

    @Override
    public void selectEnableLocationData() {
        mModel.getEnableLocationData(mDB);
    }
}
