package tw.com.terryyamg.findmycar;

public class ListItem {

	private int locationID,state;
	private String locationName;
	private double latitude,longitude;

	public ListItem() {
	}

	public ListItem(int locationID,String locationName,double latitude,double longitude,int state) {
		this.locationID = locationID;
		this.locationName = locationName;
		this.latitude = latitude;
		this.longitude = longitude;
		this.state = state;
	}

	// 地點id
	public int getLocationID(){
		return locationID;
	}

	public void setLocationID(int locationID) {
		this.locationID = locationID;
	}

	// 地點名稱
	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	// 緯度
	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	// 經度
	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	// 狀態
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

}


