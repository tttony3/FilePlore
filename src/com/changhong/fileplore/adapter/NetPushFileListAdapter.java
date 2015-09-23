package com.changhong.fileplore.adapter;

import java.util.ArrayList;
import java.util.List;

import com.changhong.fileplore.R;
import com.changhong.fileplore.utils.Utils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NetPushFileListAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private List<String> pushList;

	public NetPushFileListAdapter(List<String> pushList, Context context) {
		if (null == pushList) {
			this.pushList = new ArrayList<String>();
		} else {
			this.pushList = pushList;
		}

		inflater = LayoutInflater.from(context);

	}

	@Override
	public int getCount() {
		return pushList.size();
	}

	@Override
	public Object getItem(int position) {
		return pushList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.listitem_pushfile, null);
			viewHolder = new ViewHolder();
			viewHolder.name = (TextView) convertView.findViewById(R.id.tv_push_filename);
			viewHolder.url = (TextView) convertView.findViewById(R.id.tv_push_fileurl);
			viewHolder.img = (ImageView) convertView.findViewById(R.id.im_pushfile);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		String loc = pushList.get(position);
		String type = Utils.getMIMEType(loc);
		if (type == "video") {
			viewHolder.img.setBackgroundResource(R.drawable.file_icon_movie);
		} else if (type == "audio") {
			viewHolder.img.setBackgroundResource(R.drawable.file_icon_music);
		} else if (type == "image") {
			viewHolder.img.setBackgroundResource(R.drawable.file_icon_photo);
		} else if (type == "*") {
			viewHolder.img.setBackgroundResource(R.drawable.file_icon_unknown);
		}

		viewHolder.name.setText(loc.subSequence(loc.lastIndexOf("/") + 1, loc.length()));
		viewHolder.url.setText(loc);

		return convertView;
	}

	class ViewHolder {
		public ImageView img;
		public TextView name;
		public TextView url;

	}

	public void updatelistview(List<String> list) {
		// Log.e("notify", list1.size()+"");
		pushList.clear();
		if (list != null)
			pushList.addAll(list);
		notifyDataSetChanged();

	}

}
