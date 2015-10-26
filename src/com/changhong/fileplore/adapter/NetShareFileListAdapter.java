package com.changhong.fileplore.adapter;

import java.util.ArrayList;
import java.util.List;

import com.changhong.alljoyn.simpleclient.DeviceInfo;
import com.changhong.fileplore.R;
import com.changhong.synergystorage.javadata.JavaFile;
import com.changhong.synergystorage.javadata.JavaFolder;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NetShareFileListAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private List<JavaFile> fileList;
	private List<JavaFolder> folderList;
	DeviceInfo devInfo;
	private ImageLoader imageLoader= ImageLoader.getInstance();
	public NetShareFileListAdapter(List<JavaFile> fileList, List<JavaFolder> folderList, DeviceInfo devInfo,
			Context context) {
		this.devInfo=devInfo;
		if (null == fileList) {
			this.fileList = new ArrayList<JavaFile>();
		} else {
			this.fileList = fileList;
		}
		if (null == folderList) {
			this.folderList = new ArrayList<JavaFolder>();
		} else {
			this.folderList = folderList;
		}
		inflater = LayoutInflater.from(context);

	}

	@Override
	public int getCount() {
		if (null == folderList)
			return fileList.size();
		if (null == fileList)
			return folderList.size();
		return fileList.size() + folderList.size();
	}

	@Override
	public Object getItem(int position) {
		return (position >= folderList.size()) ? fileList.get(position - folderList.size()) : folderList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.listitem_netfile, null);
			viewHolder = new ViewHolder();
			viewHolder.name = (TextView) convertView.findViewById(R.id.tv_net_filename);
			viewHolder.url = (TextView) convertView.findViewById(R.id.tv_net_fileurl);
			viewHolder.img = (ImageView) convertView.findViewById(R.id.im_file);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		if (position >= folderList.size()) {
			if (JavaFile.FileType.AUDIO == fileList.get(position - folderList.size()).getFileType())
				viewHolder.img.setImageResource(R.drawable.file_icon_music);
			else if (JavaFile.FileType.IMAGE == fileList.get(position - folderList.size()).getFileType()) {
				Log.e("url", fileList.get(position - folderList.size()).getLocation());
				imageLoader.displayImage(devInfo.getM_httpserverurl()+fileList.get(position - folderList.size()).getLocation(), viewHolder.img);
			//	viewHolder.img.setBackgroundResource(R.drawable.file_icon_photo);

			} else if (JavaFile.FileType.VIDEO == fileList.get(position - folderList.size()).getFileType())
				viewHolder.img.setImageResource(R.drawable.file_icon_movie);
			else if (JavaFile.FileType.OTHER == fileList.get(position - folderList.size()).getFileType())
				viewHolder.img.setImageResource(R.drawable.file_icon_unknown);

			viewHolder.name.setText(fileList.get(position - folderList.size()).getFullName());
			viewHolder.url.setText(fileList.get(position - folderList.size()).getLocation());
		} else if (position < folderList.size()) {
			viewHolder.img.setImageResource(R.drawable.file_icon_folder);
			viewHolder.name.setText(folderList.get(position).getName());
			viewHolder.url.setText(folderList.get(position).getLocation());
		}

		return convertView;
	}

	class ViewHolder {
		public ImageView img;
		public TextView name;
		public TextView url;

	}

	public void updatelistview(List<JavaFile> list1, List<JavaFolder> list2) {
		// Log.e("notify", list1.size()+"");
		fileList.clear();
		folderList.clear();
		if (list1 != null)
			fileList.addAll(list1);
		if (list2 != null)
			folderList.addAll(list2);
	
		notifyDataSetChanged();

	}

}
