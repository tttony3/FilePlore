package com.changhong.fileplore.activities;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.changhong.fileplore.adapter.ClassifyListAdapter;
import com.changhong.fileplore.utils.Content;
import com.changhong.fileplore.utils.Utils;
import com.changhong.fileplore.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ClassifyListActivity extends Activity {

	static ListView lv_classify;
	TextView tv_dir;
	static TextView tv_count;
	int flg;
	MyHandler handler;
	static ClassifyListAdapter listAdapter;
	static ProgressDialog dialog;

	static class MyHandler extends Handler {
		WeakReference<ClassifyListActivity> mActivity;

		MyHandler(ClassifyListActivity activity) {
			mActivity = new WeakReference<ClassifyListActivity>(activity);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			ClassifyListActivity theActivity = mActivity.get();
			if (theActivity != null) {
				super.handleMessage(msg);
				ArrayList<Content> txts = (ArrayList<Content>) msg.getData().get("docs");
				ArrayList<Content> zips = (ArrayList<Content>) msg.getData().get("zips");
				ArrayList<Content> apks = (ArrayList<Content>) msg.getData().get("apks");
				if (txts != null) {
					listAdapter = new ClassifyListAdapter(txts, theActivity, R.id.img_txt);
					dialog.dismiss();
					lv_classify.setAdapter(listAdapter);
					tv_count.setText(txts.size() + " 项");
				}
				if (zips != null) {
					listAdapter = new ClassifyListAdapter(zips, theActivity, R.id.img_zip);
					dialog.dismiss();
					lv_classify.setAdapter(listAdapter);
					tv_count.setText(zips.size() + " 项");
				}
				if (apks != null) {
					listAdapter = new ClassifyListAdapter(apks, theActivity, R.id.img_apk);
					dialog.dismiss();
					lv_classify.setAdapter(listAdapter);
					tv_count.setText(apks.size() + " 项");
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_classify_list);
		flg = getIntent().getFlags();
		handler = new MyHandler(this);
		findView();
		initView(flg);
	}

	private void initView(int flg) {

		switch (flg) {
		case R.id.img_music:
			ArrayList<Content> musics = Utils.getMusic(this);
			listAdapter = new ClassifyListAdapter(musics, this, R.id.img_music);
			lv_classify.setAdapter(listAdapter);
			tv_count.setText(musics.size() + " 项");
			break;
		case R.id.img_txt:
			showDialog();
			new Thread(new GetRunnable("doc", false)).start();

			break;
		case R.id.img_zip:
			showDialog();
			new Thread(new GetRunnable("zip", false)).start();

			break;
		case R.id.img_apk:
			showDialog();
			new Thread(new GetRunnable("apk", false)).start();

			break;
		default:
			break;
		}
		lv_classify.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				Content content = (Content) parent.getItemAtPosition(position);
				File file = new File(content.getDir());
				Intent intent = Utils.openFile(file);
				startActivity(intent);

			}

		});
	}

	private void showDialog() {
		dialog = new ProgressDialog(ClassifyListActivity.this);
		// 设置进度条风格，风格为圆形，旋转的
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		// 设置ProgressDialog 标题
		dialog.setTitle("查找中");
		// 设置ProgressDialog 提示信息
		dialog.setMessage("请稍等...");
		// 设置ProgressDialog 标题图标
		// dialog.setIcon(android.R.drawable.ic_dialog_map);
		// 设置ProgressDialog 的一个Button

		// 设置ProgressDialog 的进度条是否不明确
		dialog.setIndeterminate(false);
		// 设置ProgressDialog 是否可以按退回按键取消
		dialog.setCancelable(true);
		// 显示
		dialog.show();
	}

	private void findView() {
		lv_classify = (ListView) findViewById(R.id.file_list);
		tv_dir = (TextView) findViewById(R.id.dir);
		tv_count = (TextView) findViewById(R.id.item_count);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
		}
		return super.onKeyDown(keyCode, event);
	}

	class GetRunnable implements Runnable {
		String type;
		boolean reseach;

		GetRunnable(String type, boolean reseach) {
			this.type = type;
			this.reseach = reseach;
		}

		@Override
		public void run() {
			ArrayList<Content> results = null;
			Message msg = new Message();
			Bundle data = new Bundle();
			if (type.equals("apk")) {
				results = Utils.getApk(ClassifyListActivity.this, reseach);
				data.putSerializable("apks", results);
			} else if (type.equals("doc")) {
				results = Utils.getDoc(ClassifyListActivity.this, reseach);
				data.putSerializable("docs", results);
			} else if (type.equals("zip")) {
				results = Utils.getZip(ClassifyListActivity.this, reseach);
				data.putSerializable("zips", results);
			}

			msg.setData(data);
			handler.sendMessage(msg);

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.reseach, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.item_reseach) {

			switch (flg) {
			case R.id.img_music:
				ArrayList<Content> musics = Utils.getMusic(this);
				listAdapter = new ClassifyListAdapter(musics, this, R.id.img_music);
				lv_classify.setAdapter(listAdapter);
				tv_count.setText(musics.size() + " 项");
				break;
			case R.id.img_txt:
				showDialog();
				new Thread(new GetRunnable("doc", true)).start();

				break;
			case R.id.img_zip:
				showDialog();
				new Thread(new GetRunnable("zip", true)).start();

				break;
			case R.id.img_apk:
				showDialog();
				new Thread(new GetRunnable("apk", true)).start();

				break;
			default:
				break;
			}
			lv_classify.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

					Content content = (Content) parent.getItemAtPosition(position);
					File file = new File(content.getDir());
					Intent intent = Utils.openFile(file);
					startActivity(intent);

				}

			});

		}
		return super.onOptionsItemSelected(item);

	}
}
