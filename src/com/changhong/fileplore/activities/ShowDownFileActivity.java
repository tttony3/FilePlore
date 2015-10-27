package com.changhong.fileplore.activities;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.changhong.alljoyn.simpleservice.FC_GetShareFile;
import com.changhong.fileplore.R;
import com.changhong.fileplore.adapter.DownFileListAdapter;
import com.changhong.fileplore.application.MyApp;
import com.changhong.fileplore.base.BaseActivity;
import com.changhong.fileplore.data.DownData;
import com.changhong.fileplore.service.DownLoadService;
import com.changhong.fileplore.service.DownLoadService.DownLoadBinder;
import com.changhong.fileplore.utils.Utils;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class ShowDownFileActivity extends BaseActivity implements OnItemLongClickListener, OnItemClickListener {
	static final private int UPDATE_LIST = 1;
	static final private int UPDATE_LIST_DELETE = 2;
	private TextView tv_num;
	static private ListView lv_downlist;
	private DownLoadService downLoadService;
	private ArrayList<DownData> downList;
	private ArrayList<DownData> alreadydownList;
	private DownFileListAdapter mAdapter;
	private MyDownHandler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_downlist);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);

		MyApp myapp = (MyApp) getApplication();
		myapp.setContext(this);
		findView();
		try {
			alreadydownList = Utils.getDownDataObject("alreadydownlist");
		} catch (Exception e) {
			alreadydownList = new ArrayList<DownData>();
		}
		downList = new ArrayList<DownData>();
		mAdapter = new DownFileListAdapter(downList, alreadydownList, this, downLoadService);
		lv_downlist.setAdapter(mAdapter);
		mHandler = new MyDownHandler(this);

		lv_downlist.setOnItemClickListener(this);
		lv_downlist.setOnItemLongClickListener(this);
	}

	@Override
	protected void onStart() {
		this.bindService(new Intent("com.changhong.fileplore.service.DownLoadService"), conn, BIND_AUTO_CREATE);
		try {
			alreadydownList = Utils.getDownDataObject("alreadydownlist");
		} catch (Exception e) {
			alreadydownList = new ArrayList<DownData>();
		}
		super.onStart();
	}

	@Override
	protected void onStop() {
		unbindService(conn);
		super.onStop();
	}

	@Override
	protected void findView() {
		tv_num = findView(R.id.item_count);
		lv_downlist = findView(R.id.lv_downlist);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		final DownData tmp = (DownData) parent.getItemAtPosition(position);
		String[] data = { "打开", "删除" };
		new AlertDialog.Builder(ShowDownFileActivity.this).setTitle("选择操作")
				.setItems(data, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:

							String download_Path = Environment.getExternalStorageDirectory().getAbsolutePath();
							String appname = FC_GetShareFile.getApplicationName(ShowDownFileActivity.this);
							startActivity(Utils
									.openFile(new File(download_Path + "/" + appname + "/download/" + tmp.getName())));
							break;
						case 1:
							if (downLoadService.cancelDownload(tmp.getUri())) {
								break;
							} else {

								try {
									alreadydownList = Utils.getDownDataObject("alreadydownlist");
								} catch (Exception e) {

									alreadydownList = new ArrayList<DownData>();
								}
								Iterator<DownData> it = alreadydownList.iterator();
								while (it.hasNext()) {
									DownData tmp1 = it.next();
									if (tmp1.getUri().equals(tmp.getUri())) {
										alreadydownList.remove(tmp1);
									}
								}
								Utils.saveObject("alreadydownlist", alreadydownList);
							}
							try {
								alreadydownList = Utils.getDownDataObject("alreadydownlist");
							} catch (Exception e) {

								alreadydownList = new ArrayList<DownData>();
							}
							HashMap<String, DownData> map = downLoadService.getAllDownStatus();
							if (map != null) {
								Iterator<String> tmp = map.keySet().iterator();
								while (tmp.hasNext()) {
									String uri = tmp.next();
									DownData data = map.get(uri);
									if (data.isDone()) {
									}
									downList.add(data);
								}

							}

							Message msg = new Message();
							Bundle bundle = new Bundle();
							bundle.putInt("key", UPDATE_LIST);
							msg.setData(bundle);
							mHandler.sendMessage(msg);

							break;
						}
					}
				}).create().show();
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.e("click", "cilck");
		final DownData tmp = (DownData) parent.getItemAtPosition(position);
		String[] data = { "打开", "删除" };
		new AlertDialog.Builder(ShowDownFileActivity.this).setTitle("选择操作")
				.setItems(data, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							String download_Path = Environment.getExternalStorageDirectory().getAbsolutePath();
							String appname = FC_GetShareFile.getApplicationName(ShowDownFileActivity.this);
							startActivity(Utils
									.openFile(new File(download_Path + "/" + appname + "/download/" + tmp.getName())));
							break;
						case 1:
							if (downLoadService.cancelDownload(tmp.getUri())) {
								break;
							} else {

								try {
									alreadydownList = Utils.getDownDataObject("alreadydownlist");
								} catch (Exception e) {

									alreadydownList = new ArrayList<DownData>();
								}
								for (int i = 0; i < alreadydownList.size(); i++) {
									if (alreadydownList.get(i).getUri().equals(tmp.getUri())) {
										alreadydownList.remove(i);
									}
								}

								Utils.saveObject("alreadydownlist", alreadydownList);
							}
							try {
								alreadydownList = Utils.getDownDataObject("alreadydownlist");
							} catch (Exception e) {

								alreadydownList = new ArrayList<DownData>();
							}
							HashMap<String, DownData> map = downLoadService.getAllDownStatus();
							if (map != null) {
								Iterator<String> tmp = map.keySet().iterator();
								while (tmp.hasNext()) {
									String uri = tmp.next();
									DownData data = map.get(uri);
									if (data.isDone()) {
									}
									downList.add(data);
								}

							}

							Message msg = new Message();
							Bundle bundle = new Bundle();
							bundle.putInt("key", UPDATE_LIST_DELETE);
							msg.setData(bundle);
							mHandler.sendMessage(msg);

							break;
						}
					}
				}).create().show();
	}

	private ServiceConnection conn = new ServiceConnection() {
		/** 获取服务对象时的操作 */
		public void onServiceConnected(ComponentName name, IBinder service) {
			downLoadService = ((DownLoadBinder) service).getService();
			new Thread(new DownloadProcessRunnable()).start();

		}

		/** 无法获取到服务对象时的操作 */
		public void onServiceDisconnected(ComponentName name) {
			downList.clear();
			downLoadService = null;
		}

	};

	class DownloadProcessRunnable implements Runnable {
		public void run() {
			int i = 0;
			int j = 0;
			do {
				downList.clear();
				i = 0;
				j = 0;
				try {
					HashMap<String, DownData> map = downLoadService.getAllDownStatus();
					if (map != null) {
						Iterator<String> tmp = map.keySet().iterator();
						while (tmp.hasNext()) {
							j++;
							String uri = tmp.next();
							DownData data = map.get(uri);
							if (data.isDone()) {
								i++;
							}
							downList.add(data);
						}

					}
					Message msg = new Message();
					Bundle bundle = new Bundle();
					bundle.putInt("key", UPDATE_LIST);
					msg.setData(bundle);
					mHandler.sendMessage(msg);
					Thread.sleep(500);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}

			} while (i < j);

		}
	}

	class MyDownHandler extends Handler {
		private WeakReference<ShowDownFileActivity> mActivity;

		public MyDownHandler(ShowDownFileActivity activity) {
			mActivity = new WeakReference<ShowDownFileActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			ShowDownFileActivity theActivity = mActivity.get();
			if (theActivity != null) {
				super.handleMessage(msg);
				int key = msg.getData().getInt("key");
				switch (key) {
				case UPDATE_LIST:
					mAdapter.update(downList, downLoadService);
					tv_num.setText(alreadydownList.size() + downList.size() + "项");
					break;
				case UPDATE_LIST_DELETE:
					mAdapter.updatedelete(downList, alreadydownList, downLoadService);
					tv_num.setText(alreadydownList.size() + downList.size() + "项");
					break;
				}

			}
		}
	}
}
