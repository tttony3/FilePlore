package com.changhong.fileplore.adapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.changhong.fileplore.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class PloreListAdapter extends BaseAdapter {

	private ArrayList<File> files;
	private Boolean[] checkbox_list;

	public Boolean[] getCheckBox_List() {
		return checkbox_list;
	}

	public void setcheckbox_list(Boolean[] checkbox_list) {
		this.checkbox_list = checkbox_list;
	}

	private LayoutInflater mInflater;
	private boolean show_cb = false;

	public PloreListAdapter(Context context, ArrayList<File> files, boolean isRoot) {
		this.files = files;
		checkbox_list = new Boolean[files.size()];
		for (int i = 0; i < checkbox_list.length; i++) {
			checkbox_list[i] = false;
		}
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return files.size();
	}

	@Override
	public Object getItem(int position) {
		return files.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.listitem_plore, null);
			convertView.setTag(viewHolder);
			viewHolder.cb = (CheckBox) convertView.findViewById(R.id.cb);
			viewHolder.name = (TextView) convertView.findViewById(R.id.filename);
			viewHolder.time = (TextView) convertView.findViewById(R.id.lasttime);
			viewHolder.img = (ImageView) convertView.findViewById(R.id.fileimg);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		File file = (File) getItem(position);
		String fileName = ((File) file).getName();
		viewHolder.name.setText(fileName);
		if (show_cb) {
			viewHolder.cb.setChecked(checkbox_list[position]);
			viewHolder.cb.setVisibility(View.VISIBLE);

		} else {
			viewHolder.cb.setChecked(checkbox_list[position]);
			viewHolder.cb.setVisibility(View.GONE);

		}
		viewHolder.cb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (((CheckBox) v).isChecked()) {
					checkbox_list[position] = true;
				} else
					checkbox_list[position] = false;
			}
		});
		if (((File) file).isDirectory()) {

			viewHolder.name.setText(fileName);
			viewHolder.time.setVisibility(View.GONE);
			viewHolder.img.setImageResource(R.drawable.file_icon_folder);
		} else {

			viewHolder.name.setText(fileName);
			viewHolder.img.setImageResource(R.drawable.file_icon_txt);
			viewHolder.time.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(((File) file).lastModified()));
		}

		return convertView;
	}

	public boolean isShow_cb() {
		return show_cb;
	}

	public void setShow_cb(boolean show_cb) {
		this.show_cb = show_cb;
	}

	class ViewHolder {
		private CheckBox cb;
		private ImageView img;
		private TextView name;
		private TextView time;
	}
}
