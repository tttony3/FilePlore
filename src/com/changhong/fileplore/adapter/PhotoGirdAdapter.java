package com.changhong.fileplore.adapter;

import java.util.ArrayList;

import com.changhong.fileplore.utils.Content;
import com.changhong.fileplore.R;
import com.changhong.fileplore.application.MyApp;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotoGirdAdapter extends BaseAdapter {
	DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.file_icon_photo)
			.showImageForEmptyUri(R.drawable.file_icon_photo).showImageOnFail(R.drawable.file_icon_photo)
			.cacheInMemory(true).cacheOnDisk(true).bitmapConfig(Bitmap.Config.RGB_565) // 设置图片的解码类型
			.build();
	ArrayList<Content> results;
	private LayoutInflater inflater;
	ImageLoader imageLoader;

	public void updateList(ArrayList<Content> results) {
		this.results = results;
		notifyDataSetChanged();
	}

	public PhotoGirdAdapter(ArrayList<Content> results, Context context, ImageLoader imageLoader) {
		this.results = results;
		this.imageLoader = imageLoader;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return results.size();
	}

	@Override
	public Object getItem(int position) {
		return results.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.griditem_classify, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.title = (TextView) convertView.findViewById(R.id.griditem_text);
			viewHolder.image = (ImageView) convertView.findViewById(R.id.griditem_img);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		// 将图片显示任务增加到执行池，图片将被显示到ImageView当轮到此ImageView
		viewHolder.title.setText(results.get(position).getTitle());
		// imageLoader.displayImage("file://" + results.get(position).getDir(),
		// viewHolder.image, options);
		final String path = results.get(position).getDir();
		final String name = results.get(position).getTitle();

		if (MyApp.fileSet.contains("cache" + name)) {
			Log.e("contains", "contains");
			imageLoader.displayImage("file://" + MyApp.storagepath + "cache" + name, viewHolder.image, options);
		} else {
			imageLoader.displayImage("file://" + path, viewHolder.image, options);

		}
		return convertView;
	}
}

class ViewHolder {
	public TextView title;
	public ImageView image;
}