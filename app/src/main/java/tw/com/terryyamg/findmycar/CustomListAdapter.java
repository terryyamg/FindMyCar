package tw.com.terryyamg.findmycar;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class CustomListAdapter extends BaseAdapter {
	private Activity activity;
	private LayoutInflater inflater;
	private List<ListItem> items;
	ListItem m;
	
	public CustomListAdapter(Activity activity, List<ListItem> items) {
		this.activity = activity;
		this.items = items;
	}

	public int getCount() {
		return items.size();
	}

	public Object getItem(int location) {
		return items.get(location);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		if (inflater == null)
			inflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null)
			convertView = inflater.inflate(R.layout.custom_list_view, null);

		TextView tvLocationName = (TextView) convertView.findViewById(R.id.tvLocationName);
		TextView tvState = (TextView) convertView.findViewById(R.id.tvState);

		m = items.get(position);
		
		tvLocationName.setText(m.getLocationName());
		if(m.getState().equals(activity.getResources().getString(R.string.stateEnalbe))){ //啟動
			tvState.setTextColor(0xFF176CED);
		}else{ //關閉
			tvState.setTextColor(0xFF990000);
		}
		tvState.setText(m.getState());


		return convertView;
	}

}
