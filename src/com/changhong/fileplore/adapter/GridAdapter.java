package com.changhong.fileplore.adapter;

import java.util.ArrayList;

import com.changhong.fileplore.utils.Content;
import com.example.fileplore.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GridAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private ArrayList<Content> pictures;
	private int type = 0;

	/**
	 * 
	 * @param pictures
	 *            适配列表
	 * @param context
	 * @param type
	 *            按钮id(R.id.XX)
	 */
	public GridAdapter(ArrayList<Content> pictures, Context context, int type) {
		super();
		this.pictures = pictures;
		inflater = LayoutInflater.from(context);
		this.type = type;

	}

	@Override
	public int getCount() {
		if (null != pictures) {
			return pictures.size();
		} else {
			return 0;
		}
	}

	@Override
	public Object getItem(int position) {
		return pictures.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.griditem_classify, null);
			viewHolder = new ViewHolder();
			viewHolder.title = (TextView) convertView.findViewById(R.id.griditem_text);
			viewHolder.image = (ImageView) convertView.findViewById(R.id.griditem_img);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.title.setText(pictures.get(position).getTitle());
		if (null != pictures.get(position).getImg())
			viewHolder.image.setImageBitmap(pictures.get(position).getImg());
		else if (type == R.id.img_music)
			viewHolder.image.setBackgroundResource(R.drawable.file_icon_music);
		else if (type == R.id.img_movie)
			viewHolder.image.setBackgroundResource(R.drawable.file_icon_movie);
		else if (type == R.id.img_txt)
			viewHolder.image.setBackgroundResource(R.drawable.file_icon_txt);
		else if (type == R.id.img_zip)
			viewHolder.image.setBackgroundResource(R.drawable.file_icon_zip);
		else if (type == R.id.img_photo)
			viewHolder.image.setBackgroundResource(R.drawable.file_icon_photo);
		else
			viewHolder.image.setBackgroundResource(R.drawable.file_icon_unknown);
		return convertView;
	}

	class ViewHolder {
		public TextView title;
		public ImageView image;
	}

}
