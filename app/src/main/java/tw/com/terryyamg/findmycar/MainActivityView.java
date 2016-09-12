package tw.com.terryyamg.findmycar;

import java.util.List;

public interface MainActivityView {
    void setLocationData(List<ListItem> data);

    void setEnableLocationData(List<ListItem> data);
}
