package com.changhong.fileplore.adapter;

import java.util.ArrayList;
import java.util.List;

import com.changhong.alljoyn.simpleclient.DeviceInfo;
import com.changhong.fileplore.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class NetDevListAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private List<DeviceInfo> list;

	public NetDevListAdapter(List<DeviceInfo> list, Context context) {
		super();
		if (null==list)
			this.list=new ArrayList<DeviceInfo>();
		else 
			this.list = list;
		inflater = LayoutInflater.from(context);

	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.listitem_netdev, null);
			viewHolder = new ViewHolder();
			viewHolder.name = (TextView) convertView.findViewById(R.id.tv_net_devname);
			viewHolder.url = (TextView) convertView.findViewById(R.id.tv_net_devurl);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.name.setText(list.get(position).getM_devicename());
		viewHolder.url.setText(list.get(position).getM_httpserverurl());
		
		return convertView;
	}

	class ViewHolder {
		public TextView name;
		public TextView url;
		
	}

	public void updatelistview(List<DeviceInfo> list2) {
		list=list2;
		notifyDataSetChanged();
		
	}
}
