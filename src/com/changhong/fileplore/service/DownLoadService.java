package com.changhong.fileplore.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.changhong.alljoyn.simpleservice.FC_GetShareFile;
import com.changhong.fileplore.R;
import com.changhong.fileplore.activities.ShowDownFileActivity;
import com.changhong.fileplore.application.MyApp;
import com.changhong.fileplore.data.DownData;
import com.changhong.fileplore.implement.DownStatusInterface;
import com.changhong.fileplore.utils.Utils;
import com.chobit.corestorage.CoreApp;
import com.chobit.corestorage.CoreDownloadProgressCB;
import com.example.libevent2.UpdateDownloadPress;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
	public void onDestroy() {
		ArrayList<DownData> alreadydownList;
		try {
			alreadydownList = Utils.getDownDataObject("alreadydownlist");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			alreadydownList = new ArrayList<DownData>();
		}
		ArrayList<String> del = new ArrayList<String>();
		Iterator<String> it = downMap.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			DownData tmp = downMap.get(key);
			if (tmp.isDone()) {
				alreadydownList.add(tmp);
				del.add(key);
			}
		}
		for (int i = 0; i < del.size(); i++) {
			downMap.remove(del.get(i));
		}
		Utils.saveObject("alreadydownlist", alreadydownList);
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
			showNotification();
			synchronized (this) {

				ArrayList<DownData> alreadydownList;
				try {
					alreadydownList = Utils.getDownDataObject("alreadydownlist");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					alreadydownList = new ArrayList<DownData>();
				}
				ArrayList<String> del = new ArrayList<String>();
				Iterator<String> it = downMap.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next();
					DownData tmp = downMap.get(key);
					if (tmp.isDone()) {
						alreadydownList.add(tmp);
						del.add(key);
					}
				}
				for (int i = 0; i < del.size(); i++) {
					downMap.remove(del.get(i));
				}
				Utils.saveObject("alreadydownlist", alreadydownList);
			}

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

	@SuppressWarnings("deprecation")
	private void showNotification() {
		// 创建一个NotificationManager的引用
		NotificationManager notificationManager = (NotificationManager) this
				.getSystemService(android.content.Context.NOTIFICATION_SERVICE);

		// 定义Notification的各种属性
		// Notification notification =new
		// Notification(R.drawable.file_icon_download,
		// "下载成功", System.currentTimeMillis());
		// FLAG_AUTO_CANCEL 该通知能被状态栏的清除按钮给清除掉
		// FLAG_NO_CLEAR 该通知不能被状态栏的清除按钮给清除掉
		// FLAG_ONGOING_EVENT 通知放置在正在运行
		// FLAG_INSISTENT 是否一直进行，比如音乐一直播放，知道用户响应
		// notification.flags |= Notification.FLAG_ONGOING_EVENT; //
		// 将此通知放到通知栏的"Ongoing"即"正在运行"组中
		// notification.flags |= Notification.FLAG_AUTO_CANCEL; //
		// 表明在点击了通知栏中的"清除通知"后，此通知不清除，经常与FLAG_ONGOING_EVENT一起使用
		// notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		// DEFAULT_ALL 使用所有默认值，比如声音，震动，闪屏等等
		// DEFAULT_LIGHTS 使用默认闪光提示
		// DEFAULT_SOUNDS 使用默认提示声音
		// DEFAULT_VIBRATE 使用默认手机震动，需加上<uses-permission
		// android:name="android.permission.VIBRATE" />权限
		// notification.defaults = Notification.DEFAULT_LIGHTS;
		// 叠加效果常量
		// notification.defaults=Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND;

		// notification.ledOnMS =5000; //闪光时间，毫秒

		// 设置通知的事件消息
		CharSequence contentTitle = "FilePlore"; // 通知栏标题
		CharSequence contentText = "下载成功，点击查看"; // 通知栏内容
		Intent notificationIntent = new Intent(this, ShowDownFileActivity.class); // 点击该通知后要跳转的Activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentItent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		// notification.setLatestEventInfo(this, contentTitle, contentText,
		// contentItent);
		// notification.largeIcon=
		// BitmapFactory.decodeResource(DownLoadService.this.getResources(),
		// R.drawable.file_icon_download);
		Notification notification = new Notification.Builder(this)
				.setLargeIcon(BitmapFactory.decodeResource(DownLoadService.this.getResources(),
						R.drawable.file_icon_download))
				.setContentText(contentText).setContentTitle(contentTitle).setContentIntent(contentItent)
				.setSmallIcon(R.drawable.file_icon_download).setTicker("下载成功").setWhen(System.currentTimeMillis())
				.setDefaults(Notification.DEFAULT_LIGHTS).build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL; // 表明在点击了通知栏中的"清除通知"后，此通知不清除，经常与FLAG_ONGOING_EVENT一起使用
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		// 把Notification传递给NotificationManager
		notificationManager.notify(0, notification);
	}
}
