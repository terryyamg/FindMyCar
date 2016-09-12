package tw.com.terryyamg.findmycar;

public interface MainActivityPresenter {
    void selectLocationData();

    void selectEnableLocationData();

    void selectUpdateEnableState(int state,int locationID);

    void selectDeleteLocation(int locationID);
}
