package com.changhong.fileplore.adapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.changhong.alljoyn.simpleservice.FC_GetShareFile;
import com.changhong.fileplore.R;
import com.changhong.fileplore.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class PloreListAdapter extends BaseAdapter {
	DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.file_icon_photo)
			.showImageForEmptyUri(R.drawable.file_icon_photo).showImageOnFail(R.drawable.file_icon_photo)
			.cacheInMemory(true).cacheOnDisk(true).bitmapConfig(Bitmap.Config.RGB_565).displayer(new RoundedBitmapDisplayer(20)) // 设置图片的解码类型
			.build();
	ImageLoader imageLoader;
	static final private int DOC = 1;
	static final private int MUSIC = 2;
	static final private int PHOTO = 3;
	static final private int ZIP = 4;
	static final private int MOVIE = 5;
	static final private int UNKNOW = 6;

	private List<File> files;
	private Boolean[] checkbox_list;

	public Boolean[] getCheckBox_List() {
		return checkbox_list;
	}

	public void setcheckbox_list(Boolean[] checkbox_list) {
		this.checkbox_list = checkbox_list;
	}

	private LayoutInflater mInflater;
	private boolean show_cb = false;
	private Context context;
	String download_Path = Environment.getExternalStorageDirectory().getAbsolutePath();
	String storagepath = download_Path + "/FilePlore/cache/";
	File folder = new File(storagepath);
	File[] folderfiles ;
	Set<String> fileSet ;
	public PloreListAdapter(Context context, List<File> files, boolean isRoot, ImageLoader imageLoader) {
		this.imageLoader = imageLoader;
		this.files = files;
		this.context = context;
		checkbox_list = new Boolean[files.size()];
		for (int i = 0; i < checkbox_list.length; i++) {
			checkbox_list[i] = false;
		}
		mInflater = LayoutInflater.from(context);
		if(!folder.exists()){
			folder.mkdir();
		}
		folderfiles = folder.listFiles();
		fileSet = new HashSet<String>();
		for (int i = 0; i < folderfiles.length; i++) {
			fileSet.add(folderfiles[i].getName());
		}
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
		final ViewHolder viewHolder;
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
		if (fileName.toLowerCase().equals("sdcard0"))
			viewHolder.name.setText("手机空间");
		else if (fileName.toLowerCase().equals("sdcard1"))
			viewHolder.name.setText("SD卡空间");
		else
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

			viewHolder.time.setVisibility(View.GONE);
			viewHolder.img.setImageResource(R.drawable.file_icon_folder);
		} else {

			viewHolder.time.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(((File) file).lastModified()));

			switch (getMIMEType(fileName)) {
			case MOVIE:
				final String path1 = file.getPath();
				final String name1 = file.getName();
				imageLoader.displayImage("file://"+path1, viewHolder.img, options);
			//	viewHolder.img.setImageResource(R.drawable.file_icon_movie);
				break;
			case MUSIC:
				viewHolder.img.setImageResource(R.drawable.file_icon_music);
				break;
			case PHOTO:
				final String path = file.getPath();
				final String name = file.getName();
//				if (fileSet.contains("cache" + name)) {
//					imageLoader.displayImage("file://" + storagepath+"cache"+name, viewHolder.img, options);
//				} else {
//					 new Thread(new Runnable() {
//						public void run() {
//							Bitmap btm = Utils.getImageThumbnail(path, 70, 70);
//							saveBitmap2file(btm, "cache" + name);
//							imageLoader.displayImage("file://" + storagepath+"cache"+name, viewHolder.img, options);
//
//						}
//					}).start();
//					
//				}
				imageLoader.displayImage("file://"+path, viewHolder.img, options);
				// viewHolder.img.setImageBitmap(

				break;
			case DOC:
				viewHolder.img.setImageResource(R.drawable.file_icon_txt);
				break;
			case UNKNOW:
				viewHolder.img.setImageResource(R.drawable.file_icon_unknown);
				break;
			case ZIP:
				viewHolder.img.setImageResource(R.drawable.file_icon_zip);
				break;

			default:
				break;
			}
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

	public int getMIMEType(String name) {

		String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
		if (end.equals("m4a") || end.equals("mp3") || end.equals("wav")) {
			return MUSIC;
		} else if (end.equals("mp4") || end.equals("3gp")) {
			return MOVIE;
		} else if (end.equals("jpg") || end.equals("png") || end.equals("jpeg") || end.equals("bmp")
				|| end.equals("gif")) {
			return PHOTO;
		} else if (end.equals("zip") || end.equals("rar")) {
			return ZIP;
		} else if (end.equals("doc") || end.equals("docx") || end.equals("txt")) {
			return DOC;
		}
		return UNKNOW;
	}

	boolean saveBitmap2file(Bitmap bmp, String filename) {
		CompressFormat format = Bitmap.CompressFormat.JPEG;
		int quality = 100;
		OutputStream stream = null;
		try {
			stream = new FileOutputStream(storagepath + filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return bmp.compress(format, quality, stream);
	}
}
