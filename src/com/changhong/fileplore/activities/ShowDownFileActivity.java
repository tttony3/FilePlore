package com.changhong.fileplore.activities;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.changhong.fileplore.R;
import com.changhong.fileplore.adapter.DownFileListAdapter;
import com.changhong.fileplore.application.MyApp;
import com.changhong.fileplore.base.BaseActivity;
import com.changhong.fileplore.data.DownData;
import com.changhong.fileplore.service.DownLoadService;
import com.changhong.fileplore.service.DownLoadService.DownLoadBinder;
import com.changhong.fileplore.utils.Utils;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class ShowDownFileActivity extends BaseActivity implements OnClickListener {
	static final private int UPDATE_LIST = 1;
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
		MyApp myapp = (MyApp) getApplication();
		myapp.setContext(this);
		findView();
		try {
			alreadydownList = Utils.getDownDataObject("alreadydownlist");
		} catch (Exception e) {
			Log.e("ee", e.toString());
			alreadydownList = new ArrayList<DownData>();
		}
		Log.e("alreadydownList", alreadydownList.toString());
		downList = new ArrayList<DownData>();
		mAdapter = new DownFileListAdapter(downList,alreadydownList, this, downLoadService);
		lv_downlist.setAdapter(mAdapter);
		mHandler = new MyDownHandler(this);
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
		//downLoadService.getAllDownStatus()
		unbindService(conn);
//		alreadydownList.addAll(downList);
//		Utils.saveObject("alreadydownlist", alreadydownList);
		super.onStop();
	}

	@Override
	protected void findView() {
		tv_num = findView(R.id.item_count);
		lv_downlist = findView(R.id.lv_downlist);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

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
			boolean isContinue = true;
			int i=0;
			int j =0;
			do {
				downList.clear();
				i=0;
				j=0;
				try {
					HashMap<String, DownData> map = downLoadService.getAllDownStatus();
					if (map != null) {						
						Iterator<String> tmp = map.keySet().iterator();
						while (tmp.hasNext()) {
							j++;
							String uri = tmp.next();
							DownData data = map.get(uri);
							if(data.isDone()){
								i++;
							}
							isContinue = isContinue&&data.isDone();
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}while(i<j);
		

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
					mAdapter.update(downList,downLoadService);
					tv_num.setText(alreadydownList.size()+downList.size() + "项");
					break;
				}
			}
		}
	}
}
