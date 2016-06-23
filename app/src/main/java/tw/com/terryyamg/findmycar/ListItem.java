package tw.com.terryyamg.findmycar;

public class ListItem {

	private String locationName, state;

	public ListItem() {
	}

	public ListItem(String locationName,String state) {
		this.locationName = locationName;
		this.state = state;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
}


