package tw.com.terryyamg.findmycar;

public class ListItem {

	private int locationID;
	private String locationName, state;
	private double latitude,longitude;

	public ListItem() {
	}

	public ListItem(int locationID, String locationName, double latitude, double longitude, String state) {
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
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
}


