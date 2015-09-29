package com.changhong.fileplore.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.changhong.alljoyn.simpleservice.FC_GetShareFile;
import com.changhong.fileplore.application.MyApp;
import com.changhong.fileplore.data.DownData;
import com.changhong.fileplore.implement.DownStatusInterface;
import com.chobit.corestorage.CoreApp;
import com.chobit.corestorage.CoreDownloadProgressCB;
import com.example.libevent2.UpdateDownloadPress;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class DownLoadService extends Service implements DownStatusInterface {
	static final public int MAX_THREAD = 2;
	private ExecutorService pool;
	private IBinder mBinder;
	HashMap<String, DownData> downMap;
	boolean setDownCB = true;

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;

	}

	@Override
	public void onCreate() {
		super.onCreate();
		mBinder = new DownLoadBinder();
		downMap = new HashMap<String, DownData>();
		pool = Executors.newFixedThreadPool(MAX_THREAD);
		Log.e("ononCreate", "ononCreate");
		if (setDownCB) {
			setDownCB = false;
			CoreApp.mBinder.setDownloadCBInterface(new ServiceDownloadProgressCB());
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		ArrayList<String> downloadlist = intent.getStringArrayListExtra("downloadlist");
		if (downloadlist != null) {
			Iterator<String> it = downloadlist.iterator();
			while (it.hasNext()) {
				DownData tmp = new DownData();
				String uri = it.next();
				tmp.setUri(uri).setCurPart(0).setTotalPart(0)
						.setName(uri.substring(uri.lastIndexOf("/") + 1, uri.length()));
				downMap.put(uri, tmp);
			}
			Iterator<String> it1 = downMap.keySet().iterator();
			while (it1.hasNext()) {
				pool.execute(new DownRunnAble(it1.next()));
			}
		} else if (downloadlist == null) {
			Log.e("show", "show");
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
	}

	public class DownLoadBinder extends Binder {
		/**
		 * 获取 Service 实例
		 * 
		 * @return
		 */
		public DownLoadService getService() {
			return DownLoadService.this;
		}

	}

	class DownRunnAble implements Runnable {
		String uri;

		public DownRunnAble(String uri) {
			this.uri = uri;
		}

		public void run() {
			CoreApp.mBinder.DownloadHttpFile("client", uri, null);
		}
	}

	class ServiceDownloadProgressCB implements CoreDownloadProgressCB {

		@Override
		public void onDowloadProgress(UpdateDownloadPress press) {
			DownData mDownData = downMap.get(press.uriString);
			mDownData.setCurPart(press.part).setTotalPart(press.total);
			mDownData.setCancel(false);
		}

		@Override
		public void onDowloaStop(String fileuri) {

		}

		@Override
		public void onDowloaCancel(String fileuri) {
			DownData mDownData = downMap.get(fileuri);
			mDownData.setCancel(true);

		}

		@Override
		public void onDowloaFailt(String fileuri) {
			DownData mDownData = downMap.get(fileuri);
			mDownData.setCancel(true);

		}

		@Override
		public void onDownloadOK(String fileuri) {
			downMap.get(fileuri).setDone(true);
			String download_Path = Environment.getExternalStorageDirectory().getAbsolutePath();
			String appname = FC_GetShareFile.getApplicationName(getApplicationContext());
			Toast.makeText(((MyApp) getApplication()).getContext(),
					"下载成功,保存在" + download_Path + "/" + appname + "/download/ 目录下", Toast.LENGTH_SHORT).show();

		}

		@Override
		public void onConnectError(String fileuri) {
			

		}

	}

	@Override
	public HashMap<String, DownData> getAllDownStatus() {
		return downMap;
	}

	@Override
	public DownData getDownStatus(String uri) {
		return downMap.get(uri);

	}

	@Override
	public void stopDownload(String uri) {
		CoreApp.mBinder.cancelDownload(uri);

	}

	@Override
	public void stopAllDownload() {
		Iterator<String> it = downMap.keySet().iterator();
		while (it.hasNext()) {
			CoreApp.mBinder.cancelDownload(it.next());
		}
	}

	public void addDownloadFile(String uri) {
		DownData tmp = new DownData();
		tmp.setUri(uri).setCurPart(0).setTotalPart(0).setName(uri.substring(uri.lastIndexOf("/") + 1, uri.length()));
		downMap.put(uri, tmp);
		pool.execute(new DownRunnAble(uri));
	}
	

}
