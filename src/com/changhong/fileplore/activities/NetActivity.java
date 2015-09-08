package com.changhong.fileplore.activities;

import java.util.List;

import com.changhong.alljoyn.simpleclient.DeviceInfo;
import com.changhong.fileplore.adapter.ListNetAdapter;
import com.changhong.fileplore.application.MyApp;
import com.changhong.fileplore.utils.MyProgressDialog;
import com.chobit.corestorage.ConnectedService;
import com.chobit.corestorage.CoreApp;
import com.chobit.corestorage.CoreDeviceListener;
import com.chobit.corestorage.CoreHttpServerCB;
import com.chobit.corestorage.CoreService.CoreServiceBinder;
import com.example.fileplore.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class NetActivity extends Activity {
	Context context =  NetActivity.this;
	ProgressDialog dialog;
	ListView mList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_net);
		mList=(ListView) findViewById(R.id.lv_netactivity);
	
		dialog = new MyProgressDialog(NetActivity.this).getDialog();
		CoreApp app = (CoreApp) this.getApplicationContext();
		app.setConnectedService(new ConnectedService() {

			@Override
			public void onConnected(Binder b) {
				CoreServiceBinder binder = (CoreServiceBinder) b;
				binder.setDeviceListener(deviceListener);
				binder.init();
				binder.setCoreHttpServerCBFunction(httpServerCB);	
				binder.StartHttpServer("/", context);
				binder.AddShareFile("/storage/sdcard1");
				
			}
		});			
	

	}

	private CoreDeviceListener deviceListener = new CoreDeviceListener() {

		@Override
		public void updateDeviceList(List<DeviceInfo> list) {
			Log.e("DeviceInfo", list.size()+"");
			mList.setAdapter(new ListNetAdapter(list, context));
			mList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					DeviceInfo devInfo=(DeviceInfo) parent.getItemAtPosition(position);
			//		MyApp.mBinder.ConnectDeivce(devInfo);
					Log.e("root",MyApp.mBinder.GetShareList().get(0));
				//	MyApp.mBinder.getFolderChildren(devInfo, parentfolder, childrenfolder);
					
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
			app.onTerminate();
			
//			Intent intent = new Intent("com.chobit.corestorage.CoreService");
//			
//			app.stopService(intent);
//			
		}
		return super.onKeyDown(keyCode, event);
	}
}
