package com.changhong.fileplore.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.changhong.fileplore.R;
import com.changhong.fileplore.adapter.PloreListAdapter;
import com.changhong.fileplore.base.BaseActivity;
import com.changhong.fileplore.data.PloreData;
import com.changhong.fileplore.utils.Utils;
import com.changhong.fileplore.view.RefreshListView;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class QQListActivity extends BaseActivity implements OnItemClickListener {
	private RefreshListView lv_classify;
	private TextView tv_dir;
	private TextView tv_count;
	private List<File> files = new ArrayList<File>();
	private LinkedList<File> father = new LinkedList<File>();
	private PloreListAdapter qqAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		setContentView(R.layout.activity_classify_list);
		findView();
		initView();

	}

	private void initView() {
		int flag = getIntent().getFlags();
		PloreData mPloreData = new PloreData();
		File file;
		File sdfile;
		switch (flag) {

		case R.id.img_wechat:
			
			ArrayList<File> wcfiles = new ArrayList<File>();
			tv_dir.setText("微信小视频");
			file = new File("/storage/sdcard0/tencent/MicroMsg");
			sdfile = new File("/storage/sdcard1/tencent/MicroMsg");
			if (file.exists()) {
				File[] mfils1 = file.listFiles();
				for (int i = 0; i < mfils1.length; i++) {
					if (mfils1[i].getName().length() > 25 && mfils1[i].isDirectory())
						wcfiles.add(mfils1[i]);

				}
			}
			if (sdfile.exists()) {
				File[] mfils2 = sdfile.listFiles();
				for (int i = 0; i < mfils2.length; i++) {
					if (mfils2[i].getName().length() > 25&&mfils2[i].isDirectory())
						wcfiles.add(mfils2[i]);

				}
			}
			for(int i=0;i<wcfiles.size();i++){
				
				file =new File(wcfiles.get(i).getPath()+"/video");
				Log.e("filespath", file.getPath());
				files.addAll(mPloreData.lodaData(file));
			}
			for (int i = 0; i < files.size(); i++) {
				if (files.get(i).getName().startsWith(".")|| !Utils.getMIMEType(files.get(i)).equals("video/*")) {
					files.remove(i);
					i--;
				}
				
			}
			break;
		case R.id.img_qq:
			tv_dir.setText("QQ接收文件");
			file = new File("/storage/sdcard0/tencent/QQfile_recv");
			sdfile = new File("/storage/sdcard1/tencent/QQfile_recv");
			mPloreData = new PloreData();

			if (file.exists() && file.isDirectory()) {
				files.addAll(mPloreData.lodaData(file));
			}
			if (sdfile.exists() && sdfile.isDirectory()) {
				files.addAll(mPloreData.lodaData(sdfile));
			}
			for (int i = 0; i < files.size(); i++) {
				if (files.get(i).getName().startsWith(".")) {
					files.remove(i);
					i--;
				}
			}
			break;
		default:
			break;
		}

		
		tv_count.setText(files.size() + "项");
		qqAdapter = new PloreListAdapter(QQListActivity.this, files, true, ImageLoader.getInstance());
		lv_classify.setAdapter(qqAdapter);
		lv_classify.setOnItemClickListener(this);

	}

	@Override
	protected void findView() {
		getLayoutInflater();
		lv_classify = (RefreshListView) findViewById(R.id.file_list);
		tv_dir = (TextView) findViewById(R.id.path);
		tv_count = (TextView) findViewById(R.id.item_count);

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		File file = (File) parent.getItemAtPosition(position);
		if (file.isDirectory()) {
			PloreData mPloreData = new PloreData();
			files.clear();
			files.addAll(mPloreData.lodaData(file));
			for (int i = 0; i < files.size(); i++) {
				if (files.get(i).getName().startsWith(".")) {
					files.remove(i);
					i--;
				}
			}
			qqAdapter.updateList(files);
			father.add(file.getParentFile());

		} else {
			startActivity(Utils.openFile(file));
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			File file = father.pollLast();
			if (file != null) {
				PloreData mPloreData = new PloreData();
				files.clear();
				files.addAll(mPloreData.lodaData(file));
				for (int i = 0; i < files.size(); i++) {
					if (files.get(i).getName().startsWith(".")) {
						files.remove(i);
						i--;
					}
				}
				qqAdapter.updateList(files);
				return true;
			}

		}
		return super.onKeyDown(keyCode, event);
	}
}
