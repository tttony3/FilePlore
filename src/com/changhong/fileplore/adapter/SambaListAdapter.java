package com.changhong.fileplore.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;


import com.changhong.fileplore.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class SambaListAdapter extends BaseAdapter {

	private ArrayList<SmbFile> smbfiles;
	private LayoutInflater mInflater;

	public SambaListAdapter(Context context, ArrayList<SmbFile> files) {
		this.smbfiles = files;
		mInflater = LayoutInflater.from(context);

	}

	@Override
	public int getCount() {
		return smbfiles.size();
	}

	@Override
	public Object getItem(int position) {
		return smbfiles.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.listitem_plore, null);
			convertView.setTag(viewHolder);
			viewHolder.name = (TextView) convertView.findViewById(R.id.filename);
			viewHolder.time = (TextView) convertView.findViewById(R.id.lasttime);
			viewHolder.img = (ImageView) convertView.findViewById(R.id.fileimg);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		SmbFile file = (SmbFile) getItem(position);
		String fileName = file.getName();
		viewHolder.name.setText(fileName);
		try {
			if (file.isDirectory()) {
				viewHolder.name.setText(fileName);
				viewHolder.time.setVisibility(View.GONE);
				viewHolder.img.setImageResource(R.drawable.file_icon_folder);
			} else {
				viewHolder.name.setText(fileName);
				viewHolder.img.setImageResource(R.drawable.file_icon_txt);
				try {
					viewHolder.time.setText(new SimpleDateFormat("yyyy/MM/dd").format(file.lastModified()));
				} catch (SmbException e) {

					e.printStackTrace();
				}
			}
		} catch (SmbException e) {

			e.printStackTrace();
		}

		return convertView;
	}

	class ViewHolder {
		private ImageView img;
		private TextView name;
		private TextView time;
	}
}
