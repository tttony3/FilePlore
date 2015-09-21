package com.changhong.fileplore.activities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.changhong.fileplore.adapter.PloreListAdapter;
import com.changhong.fileplore.application.MyApp;
import com.changhong.fileplore.view.RefreshListView;
import com.chobit.corestorage.CoreApp;
import com.changhong.fileplore.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.changhong.fileplore.utils.*;

public class PloreActivity extends Activity implements RefreshListView.IOnRefreshListener {
	ArrayList<File> fileList = new ArrayList<File>();
	ArrayList<String> shareList = new ArrayList<String>();
	private RefreshDataAsynTask mRefreshAsynTask;
	RefreshListView mListView;
	TextView mPathView;
	ImageView iv_back;
	TextView mItemCount;
	Button btn_4;
	Button btn_1;
	Button btn_2;
	Button btn_3;
	Button btn_more;
	public PloreListAdapter mFileAdpter;
	public LinearLayout ll_btn;
	public AlertDialog.Builder builder;
	boolean isCopy = false;
	myItemListener mylistener = new myItemListener();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plore);
		MyApp myapp = (MyApp) getApplication();
		fileList = myapp.getFileList();
		findView();

		initView();

	}

	private void findView() {
		mListView = (RefreshListView) findViewById(R.id.file_list);
		mPathView = (TextView) findViewById(R.id.path);
		mItemCount = (TextView) findViewById(R.id.item_count);
		iv_back = (ImageView) findViewById(R.id.iv_back);

		btn_1 = (Button) findViewById(R.id.plore_btn_1);
		btn_2 = (Button) findViewById(R.id.plore_btn_2);
		btn_3 = (Button) findViewById(R.id.plore_btn_3);
		btn_more = (Button) findViewById(R.id.plore_btn_more);
		ll_btn = (LinearLayout) findViewById(R.id.ll_btn);
	}

	private void initView() {

		mListView.setOnItemClickListener(mylistener);
		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (!mFileAdpter.isShow_cb()) {
					mFileAdpter.setShow_cb(true);
					ll_btn.setVisibility(View.VISIBLE);
					mFileAdpter.notifyDataSetChanged();
				} else {
				}
				return true;
			}
		});
		mListView.setOnRefreshListener(this);
		btn_1.setOnClickListener(btn_listener);
		btn_2.setOnClickListener(btn_listener);
		btn_3.setOnClickListener(btn_listener);
		btn_more.setOnClickListener(btn_listener);
		File folder = new File("/storage");
		initData(folder);
	}

	void initData(File folder) {
		ll_btn.setVisibility(View.GONE);
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
						ll_btn.setVisibility(View.GONE);
						mFileAdpter.notifyDataSetChanged();
					} else {
						String str = (String) mPathView.getText();
						if (str.lastIndexOf("/") == 0) {

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
			mFileAdpter = new PloreListAdapter(this, files, isRoot);

			mListView.setAdapter(mFileAdpter);
		}

	}

	// listview监听
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

	/**
	 * 底部button监听
	 */
	private OnClickListener btn_listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			switch (v.getId()) {

			case R.id.plore_btn_1:
				fileList.clear();
				final EditText ed = new EditText(PloreActivity.this);
				builder = new AlertDialog.Builder(PloreActivity.this);
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
							else {

							}
							File folder = new File(mPathView.getText().toString());
							initData(folder);
						}
					}
				}).setPositiveButton("取消", null).create().show();

				break;

			case R.id.plore_btn_2:
				if (!mFileAdpter.isShow_cb()) {
					mFileAdpter.setShow_cb(true);
					mFileAdpter.notifyDataSetChanged();
				} else {
					Boolean[] mlist = mFileAdpter.getCheckBox_List();
					fileList.clear();
					if (mFileAdpter.isShow_cb()) {
						for (int i = 0; i < mlist.length; i++) {
							if (mlist[i]) {
								File file = (File) mFileAdpter.getItem(i);
								fileList.add(file);
							}
						}
					}
					mFileAdpter.setShow_cb(false);
					mFileAdpter.notifyDataSetChanged();
					isCopy = false;
					Toast.makeText(PloreActivity.this, "剪切成功", Toast.LENGTH_SHORT).show();
				}
				break;
				
			case R.id.plore_btn_3:
				if (fileList.isEmpty())
					Toast.makeText(PloreActivity.this, "没有选择文件", Toast.LENGTH_SHORT).show();
				else {
					String path = mPathView.getText().toString();
					if (!isCopy) {
						for (int i = 0; i < fileList.size(); i++) {
							File file = fileList.get(i);
							file.renameTo(new File(path + "/" + file.getName()));
						}
						fileList.clear();
						File folder = new File(mPathView.getText().toString());
						initData(folder);
					} else {
						for (int i = 0; i < fileList.size(); i++) {
							File file = fileList.get(i);
							File newFile = new File(path + "/" + file.getName());
							if (!newFile.exists()) {
								try {
									newFile.createNewFile();
								} catch (IOException e) {

									e.printStackTrace();
								}

								FileUtils.fileChannelCopy(file, newFile);
							} else {
								Toast.makeText(PloreActivity.this, "文件已存在", Toast.LENGTH_SHORT).show();
							}
						}
						fileList.clear();
						File folder = new File(mPathView.getText().toString());
						initData(folder);
					}
				}
				break;
			case R.id.plore_btn_more:

				AlertDialog.Builder dialog = new AlertDialog.Builder(PloreActivity.this);
				dialog.setTitle("");
				String[] dataArray = new String[] { "复制", "删除", "共享" };
				dialog.setItems(dataArray, new DialogInterface.OnClickListener() {
					/***
					 * 我们这里传递给dialog.setItems方法的参数为数组，这就导致了我们下面的
					 * onclick方法中的which就跟数组下标是一样的，点击hello时返回0；点击baby返回1……
					 */
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// dialog.dismiss();
						switch (which) {
						case 0:
							if (!mFileAdpter.isShow_cb()) {
								mFileAdpter.setShow_cb(true);
								mFileAdpter.notifyDataSetChanged();
							} else {
								Boolean[] mlist = mFileAdpter.getCheckBox_List();
								fileList.clear();
								if (mFileAdpter.isShow_cb()) {
									for (int i = 0; i < mlist.length; i++) {
										if (mlist[i]) {
											File file = (File) mFileAdpter.getItem(i);
											fileList.add(file);
										}
									}
								}
								mFileAdpter.setShow_cb(false);
								mFileAdpter.notifyDataSetChanged();
								isCopy = true;
								Toast.makeText(PloreActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
							}
							break;
						case 1:
							if (!mFileAdpter.isShow_cb()) {
								mFileAdpter.setShow_cb(true);
								mFileAdpter.notifyDataSetChanged();
							} else {
								Boolean[] mlist = mFileAdpter.getCheckBox_List();
								for (int i = 0; i < mlist.length; i++) {
									if (mlist[i]) {
										File file = (File) mFileAdpter.getItem(i);
										Log.e("file", file.getName());
										if (file.delete()) {

											Toast.makeText(PloreActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
										} else {
											Toast.makeText(PloreActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
										}
									}
								}
								File folder = new File(mPathView.getText().toString());
								initData(folder);
							}
							break;

						case 2:

							if (!mFileAdpter.isShow_cb()) {
								mFileAdpter.setShow_cb(true);
								mFileAdpter.notifyDataSetChanged();
							} else {
								Boolean[] mlist = mFileAdpter.getCheckBox_List();
								for (int i = 0; i < mlist.length; i++) {
									if (mlist[i]) {
										File file = (File) mFileAdpter.getItem(i);
										shareList.add(file.getPath());
										// CoreApp.mBinder.AddShareFile(file.getPath());
										String s = CoreApp.mBinder.AddShareFile(file.getPath());
										Toast.makeText(PloreActivity.this, "AddShareFile  " + s, Toast.LENGTH_SHORT)
												.show();
									}
								}
								File folder = new File(mPathView.getText().toString());
								initData(folder);
								// Toast.makeText(PloreActivity.this, "已共享",
								// Toast.LENGTH_SHORT).show();
								// Intent intent = new Intent();
								// intent.setClass(PloreActivity.this,NetActivity.class);
								// intent.putStringArrayListExtra("shareList",
								// shareList);
								// startActivity(intent);

							}

							break;
						default:
							break;
						}

					}
				}).show();

				break;
			default:
				break;
			}

		}
	};

	class RefreshDataAsynTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {

			try {
				Thread.sleep(700);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			initData(new File(mPathView.getText().toString()));
			mListView.onRefreshComplete();
		}

	}

	@Override
	public void OnRefresh() {
		mRefreshAsynTask = new RefreshDataAsynTask();
		mRefreshAsynTask.execute();

	}

}
