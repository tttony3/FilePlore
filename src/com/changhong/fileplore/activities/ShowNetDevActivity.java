package com.changhong.fileplore.activities;

import java.util.ArrayList;
import java.util.List;

import com.changhong.alljoyn.simpleclient.ClientBusHandler;
import com.changhong.alljoyn.simpleclient.DeviceInfo;
import com.changhong.fileplore.adapter.NetDevListAdapter;
import com.changhong.fileplore.application.MyApp;
import com.changhong.fileplore.view.CircleProgress;
import com.chobit.corestorage.CoreApp;
import com.chobit.corestorage.CoreDeviceListener;
import com.changhong.fileplore.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

public class ShowNetDevActivity extends Activity {
	Context context = ShowNetDevActivity.this;
	ProgressDialog dialog;
	ListView netList;
	ArrayList<String> shareList;
	NetDevListAdapter netListAdapter;
	AlertDialog alertDialog;
	AlertDialog.Builder builder;
	CircleProgress mProgressView;
	View layout;
	ArrayList<String> pushList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_net_dev);
		MyApp myapp = (MyApp) getApplication();
		myapp.setContext(this);
		LayoutInflater inflater = getLayoutInflater();
		layout = inflater.inflate(R.layout.circle_progress, (ViewGroup) findViewById(R.id.rl_progress));
		Intent intent = getIntent();
		Bundle b = intent.getBundleExtra("pushList");
		if (b != null) {
			pushList = b.getStringArrayList("pushList");
		}
		builder = new AlertDialog.Builder(this).setView(layout);
		alertDialog = builder.create();
		mProgressView = (CircleProgress) layout.findViewById(R.id.progress);
		netList = (ListView) findViewById(R.id.lv_netactivity);
		// dialog = new MyProgressDialog(ShowNetDevActivity.this).getDialog();

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
		netList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				final DeviceInfo info = (DeviceInfo) parent.getItemAtPosition(position);
				AlertDialog.Builder dialog = new AlertDialog.Builder(ShowNetDevActivity.this);
				dialog.setTitle("");
				String[] dataArray = new String[] { "推送" };
				dialog.setItems(dataArray, new DialogInterface.OnClickListener() {
					/***
					 * 我们这里传递给dialog.setItems方法的参数为数组，这就导致了我们下面的
					 * onclick方法中的which就跟数组下标是一样的，点击hello时返回0；点击baby返回1……
					 */
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							CoreApp.mBinder.PushResourceToDevice(info, pushList);
							break;
						default:
							break;
						}

					}
				}).create().show();
				return true;
			}

		});
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
			// if (dialog.isShowing())
			// dialog.dismiss();
			if (alertDialog.isShowing()) {
				mProgressView.stopAnim();
				alertDialog.dismiss();
			}
		}

		@Override
		public void startWaiting() {
			// dialog.show();
			mProgressView.startAnim();
			alertDialog.show();

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
