package tw.com.terryyamg.findmycar;

public interface AddLocationPresenter {
    void selectInsertLocation(String name, String latitude, String longitude);

    void selectUpdateLocation(String name, String latitude, String longitude,int locationID);
}
