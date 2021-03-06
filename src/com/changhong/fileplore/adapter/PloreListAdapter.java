package com.changhong.fileplore.adapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import com.changhong.fileplore.R;
import com.changhong.fileplore.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.Visibility;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

public class PloreListAdapter extends BaseAdapter {
	
	public interface ImgOnClick{
		void onClick(View v,File file);
	}

	ImageLoader imageLoader;
	static final private int DOC = 1;
	static final private int MUSIC = 2;
	static final private int PHOTO = 3;
	static final private int ZIP = 4;
	static final private int MOVIE = 5;
	static final private int UNKNOW = 6;
	static final private int APK = 7;

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
	private boolean isAllSelect = false;

	public PloreListAdapter(Context context, List<File> files, boolean isRoot, ImageLoader imageLoader) {

		this.imageLoader = imageLoader;		
		this.context = context;
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
		
		final File file = (File) getItem(position);
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
		if (file.isDirectory()) {
			viewHolder.time.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(((File) file).lastModified()));
		//	viewHolder.time.setVisibility(View.GONE);
			viewHolder.img.setImageResource(R.drawable.file_icon_folder);
			viewHolder.img.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					((ImgOnClick)context).onClick(v, file);;
					
				}
			});
			viewHolder.img.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					if(!file.exists()||!file.canRead())
						return false;
					File[] files = file.listFiles();
					if(files.length ==0)
						return false;
					final File[] resultfiles = getMaxSort(files);

					LayoutInflater inflater = LayoutInflater.from(context);
					View layout = inflater.inflate(R.layout.dialog_filepreview, null);
					TableRow tr_1 = (TableRow) layout.findViewById(R.id.tr_filepreview_1);
					TableRow tr_2 = (TableRow) layout.findViewById(R.id.tr_filepreview_2);
					TableRow tr_3 = (TableRow) layout.findViewById(R.id.tr_filepreview_3);
					TableRow tr_4 = (TableRow) layout.findViewById(R.id.tr_filepreview_4);
					TextView tv_1 = (TextView) layout.findViewById(R.id.tv_filepreview_1);
					TextView tv_2 = (TextView) layout.findViewById(R.id.tv_filepreview_2);
					TextView tv_3 = (TextView) layout.findViewById(R.id.tv_filepreview_3);
					TextView tv_4 = (TextView) layout.findViewById(R.id.tv_filepreview_4);
					ImageView iv_1 = (ImageView) layout.findViewById(R.id.iv_filepreview_1);
					ImageView iv_2 = (ImageView) layout.findViewById(R.id.iv_filepreview_2);
					ImageView iv_3 = (ImageView) layout.findViewById(R.id.iv_filepreview_3);
					ImageView iv_4 = (ImageView) layout.findViewById(R.id.iv_filepreview_4);
					
					AlertDialog.Builder builder = new AlertDialog.Builder(context).setView(layout);
					switch(resultfiles.length){
					case 0:
						return false;
					case 1:
						tr_2.setVisibility(View.GONE);
						tr_3.setVisibility(View.GONE);
						tr_4.setVisibility(View.GONE);
						break;
					case 2:
						tr_3.setVisibility(View.GONE);
						tr_4.setVisibility(View.GONE);
						break;
					case 3:
						tr_4.setVisibility(View.GONE);
						break;
						}
						
						
					if (resultfiles.length >= 1 && resultfiles[0] != null) {
						tv_1.setText(resultfiles[0].getName());
						setImage(iv_1, resultfiles[0]);
						tr_1.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								context.startActivity(Utils.openFile(resultfiles[0]));

							}
						});
						
					}
					if (resultfiles.length >= 2 && resultfiles[1] != null) {
						tv_2.setText(resultfiles[1].getName());
						setImage(iv_2, resultfiles[1]);
					
						tr_2.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								context.startActivity(Utils.openFile(resultfiles[1]));

							}
						});
					}
					if (resultfiles.length >= 3 && resultfiles[2] != null) {
						tv_3.setText(resultfiles[2].getName());
						setImage(iv_3, resultfiles[2]);
						
						tr_3.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								context.startActivity(Utils.openFile(resultfiles[2]));

							}
						});
					}
					if (resultfiles.length >= 4 && resultfiles[3] != null) {
						tv_4.setText(resultfiles[3].getName());
						setImage(iv_4, resultfiles[3]);
						
						tr_4.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								context.startActivity(Utils.openFile(resultfiles[3]));

							}
						});
					}

					AlertDialog dia = builder.create();
					dia.show();
					float scale = context.getResources().getDisplayMetrics().density; 
					dia.getWindow().setLayout((int)(250 * scale + 0.5f), -2);

					return true;
				}

			});

		} else {
			viewHolder.img.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					viewHolder.name.callOnClick();
					
				}
			});
			viewHolder.time.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(((File) file).lastModified()));

			switch (getMIMEType(fileName)) {
			case MOVIE:
				final String path1 = file.getPath();
		//		final String name1 = file.getName();
				imageLoader.displayImage("file://" + path1, viewHolder.img);
				// viewHolder.img.setImageResource(R.drawable.file_icon_movie);
				break;
			case MUSIC:
				viewHolder.img.setImageResource(R.drawable.file_icon_music);
				break;
			case PHOTO:
				final String path = file.getPath();
				imageLoader.displayImage("file://" + path, viewHolder.img);

				
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
			case APK:
				viewHolder.img.setImageResource(R.drawable.file_icon_apk);
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
		else if (end.equals("apk") ) {
			return APK;
		}
		return UNKNOW;
	}

	private void setImage(ImageView iv_1, File file) {
		switch (getMIMEType(file.getName())) {
		case MOVIE:
			final String path1 = file.getPath();
		//	final String name1 = file.getName();
			imageLoader.displayImage("file://" + path1, iv_1);
			// viewHolder.img.setImageResource(R.drawable.file_icon_movie);
			break;
		case MUSIC:
			iv_1.setImageResource(R.drawable.file_icon_music);
			break;
		case PHOTO:
			final String path = file.getPath();
			imageLoader.displayImage("file://" + path, iv_1);

			

			break;
		case DOC:
			iv_1.setImageResource(R.drawable.file_icon_txt);
			break;
		case UNKNOW:
			iv_1.setImageResource(R.drawable.file_icon_unknown);
			break;
		case ZIP:
			iv_1.setImageResource(R.drawable.file_icon_zip);
			break;
		case APK:
			iv_1.setImageResource(R.drawable.file_icon_apk);
			break;
		default:
			break;
		}

	}

	private File[] getMaxSort(File[] files) {
		ArrayList<File> list = new ArrayList<File>();
		for (int i = 0; i < files.length; i++) {
			if (!files[i].isDirectory()) {
				list.add(files[i]);
			}
		}
		File[] files1 = list.toArray(new File[list.size()]);
		for (int i = 0; i<4; ++i) {
			for (int j = i+1; j < files1.length; ++j) {
				if (files1[i].lastModified() < files1[j].lastModified()) {
					File tmp = files1[j];
					files1[j] = files1[i];
					files1[i] = tmp;
				}
			}
		}
		return files1;
	}
	
	public void updateList(List<File> files){
		this.files = files;
		checkbox_list = new Boolean[files.size()];
		for (int i = 0; i < checkbox_list.length; i++) {
			checkbox_list[i] = false;
		}
		notifyDataSetChanged();
	}

	public void setAllSelect() {
		if(!isAllSelect){
			for (int i = 0; i < checkbox_list.length; i++) {
				checkbox_list[i] = true;
			}
			isAllSelect = true;
		}else{
			for (int i = 0; i < checkbox_list.length; i++) {
				checkbox_list[i] = false;
			}
			isAllSelect = false;
		}
		
	}
	public void setNoneSelect() {
		for (int i = 0; i < checkbox_list.length; i++) {
			checkbox_list[i] = false;
		}
		
	}
}
