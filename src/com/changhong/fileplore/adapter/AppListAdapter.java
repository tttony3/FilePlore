package com.changhong.fileplore.adapter;

import java.util.ArrayList;

import com.changhong.fileplore.R;
import com.changhong.fileplore.data.AppInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class AppListAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<AppInfo> appList;

	public AppListAdapter(Context context, ArrayList<AppInfo> appList) {
		this.context = context;
		this.appList = appList;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return appList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return appList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView =  LayoutInflater.from(context).inflate(R.layout.listitem_plore, null);
			convertView.setTag(viewHolder);
			viewHolder.cb = (CheckBox) convertView.findViewById(R.id.cb);
			viewHolder.name = (TextView) convertView.findViewById(R.id.filename);
			viewHolder.time = (TextView) convertView.findViewById(R.id.lasttime);
			viewHolder.img = (ImageView) convertView.findViewById(R.id.fileimg);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		AppInfo tmp = (AppInfo) getItem(position);
		viewHolder.name.setText(tmp.appName);
		viewHolder.img.setImageDrawable(tmp.appIcon);;
		viewHolder.time.setText(tmp.versionName);
		return convertView;
	}
	
	class ViewHolder {
		private CheckBox cb;
		private ImageView img;
		private TextView name;
		private TextView time;
	}
}
