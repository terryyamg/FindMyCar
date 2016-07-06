package tw.com.terryyamg.findmycar;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by terryyamg on 2016/6/27.
 */
public class CustomRecyclerViewAdapter extends RecyclerView.Adapter<CustomRecyclerViewAdapter.CustomViewHolder> {
    private Context mContext;
    private List<ListItem> mItems;

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        public TextView tvLocationName, tvState;

        public CustomViewHolder(View view) {
            super(view);
            tvLocationName = (TextView) view.findViewById(R.id.tvLocationName);
            tvState = (TextView) view.findViewById(R.id.tvState);
        }
    }

    public CustomRecyclerViewAdapter(Context context, List<ListItem> items) {
        this.mItems = items;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //設定item layout
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_list_view, parent, false);

        return new CustomViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        //放入資料
        holder.tvLocationName.setText(mItems.get(position).getLocationName());
        switch (mItems.get(position).getState()) {
            case 1:
                holder.tvState.setText(mContext.getResources().getString(R.string.state_enalbe));
                break;
            case 0:
                holder.tvState.setText(mContext.getResources().getString(R.string.state_disalbe));
                break;
            default:
                break;
        }

    }

    @Override
    public int getItemCount() {
        //取得長度
        return mItems.size();
    }
}
