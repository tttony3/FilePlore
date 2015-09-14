package com.changhong.fileplore.activities;

import java.lang.ref.WeakReference;
import java.util.List;

import com.changhong.fileplore.R;
import com.changhong.fileplore.adapter.NetShareFileListAdapter;
import com.changhong.synergystorage.javadata.JavaFile;
import com.changhong.synergystorage.javadata.JavaFolder;
import com.chobit.corestorage.CoreApp;
import com.chobit.corestorage.CoreShareFileListener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

public class ShowNetSharePathActivity extends Activity {
	static ProgressDialog dialog;
	static List<JavaFile> shareFileList;
	static List<JavaFolder> shareFolderList;
	ListView lv_sharepath;
	static TextView tv_path;
	static NetShareFileListAdapter netShareFileListAdapter;
	Handler handler;
	static String path;
	static final int UPDATE_LIST = 1;
	static final int SHOW_DIALOG = 2;
	static final int DISMISS_DIALOG = 3;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_net_path);
		// dialog = new
		// MyProgressDialog(ShowNetSharePathActivity.this).getDialog();
		dialog = new ProgressDialog(ShowNetSharePathActivity.this);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setTitle("查找中");
		dialog.setMessage("请稍等...");
		dialog.setIndeterminate(false);
		dialog.setCancelable(true);
		handler = new MyNetHandler(this);
		findView();

		netShareFileListAdapter = new NetShareFileListAdapter(shareFileList, shareFolderList, this);
		// Intent intent = getIntent();
		// DeviceInfo devInfo = (DeviceInfo)
		// intent.getBundleExtra("devinfo").get("devinfo");

		// CoreApp.mBinder.ConnectDeivce(devInfo);
		CoreApp.mBinder.setShareFileListener(shareListener);
		lv_sharepath.setAdapter(netShareFileListAdapter);
		// netShareFileListAdapter = new netShareFileListAdapter(null, this);
		// lv_sharepath.setAdapter(netShareFileListAdapter);
		// Log.e("devInfo", devInfo.getM_devicename() + "");
	}

	private void findView() {
		tv_path = (TextView) findViewById(R.id.path);
		lv_sharepath = (ListView) findViewById(R.id.lv_netsharepath);

	}

	private CoreShareFileListener shareListener = new CoreShareFileListener() {

		@Override
		public void updateShareContent(JavaFolder folder) {
			shareFileList = folder.getSubFileList();
			shareFolderList = folder.getSubFolderList();
			// Log.e("FolderList", folder.getSubFolderList().toString());
			// path = folder.getName();
			// Log.e("FileList", folder.getSubFileList().toString());

			Message msg = new Message();
			Bundle bundle = new Bundle();
			bundle.putInt("key", UPDATE_LIST);
			msg.setData(bundle);
			handler.sendMessage(msg);

		}

		@Override
		public void updateImageThumbNails(Bitmap bt) {
			// TODO Auto-generated method stub

		}

		@Override
		public void stopWaiting() {
			Message msg = new Message();
			Bundle bundle = new Bundle();
			bundle.putInt("key", DISMISS_DIALOG);
			msg.setData(bundle);
			handler.sendMessage(msg);

		}

		@Override
		public void startWaiting() {
			Message msg = new Message();
			Bundle bundle = new Bundle();
			bundle.putInt("key", SHOW_DIALOG);
			msg.setData(bundle);
			handler.sendMessage(msg);

		}
	};

	static class MyNetHandler extends Handler {
		WeakReference<ShowNetSharePathActivity> mActivity;

		MyNetHandler(ShowNetSharePathActivity activity) {
			mActivity = new WeakReference<ShowNetSharePathActivity>(activity);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			ShowNetSharePathActivity theActivity = mActivity.get();
			Log.e("Message", "Message");
			if (theActivity != null) {
				super.handleMessage(msg);
				int key = msg.getData().getInt("key");
				Log.e("switch", key + "");
				switch (key) {
				case UPDATE_LIST:
					Log.e("handle", "handle");
					tv_path.setText(path);
					netShareFileListAdapter.updatelistview(shareFileList, shareFolderList);
					break;
				case SHOW_DIALOG:
					// dialog.show();
					break;
				case DISMISS_DIALOG:
					// dialog.dismiss();
					break;

				default:
					break;
				}

			}
		}
	};
}
