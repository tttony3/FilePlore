package com.changhong.fileplore.activities;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.example.fileplore.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PloreActivity extends Activity {
	ArrayList<File> waitCopy = new ArrayList<File>();
	ListView mListView;
	TextView mPathView;
	ImageView iv_back;
	TextView mItemCount;
	Button btn_1;
	Button btn_2;
	Button btn_3;
	Button btn_4;
	FileListAdapter mFileAdpter;
	myItemListener mylistener = new myItemListener();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plore);
		findView();
		initView();
	}

	private void findView() {
		mListView = (ListView) findViewById(R.id.file_list);
		mPathView = (TextView) findViewById(R.id.path);
		mItemCount = (TextView) findViewById(R.id.item_count);
		iv_back = (ImageView) findViewById(R.id.iv_back);
		btn_1 = (Button) findViewById(R.id.plore_btn_1);
		btn_2 = (Button) findViewById(R.id.plore_btn_2);
		btn_3 = (Button) findViewById(R.id.plore_btn_3);
		btn_4 = (Button) findViewById(R.id.plore_btn_4);
	}

	private void initView() {
		OnClickListener btn_listener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.plore_btn_1) {
					waitCopy.clear();
					final EditText ed = new EditText(PloreActivity.this);
					AlertDialog.Builder builder = new AlertDialog.Builder(PloreActivity.this);
					builder.setTitle("输入文件夹名称");
					builder.setView(ed).setNegativeButton("确定", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							File file = new File(mPathView.getText().toString() + "/" + ed.getText());
							if (file.exists())
								Toast.makeText(PloreActivity.this, "文件夹已存在", Toast.LENGTH_SHORT).show();
							else {
								if (file.mkdir())
									Toast.makeText(PloreActivity.this, "文件夹创建成功", Toast.LENGTH_SHORT).show();
								File folder = new File(mPathView.getText().toString());
								initData(folder);
							}
						}
					}).setPositiveButton("取消", null).create().show();

				} else if (v.getId() == R.id.plore_btn_3) {
					if (waitCopy.isEmpty())
						Toast.makeText(PloreActivity.this, "没有选择文件", Toast.LENGTH_SHORT).show();
					else {
						String path = mPathView.getText().toString();
						for (int i = 0; i < waitCopy.size(); i++) {
							File file = waitCopy.get(i);
							file.renameTo(new File(path + "/" + file.getName()));
						}
						waitCopy.clear();
						File folder = new File(mPathView.getText().toString());
						initData(folder);
					}
				} else if (!mFileAdpter.isShow_cb()) {
					mFileAdpter.setShow_cb(true);
					mFileAdpter.notifyDataSetChanged();
				} else {
					Boolean[] mlist = mFileAdpter.getCb_list();
					switch (v.getId()) {

					case R.id.plore_btn_2:
						waitCopy.clear();
						if (mFileAdpter.isShow_cb()) {
							for (int i = 0; i < mlist.length; i++) {
								if (mlist[i]) {
									File file = (File) mFileAdpter.getItem(i);
									waitCopy.add(file);
								}
							}
						}
						mFileAdpter.setShow_cb(false);
						mFileAdpter.notifyDataSetChanged();
						Toast.makeText(PloreActivity.this, "剪切成功", Toast.LENGTH_SHORT).show();
						break;

					case R.id.plore_btn_4:
						for (int i = 0; i < mlist.length; i++) {
							if (mlist[i]) {
								File file = (File) mFileAdpter.getItem(i);
								if (file.delete()) {
									Toast.makeText(PloreActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
								} else {
									Toast.makeText(PloreActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
								}
							}
						}
						File folder = new File(mPathView.getText().toString());
						initData(folder);
						break;
					default:
						break;
					}

				}

			}
		};
		mListView.setOnItemClickListener(mylistener);
		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

				if (!mFileAdpter.isShow_cb()) {
					mFileAdpter.setShow_cb(true);
					mFileAdpter.notifyDataSetChanged();
				} else {
				}
				return true;
			}
		});
		btn_1.setOnClickListener(btn_listener);
		btn_2.setOnClickListener(btn_listener);
		btn_3.setOnClickListener(btn_listener);
		btn_4.setOnClickListener(btn_listener);
		File folder = new File("/storage");
		initData(folder);
	}

	void initData(File folder) {
		mylistener.smb = 0;
		boolean isRoot = (folder.getParent() == null);
		if (folder.canRead()) {
			String path = folder.getPath();
			String[] names = folder.list();
			mPathView.setText(folder.getPath());
			OnClickListener myOnClickListener = new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mFileAdpter.isShow_cb()) {
						mFileAdpter.setShow_cb(false);
						mFileAdpter.notifyDataSetChanged();
					} else {
						String str = (String) mPathView.getText();
						if (str.lastIndexOf("/") == 1) {
							initData(new File("/storage"));
						} else {
							initData(new File((String) str.subSequence(0, str.lastIndexOf("/"))));
						}

					}
				}
			};
			iv_back.setOnClickListener(myOnClickListener);
			mPathView.setOnClickListener(myOnClickListener);
			mItemCount.setText(names.length + "项");

			ArrayList<File> files = new ArrayList<File>();
			for (int i = 0; i < names.length; i++) {
				files.add(new File("/" + path + "/" + names[i]));
			}
			int n = 0;
			for (int i = 0, j = 0; j < files.size(); j++, i++) {
				if (!files.get(i).isDirectory()) {
					files.add(files.remove(i));
					i--;
					continue;
				}
				n++;
			}

			for (int i = 0; i < n - 1; i++)
				for (int j = i + 1; j < n; j++) {
					if (files.get(i).getName().charAt(0) > files.get(j).getName().charAt(0)) {
						File tmp = files.remove(i);
						files.add(i, files.remove(j - 1));
						files.add(j, tmp);
					}
				}
			for (int i = n; i < files.size() - 1; i++)
				for (int j = i + 1; j < files.size(); j++) {
					if (files.get(i).getName().charAt(0) > files.get(j).getName().charAt(0)) {
						File tmp = files.remove(i);
						files.add(i, files.remove(j - 1));
						files.add(j, tmp);
					}
				}
			mFileAdpter = new FileListAdapter(this, files, isRoot);

			mListView.setAdapter(mFileAdpter);
		}

	}

	public class FileListAdapter extends BaseAdapter {

		private ArrayList<File> files;
		private Boolean[] cb_list;

		public Boolean[] getCb_list() {
			return cb_list;
		}

		public void setCb_list(Boolean[] cb_list) {
			this.cb_list = cb_list;
		}

		private LayoutInflater mInflater;
		private boolean show_cb = false;

		public FileListAdapter(Context context, ArrayList<File> files, boolean isRoot) {
			this.files = files;
			cb_list = new Boolean[files.size()];
			for (int i = 0; i < cb_list.length; i++) {
				cb_list[i] = false;
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
				viewHolder.cb.setChecked(cb_list[position]);
				viewHolder.cb.setVisibility(View.VISIBLE);

			} else {
				viewHolder.cb.setChecked(cb_list[position]);
				viewHolder.cb.setVisibility(View.GONE);

			}
			viewHolder.cb.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (((CheckBox) v).isChecked()) {
						cb_list[position] = true;
					} else
						cb_list[position] = false;
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

	class myItemListener implements OnItemClickListener {
		public int smb = 0;

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			File file = (File) parent.getItemAtPosition(position);
			if (!file.canRead()) {
				Toast.makeText(view.getContext(), "打不开", Toast.LENGTH_SHORT).show();
			} else if (file.isDirectory()) {
				initData(file);
			} else {
				openFile(file);

			}

		}

		public void openFile(File file) {
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(android.content.Intent.ACTION_VIEW);

			String type = getMIMEType(file);
			intent.setDataAndType(Uri.fromFile(file), type);
			startActivity(intent);
		}

		// mimetype
		public String getMIMEType(File file) {
			String type = "";
			String name = file.getName();

			String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
			if (end.equals("m4a") || end.equals("mp3") || end.equals("wav")) {
				type = "audio";
			} else if (end.equals("mp4") || end.equals("3gp")) {
				type = "video";
			} else if (end.equals("jpg") || end.equals("png") || end.equals("jpeg") || end.equals("bmp")
					|| end.equals("gif")) {
				type = "image";
			} else {
				type = "*";
			}
			type += "/*";
			return type;
		}
	}

}
