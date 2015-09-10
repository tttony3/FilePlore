package com.changhong.fileplore.activities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.changhong.alljoyn.simpleclient.ClientBusHandler;
import com.changhong.alljoyn.simpleclient.DeviceInfo;
import com.changhong.fileplore.adapter.ListNetAdapter;
import com.changhong.fileplore.application.MyApp;
import com.changhong.fileplore.utils.MyProgressDialog;
import com.changhong.synergystorage.javadata.JavaFolder;
import com.chobit.corestorage.ConnectedService;
import com.chobit.corestorage.CoreApp;
import com.chobit.corestorage.CoreDeviceListener;
import com.chobit.corestorage.CoreHttpServerCB;
import com.chobit.corestorage.CoreService.CoreServiceBinder;
import com.chobit.corestorage.CoreShareFileListener;
import com.example.fileplore.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class NetActivity extends Activity {
	Context context = NetActivity.this;
	ProgressDialog dialog;
	ListView mList;
	ArrayList<String> shareList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_net);
		mList = (ListView) findViewById(R.id.lv_netactivity);
		Intent intent = getIntent();
		shareList = intent.getStringArrayListExtra("shareList");
		dialog = new MyProgressDialog(NetActivity.this).getDialog();
		Log.e("num", ClientBusHandler.List_DeviceInfo.size() + "");
		CoreApp.mBinder.setDeviceListener(deviceListener);
		CoreApp.mBinder.setShareFileListener(shareListener);
		deviceListener.startWaiting();
//		
//		CoreApp app = (CoreApp) this.getApplicationContext();
//		app.setConnectedService(new ConnectedService() {
//
//			@Override
//			public void onConnected(Binder b) {
//				CoreServiceBinder binder = (CoreServiceBinder) b;
//
//				binder.setDeviceListener(deviceListener);
//				binder.init();
//				binder.setCoreHttpServerCBFunction(httpServerCB);
//				binder.StartHttpServer("/", context);
////				if (shareList != null) {
////					Iterator<String> iterator = shareList.iterator();
////					while (iterator.hasNext()) {
////						String str = iterator.next();
////						Log.e("share path", str);
////						binder.AddShareFile(str);
////					}
////				}
//
//			}
//		});
		// deviceListener.startWaiting();
	}

	private CoreShareFileListener shareListener = new CoreShareFileListener() {

		@Override
		public void updateShareContent(JavaFolder folder) {
			Log.e("sss", "folder " + folder.toString());
			Log.e("sss", "folder subnum" + folder.getSubFileNum());
			// Iterator<String> i = MyApp.mBinder.GetShareList().iterator();
			// while (i.hasNext()) {
			// Log.e("GetShareList", i.next());
			// }
		}

		@Override
		public void updateImageThumbNails(Bitmap bt) {
			// TODO Auto-generated method stub

		}

		@Override
		public void stopWaiting() {
			dialog.dismiss();

		}

		@Override
		public void startWaiting() {
			dialog.show();

		}
	};

	private CoreDeviceListener deviceListener = new CoreDeviceListener() {

		@Override
		public void updateDeviceList(List<DeviceInfo> list) {
			Log.e("List_DeviceInfo", ClientBusHandler.List_DeviceInfo.size() + "");
			Log.e("DeviceInfo", list.size() + "");
			mList.setAdapter(new ListNetAdapter(list, context));
			mList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					DeviceInfo devInfo = (DeviceInfo) parent.getItemAtPosition(position);
					
					CoreApp.mBinder.ConnectDeivce(devInfo);

					Log.e("devInfo", devInfo.getM_devicename() + "");
					// Log.e("GetShareListSize",
					// CoreApp.mBinder.GetShareList().toString() + "");

				}
			});

		}

		@Override
		public void stopWaiting() {
			dialog.dismiss();
		}

		@Override
		public void startWaiting() {
			dialog.show();
		}

		@Override
		public void showMessage(String arg0) {
			Log.e("showMessage", arg0);
		}
	};
	private CoreHttpServerCB httpServerCB = new CoreHttpServerCB() {

		@Override
		public void onTransportUpdata(String arg0, String arg1, long arg2, long arg3, long arg4) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onHttpServerStop() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onHttpServerStart(String ip, int port) {

			Log.i("tl", ip + "port" + port);

		}

		@Override
		public String onGetRealFullPath(String arg0) {
			return null;
		}

		@Override
		public void recivePushResources(List<String> resourceslist) {
			// TODO Auto-generated method stub

		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			CoreApp app = (CoreApp) this.getApplicationContext();
			
//			 app.onTerminate();
//			 CoreApp.mBinder.deinit();
			// CoreApp.mBinder.DisConnectDeivce();
			//

			// Intent intent = new Intent("com.chobit.corestorage.CoreService");
			//
			// app.stopService(intent);
			//
		}
		return super.onKeyDown(keyCode, event);
	}
}
