package com.changhong.fileplore.activities;

import java.lang.ref.WeakReference;
import java.util.List;

import com.changhong.fileplore.R;
import com.changhong.fileplore.activities.ShowNetFileActivity.DownloadImageTask;
import com.changhong.fileplore.adapter.NetPushFileListAdapter;
import com.changhong.fileplore.base.BaseActivity;
import com.changhong.fileplore.utils.MyCoreDownloadProgressCB;
import com.chobit.corestorage.CoreApp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ShowPushFileActivity extends BaseActivity implements OnItemClickListener {
	private ListView lv_pushfile;
	private TextView tv_pushfilepath;
	private TextView tv_pushfilenum;
	private List<String> pushList;
	private NetPushFileListAdapter netPushFileListAdapter;
	private Handler handler;
	private View layout_download;
	private AlertDialog alertDialog_download;
	private AlertDialog.Builder builder_download;
	private Button btn_stop;
	private ProgressBar pb_download;
	private TextView tv_download;
	private LayoutInflater inflater;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_push_file);
		findView();
		Intent intent = getIntent();
		pushList = intent.getStringArrayListExtra("pushList");
		tv_pushfilenum.setText(pushList.size() + "项");
		netPushFileListAdapter = new NetPushFileListAdapter(pushList, this);
		lv_pushfile.setAdapter(netPushFileListAdapter);
		lv_pushfile.setOnItemClickListener(this);
		CoreApp.mBinder.setDownloadCBInterface(new MyCoreDownloadProgressCB(handler, this));

	}

	@Override
	protected void findView() {
		lv_pushfile = findView(R.id.lv_pushfile);
		tv_pushfilepath = findView(R.id.tv_pushfilepath);
		tv_pushfilenum = findView(R.id.tv_pushfilenum);

		handler = new MyPushHandler(this);
		inflater = getLayoutInflater();
		layout_download = inflater.inflate(R.layout.dialog_download, null);
		builder_download = new AlertDialog.Builder(this).setView(layout_download);
		alertDialog_download = builder_download.create();
		btn_stop = (Button) layout_download.findViewById(R.id.download_stop);
		pb_download = (ProgressBar) layout_download.findViewById(R.id.download_bar);
		tv_download = (TextView) layout_download.findViewById(R.id.tv_process);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		String fileLocation = (String) parent.getItemAtPosition(position);
		CoreApp.mBinder.DownloadHttpFile("client", fileLocation, null);

	}

	class MyPushHandler extends Handler {
		private WeakReference<ShowPushFileActivity> mActivity;

		public MyPushHandler(ShowPushFileActivity activity) {
			mActivity = new WeakReference<ShowPushFileActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			ShowPushFileActivity theActivity = mActivity.get();
			if (theActivity != null) {
				super.handleMessage(msg);
				int key = msg.getData().getInt("key");
				switch (key) {
				case MyCoreDownloadProgressCB.UPDATE_DOWNLOAD_BAR:
					if (!alertDialog_download.isShowing()) {
						long total = msg.getData().getLong("total");
						int max = (int) (total / 100);
						pb_download.setMax(max);
						tv_download.setText("0%");
						alertDialog_download.show();
					} else {
						long part = msg.getData().getLong("part");
						pb_download.setProgress((int) (part / 100));
						int p = (int) (part / pb_download.getMax());
						tv_download.setText(p + "%");
					}
					break;
				case MyCoreDownloadProgressCB.DISMISS_DOWN_BAR:
					if (alertDialog_download.isShowing())
						alertDialog_download.dismiss();
					break;
				default:
					break;
				}

			}
		}
	};
}
