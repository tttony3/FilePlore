package com.changhong.fileplore.activities;

import java.lang.ref.WeakReference;
import java.util.List;

import com.changhong.alljoyn.simpleclient.DeviceInfo;
import com.changhong.fileplore.R;

import com.changhong.fileplore.adapter.NetShareFileListAdapter;
import com.changhong.fileplore.utils.MyProgressDialog;
import com.changhong.fileplore.view.CircleProgress;
import com.changhong.synergystorage.javadata.JavaAudioFile;
import com.changhong.synergystorage.javadata.JavaFile;
import com.changhong.synergystorage.javadata.JavaFolder;
import com.changhong.synergystorage.protobufdata.ProtobufData.PBFile;
import com.chobit.corestorage.CoreApp;
import com.chobit.corestorage.CoreShareFileListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
	private TextView tv_path;
	private NetShareFileListAdapter netShareFileListAdapter;
	private MyOnItemClickListener myOnItemClickListener;
	private Handler handler;
	private String path;
	AlertDialog alertDialog;
	AlertDialog.Builder builder;
	CircleProgress mProgressView;
	View layout;
	LayoutInflater inflater;
	DeviceInfo devInfo;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_net_path);
		findView();

		Intent intent = getIntent();
		int position = intent.getFlags();
		devInfo = CoreApp.mBinder.GetDeviceList().get(position);
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
		// dialog = new MyProgressDialog(ShowNetFileActivity.this).getDialog();
		path = "共享文件";
		tv_path.setText(path);
		inflater = getLayoutInflater();
		layout = inflater.inflate(R.layout.circle_progress, (ViewGroup) findViewById(R.id.rl_progress));
		builder = new AlertDialog.Builder(this).setView(layout);
		alertDialog = builder.create();
		mProgressView = (CircleProgress) layout.findViewById(R.id.progress);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			CoreApp.mBinder.DisSession();
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private CoreShareFileListener shareListener = new CoreShareFileListener() {

		@Override
		public void updateShareContent(JavaFolder folder) {
			if(folder !=null){
			shareFileList = folder.getSubFileList();
			shareFolderList = folder.getSubFolderList();

			Message msg = new Message();
			Bundle bundle = new Bundle();
			bundle.putInt("key", UPDATE_LIST);
			msg.setData(bundle);
			handler.sendMessage(msg);
			}
			else Log.e("null!!", "folder is null");
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
		private WeakReference<ShowNetFileActivity> mActivity;

		public MyNetHandler(ShowNetFileActivity activity) {
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
			//		lv_sharepath.setAdapter(netShareFileListAdapter);
					break;

				case SHOW_DIALOG:
					mProgressView.startAnim();
					alertDialog.show();
					break;

				case DISMISS_DIALOG:
					if (alertDialog.isShowing()) {
						mProgressView.stopAnim();
						alertDialog.dismiss();
					}
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

			final JavaFile file;
			JavaFolder folder;
			if (position < shareFolderList.size()) {
				folder = (JavaFolder) parent.getItemAtPosition(position);
	//			folder.getSubFileList();
//				Log.e("SubFolderList",
//						folder.getName() + " file getSubFileNum " + folder.getSubFileNum() + "  folder getSubFolderNum " + folder.getSubFolderNum());
				JavaFolder childrenfolder = new JavaFolder();
				childrenfolder.setParent(folder);
				Log.e("folder loc", folder.getLocation());
				Log.e("loc", childrenfolder.getLocation());
				CoreApp.mBinder.getFolderChildren(devInfo, folder, folder);
			} else {
				file = (JavaFile) parent.getItemAtPosition(position);
				// byte [] b =file.toSendData();
				// JavaAudioFile newfile = new JavaAudioFile();
				// newfile.fromReceiveData(b);
				Runnable r = new Runnable() {
					public void run() {
						Log.e("down", "" + CoreApp.mBinder.DownloadHttpFile(devInfo.getM_devicename(),
								file.getmHttpURI(), "/storage/sdcard0/tt1"));
					}
				};
				new Thread(r).start();
				Log.e("file name", file.getName());

			}

		}

	}

}
