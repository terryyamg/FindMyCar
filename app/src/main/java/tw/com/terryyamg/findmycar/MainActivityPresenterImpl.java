package tw.com.terryyamg.findmycar;

import android.database.sqlite.SQLiteDatabase;

public class MainActivityPresenterImpl implements MainActivityPresenter{
    private MainActivityView mView;
    private MainActivityModel mModel;
    private SQLiteDatabase mDB;

    public MainActivityPresenterImpl(MainActivityView view,SQLiteDatabase db){
        this.mView = view;
        this.mDB =db;
        this.mModel = new MainActivityModelImpl();
    }

    @Override
    public void selectLocationData() {
        mModel.getLocationData(mView,mDB);
    }

    @Override
    public void selectEnableLocationData() {
        mModel.getEnableLocationData(mView,mDB);
    }

    @Override
    public void selectUpdateEnableState(int state, int locationID) {
        mModel.updateEnableState(mDB,state,locationID);
    }

    @Override
    public void selectDeleteLocation(int locationID) {
        mModel.deleteLocation(mDB,locationID);
    }
}
