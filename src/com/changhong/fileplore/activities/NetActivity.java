package com.changhong.fileplore.activities;

import java.util.List;

import com.changhong.alljoyn.simpleclient.DeviceInfo;
import com.changhong.fileplore.utils.MyProgressDialog;
import com.chobit.corestorage.CoreApp;
import com.chobit.corestorage.CoreDeviceListener;
import com.example.fileplore.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
		dialog = new MyProgressDialog(NetActivity.this).getDialog();

		if (CoreApp.mBinder != null) {
			CoreApp.mBinder.setDeviceListener(deviceListener);
			deviceListener.startWaiting();
		}

	}

	private CoreDeviceListener deviceListener = new CoreDeviceListener() {

		@Override
		public void updateDeviceList(List<DeviceInfo> list) {
			for (int i = 0; i < list.size(); i++) {
				Log.i("devicename", list.get(i).getM_devicename());
			}
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

		}
	};
}
