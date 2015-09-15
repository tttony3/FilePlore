package com.changhong.fileplore.activities;

import java.util.ArrayList;
import java.util.List;

import com.changhong.alljoyn.simpleclient.ClientBusHandler;
import com.changhong.alljoyn.simpleclient.DeviceInfo;
import com.changhong.fileplore.adapter.NetDevListAdapter;
import com.changhong.fileplore.utils.MyProgressDialog;
import com.changhong.synergystorage.javadata.JavaFolder;
import com.chobit.corestorage.CoreApp;
import com.chobit.corestorage.CoreDeviceListener;
import com.chobit.corestorage.CoreHttpServerCB;
import com.chobit.corestorage.CoreShareFileListener;
import com.changhong.fileplore.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ShowNetDevActivity extends Activity {
	Context context = ShowNetDevActivity.this;
	ProgressDialog dialog;
	ListView netList;
	ArrayList<String> shareList;
	NetDevListAdapter netListAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_net_dev);
		netList = (ListView) findViewById(R.id.lv_netactivity);
		dialog = new MyProgressDialog(ShowNetDevActivity.this).getDialog();

		// ClientBusHandler.List_DeviceInfo.clear();

		if (CoreApp.mBinder != null) {
			CoreApp.mBinder.setDeviceListener(deviceListener);
			deviceListener.startWaiting();
			Log.e("GetDeviceList",
					CoreApp.mBinder.GetDeviceList().toString() + "list info" + ClientBusHandler.List_DeviceInfo.size());
			setUpdateList(CoreApp.mBinder.GetDeviceList());

		}

		netList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				Intent intent = new Intent();
				intent.setClass(ShowNetDevActivity.this, ShowNetFileActivity.class);
				intent.setFlags(position);
				startActivity(intent);
			}
		});
		//

	}

	private void setUpdateList(List<DeviceInfo> list) {
		Log.e("ss", list.toString());
		if (list.size() > 0) {
			deviceListener.stopWaiting();
		}
		netListAdapter = new NetDevListAdapter(list, context);
		netList.setAdapter(netListAdapter);
	}

	private CoreDeviceListener deviceListener = new CoreDeviceListener() {

		@Override
		public void updateDeviceList(List<DeviceInfo> list) {
			Log.e("List_DeviceInfo", ClientBusHandler.List_DeviceInfo.size() + "");
			Log.e("DeviceInfo", list.size() + "");
			setUpdateList(list);

		}

		@Override
		public void stopWaiting() {
			if (dialog.isShowing())
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			// CoreApp app = (CoreApp) this.getApplicationContext();

			// app.onTerminate();
			// CoreApp.mBinder.deinit();
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
