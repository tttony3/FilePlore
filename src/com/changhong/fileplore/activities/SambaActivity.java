package com.changhong.fileplore.activities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.example.fileplore.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

public class SambaActivity extends Activity {
	SmbFile[] fileList;
	String ip;
	String password;
	String user;
	String dir;
	ListView mListView;
	TextView mPathView;
	ImageView iv_back;
	TextView mItemCount;
	myItemListener mylistener = new myItemListener();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		ip = intent.getStringExtra("ip");
		password = intent.getStringExtra("password");
		user = intent.getStringExtra("user");
		dir = intent.getStringExtra("dir");
		setContentView(R.layout.activity_plore);
		initView();
	}

	private void initView() {
		mListView = (ListView) findViewById(R.id.file_list);
		mPathView = (TextView) findViewById(R.id.path);
		mItemCount = (TextView) findViewById(R.id.item_count);
		iv_back = (ImageView) findViewById(R.id.iv_back);
		initData(ip, user, password, dir);
		mListView.setOnItemClickListener(mylistener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.plore, menu);
		return true;
	}

	void initData(final SmbFile smbfile) throws MalformedURLException {
		SmbFile[] fileList;
		try {
			if (smbfile.isDirectory()) {
				fileList = smbfile.listFiles();
				String path = smbfile.getPath();
				String[] names = new String[fileList.length];
				for (int i = 0; i < fileList.length; i++) {
					names[i] = fileList[i].getName();

				}
				mPathView.setText(path);

				mPathView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						try {
							if (smbfile.getParent().length() >= 27)
								initData(new SmbFile(smbfile.getParent()));
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}

					}
				});
				mItemCount.setText(names.length + "项");

				ArrayList<SmbFile> files = new ArrayList<SmbFile>();
				for (int i = 0; i < names.length; i++) {
					files.add(fileList[i]);
				}
				mylistener.smb = 1;
				FileListAdapter mFileAdpter = new FileListAdapter(this, files);

				mListView.setAdapter(mFileAdpter);

			}
		} catch (SmbException e) {
			e.printStackTrace();
		}
	}

	void initData(final String ip, final String user, final String password, final String dir) {
		sambaRunable sambarun = new sambaRunable(ip, user, password, dir);
		Thread thread = new Thread(sambarun);
		thread.start();
		while (thread.isAlive())
			try {

				thread.join();
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		Toast.makeText(SambaActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
		SmbFile[] smbFile = sambarun.fileList;

		// String path = dir;
		String[] names = new String[smbFile.length];
		for (int i = 0; i < smbFile.length; i++) {
			names[i] = smbFile[i].getName();

		}
		mPathView.setText(dir);
		mPathView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// String str = (String) mPathView.getText();

			}
		});
		mItemCount.setText(names.length + "项");

		ArrayList<SmbFile> files = new ArrayList<SmbFile>();
		for (int i = 0; i < names.length; i++) {
			try {
				files.add(new SmbFile("smb://" + user + ":" + password + "@" + ip + "/" + dir + "/" + names[i]));
			} catch (MalformedURLException e) {

				e.printStackTrace();
			}
		}
		mylistener.smb = 1;
		FileListAdapter mFileAdpter = new FileListAdapter(this, files);

		mListView.setAdapter(mFileAdpter);

	}

	public SmbFile[] downloadViaShare(final String ip, final String user, final String password, final String dir) {

		String newDir = dir;
		String url = "";

		final FileOutputStream fos = null;
		final SmbFileInputStream smbIs = null;
		if (!dir.endsWith("/")) // directory must end with "/"
			newDir = dir + "/";
		url = "smb://" + user + ":" + password + "@" + ip + "/" + newDir;
		final String url1 = url;
		System.currentTimeMillis();
		new Thread() {

			@Override
			public void run() {
				try {
					Log.e("11", "11");
					SmbFile shareDir = new SmbFile(url1);
					Log.e("11", shareDir.exists() + "");
					shareDir.connect();
					Log.e("11", shareDir.exists() + "");
					fileList = null;
					if (shareDir.isDirectory()) {
						fileList = shareDir.listFiles();
					}
				} catch (MalformedURLException urle) {
					Log.e("11", "Incorrect URL format!");
				} catch (SmbException smbe) {
					smbe.printStackTrace();
					Log.e("11", this.getClass().getName() + "||" + smbe.getMessage());
				} catch (IOException ioe) {
					ioe.printStackTrace();
					Log.e("11", this.getClass().getName() + "||" + ioe.getMessage());
				} finally {
					try {
						smbIs.close();
						fos.close();
					} catch (Exception smbe) {
						Log.e("11", this.getClass().getName() + "||" + smbe.getMessage());
					}
				}

			}

		}.start();
		return fileList;
	}

	class myItemListener implements OnItemClickListener {
		public int smb = 0;

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			SmbFile file = (SmbFile) parent.getItemAtPosition(position);
			try {
				if (!file.canRead()) {
	
					Toast.makeText(view.getContext(), "打不开", Toast.LENGTH_SHORT).show();
				} else if (file.isDirectory()) {
			
					initData(file);
				} else {
					// openFile(file);

				}
			} catch (SmbException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();

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

		// private void openFile(SmbFile file) {
		// Intent intent = new Intent();
		// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// intent.setAction(android.content.Intent.ACTION_VIEW);
		//
		// String type = getMIMEType(file);
		// intent.setDataAndType(Uri.parse(file.getPath()), type);
		// startActivity(intent);
		//
		// }

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

	class sambaRunable implements Runnable {
		String ip;
		String user;
		String password;
		String dir;
		String newDir;
		String url;
		SmbFile[] fileList;

		sambaRunable(String ip, String user, String password, String dir) {
			this.dir = dir;
			this.ip = ip;
			this.password = password;
			this.user = user;
			if (!dir.endsWith("/")) // directory must end with "/"
				newDir = dir + "/";
			url = "smb://" + user + ":" + password + "@" + ip + "/" + newDir;
		}

		public void run() {
			try {

				SmbFile shareDir = new SmbFile(url);
				shareDir.connect();
				fileList = null;
				if (shareDir.isDirectory()) {
					fileList = shareDir.listFiles();
				}
			} catch (MalformedURLException urle) {
				Log.e("11", "Incorrect URL format!");
			} catch (SmbException smbe) {
				smbe.printStackTrace();
				Log.e("11", this.getClass().getName() + "||" + smbe.getMessage());
			} catch (IOException ioe) {
				ioe.printStackTrace();
				Log.e("11", this.getClass().getName() + "||" + ioe.getMessage());
			} finally {
				try {
					// smbIs.close();
					// fos.close();
				} catch (Exception smbe) {
					Log.e("11", this.getClass().getName() + "||" + smbe.getMessage());
				}
			}

		}

	}

	public class FileListAdapter extends BaseAdapter {

		private ArrayList<SmbFile> smbfiles;
		private LayoutInflater mInflater;

		public FileListAdapter(Context context, ArrayList<SmbFile> files) {
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
}
