package com.changhong.fileplore.activities;

import java.lang.ref.WeakReference;
import java.util.List;

import com.changhong.alljoyn.simpleclient.DeviceInfo;
import com.changhong.fileplore.R;

import com.changhong.fileplore.adapter.NetShareFileListAdapter;
import com.changhong.fileplore.utils.MyProgressDialog;
import com.changhong.synergystorage.javadata.JavaFile;
import com.changhong.synergystorage.javadata.JavaFolder;
import com.chobit.corestorage.CoreApp;
import com.chobit.corestorage.CoreShareFileListener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class ShowNetFileActivity extends Activity {
	static private final int UPDATE_LIST = 1;
	static private final int SHOW_DIALOG = 2;
	static private final int DISMISS_DIALOG = 3;
	private static ProgressDialog dialog;
	private List<JavaFile> shareFileList;
	private List<JavaFolder> shareFolderList;
	private static ListView lv_sharepath;
	private  TextView tv_path;
	private NetShareFileListAdapter netShareFileListAdapter;
	private MyOnItemClickListener myOnItemClickListener;
	private Handler handler;
	private String path;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_net_path);
		findView();

		Intent intent = getIntent();
		int position = intent.getFlags();
		DeviceInfo devInfo = CoreApp.mBinder.GetDeviceList().get(position);
		CoreApp.mBinder.setShareFileListener(shareListener);
		CoreApp.mBinder.ConnectDeivce(devInfo);
	
		
		netShareFileListAdapter = new NetShareFileListAdapter(shareFileList, shareFolderList, this);
		myOnItemClickListener = new MyOnItemClickListener();

		lv_sharepath.setAdapter(netShareFileListAdapter);
		lv_sharepath.setOnItemClickListener(myOnItemClickListener);
	}

	private void findView() {
		handler = new MyNetHandler(this);
		tv_path = (TextView) findViewById(R.id.path);
		lv_sharepath = (ListView) findViewById(R.id.lv_netsharepath);
		dialog = new MyProgressDialog(ShowNetFileActivity.this).getDialog();
		path = "共享文件";
		tv_path.setText(path);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			CoreApp.mBinder.setShareFileListener(null);
			CoreApp.mBinder.ConnectDeivce(null);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private CoreShareFileListener shareListener = new CoreShareFileListener() {

		@Override
		public void updateShareContent(JavaFolder folder) {
			shareFileList = folder.getSubFileList();
			shareFolderList = folder.getSubFolderList();
			
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

	class MyNetHandler extends Handler {
		WeakReference<ShowNetFileActivity> mActivity;

		MyNetHandler(ShowNetFileActivity activity) {
			mActivity = new WeakReference<ShowNetFileActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			ShowNetFileActivity theActivity = mActivity.get();
			if (theActivity != null) {
				super.handleMessage(msg);
				int key = msg.getData().getInt("key");
				switch (key) {
				case UPDATE_LIST:
					netShareFileListAdapter.updatelistview(shareFileList, shareFolderList);
					netShareFileListAdapter.notifyDataSetChanged();
					lv_sharepath.setAdapter(netShareFileListAdapter);
					break;

				case SHOW_DIALOG:
					dialog.show();
					break;

				case DISMISS_DIALOG:
					dialog.dismiss();
					break;

				default:
					break;
				}

			}
		}
	};

	private class MyOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			JavaFile file;
			JavaFolder folder;
			if (position < shareFolderList.size()) {
				folder = (JavaFolder) parent.getItemAtPosition(position);
				Log.e("SubFolderList",
						folder.getName() + " file " + folder.getSubFileNum() + "  folder " + folder.getSubFolderNum());

			} else {
				file = (JavaFile) parent.getItemAtPosition(position);
				Log.e("file name", file.getName());
			}

		}

	}
}
