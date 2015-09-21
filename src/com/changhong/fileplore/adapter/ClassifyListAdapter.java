package com.changhong.fileplore.adapter;

import java.util.ArrayList;

import com.changhong.fileplore.utils.Content;
import com.changhong.fileplore.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ClassifyListAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private ArrayList<Content> content;
	private int type;

	public ClassifyListAdapter(ArrayList<Content> content, Context context, int type) {
		super();
		this.content = content;
		inflater = LayoutInflater.from(context);
		this.type = type;

	}

	@Override
	public int getCount() {
		return content.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return content.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.listitem_plore, null);
			viewHolder = new ViewHolder();
			viewHolder.title = (TextView) convertView.findViewById(R.id.filename);
			viewHolder.singer = (TextView) convertView.findViewById(R.id.lasttime);
			viewHolder.image = (ImageView) convertView.findViewById(R.id.fileimg);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.title.setText(content.get(position).getTitle());
		if (type == R.id.img_music)
			viewHolder.singer.setText(content.get(position).getSinger());
		if (type == R.id.img_txt)
			viewHolder.singer.setText(content.get(position).getTime());
		if (null != content.get(position).getImg())
			viewHolder.image.setImageBitmap(content.get(position).getImg());
		else if (type == R.id.img_music)
			viewHolder.image.setBackgroundResource(R.drawable.file_icon_music);
		else if (type == R.id.img_txt)
			viewHolder.image.setBackgroundResource(R.drawable.file_icon_txt);
		else if (type == R.id.img_zip)
			viewHolder.image.setBackgroundResource(R.drawable.file_icon_zip);
		else if (type == R.id.img_apk)
			viewHolder.image.setBackgroundResource(R.drawable.file_icon_apk);
		return convertView;
	}

	class ViewHolder {
		public TextView title;
		public TextView singer;
		public ImageView image;
	}

	public void updateList(ArrayList<Content> results) {
		content = results;
		notifyDataSetChanged();
		
	}
}
