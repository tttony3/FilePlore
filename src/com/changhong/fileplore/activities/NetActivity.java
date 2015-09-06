package com.changhong.fileplore.activities;

import java.util.List;

import com.changhong.alljoyn.simpleclient.DeviceInfo;
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

public class NetActivity extends Activity {
	Context context = null;
	ProgressDialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_net);
		context = NetActivity.this;
		CoreApp app = (CoreApp) this.getApplicationContext();
		app.setConnectedService(new ConnectedService() {

			@Override
			public void onConnected(Binder b) {
				CoreServiceBinder binder = (CoreServiceBinder) b;
				binder.init();
				binder.setCoreHttpServerCBFunction(httpServerCB);

				binder.setDeviceListener(deviceListener);
				Log.i("G1",  "111");
				
				binder.StartHttpServer("/", context);
				Log.i("GetShareList ++",binder.GetShareList().size()+"");
				Log.i("GetDeviceList ++", binder.GetDeviceList().size() + "");
			}
		});
	
	
	}

	private CoreHttpServerCB httpServerCB = new CoreHttpServerCB() {

		@Override
		public void recivePushResources(String arg0, List<String> arg1) {
			// TODO Auto-generated method stub

		}

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
	};

	private CoreDeviceListener deviceListener = new CoreDeviceListener() {

		@Override
		public void updateDeviceList(List<DeviceInfo> list) {
			for(int i=0;i<list.size();i++){
				Log.i("devicename", list.get(i).getM_devicename());
			}
		}

		@Override
		public void stopWaiting() {

			dialog.dismiss();

		}

		@Override
		public void startWaiting() {

			dialog = new ProgressDialog(NetActivity.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setTitle("查找中");
			dialog.setMessage("请稍等...");
			dialog.setIndeterminate(false);
			dialog.setCancelable(true);
			dialog.show();

		}

		@Override
		public void showMessage(String arg0) {
			

		}
	};
}
