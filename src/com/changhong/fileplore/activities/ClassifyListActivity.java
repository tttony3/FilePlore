package com.changhong.fileplore.activities;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.changhong.fileplore.adapter.ClassifyListAdapter;
import com.changhong.fileplore.application.MyApp;
import com.changhong.fileplore.utils.Content;
import com.changhong.fileplore.utils.Utils;
import com.changhong.fileplore.view.CircleProgress;
import com.chobit.corestorage.CoreApp;
import com.changhong.fileplore.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class ClassifyListActivity extends Activity {
	static private final int APK = 1;
	static private final int DOC = 2;
	static private final int ZIP = 3;
	ArrayList<Content> results;
	LayoutInflater inflater;
	AlertDialog alertDialog;
	AlertDialog.Builder builder;
	CircleProgress mProgressView;
	View layout;
	ListView lv_classify;
	TextView tv_dir;
	TextView tv_count;
	int flg;
	MyHandler handler;
	ClassifyListAdapter listAdapter;
	ProgressDialog dialog;

	class MyHandler extends Handler {
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
				ArrayList<Content> data = null;
				switch (msg.getData().getInt("tag")) {
				case APK:
					data = (ArrayList<Content>) msg.getData().get("data");
					listAdapter = new ClassifyListAdapter(data, theActivity, R.id.img_apk);
					break;
				case DOC:
					data = (ArrayList<Content>) msg.getData().get("data");
					listAdapter = new ClassifyListAdapter(data, theActivity, R.id.img_txt);
					break;
				case ZIP:
					data = (ArrayList<Content>) msg.getData().get("data");
					listAdapter = new ClassifyListAdapter(data, theActivity, R.id.img_zip);
					break;

				default:
					break;
				}
				if (alertDialog.isShowing()) {
					mProgressView.stopAnim();
					alertDialog.dismiss();
				}
				lv_classify.setAdapter(listAdapter);
				tv_count.setText(data.size() + " 项");
			}

		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		setContentView(R.layout.activity_classify_list);
		flg = getIntent().getIntExtra("key", 0);
		MyApp myapp = (MyApp) getApplication();
		myapp.setContext(this);
		handler = new MyHandler(this);
		findView();

		initView(flg);
	}

	private void initView(int flg) {

		switch (flg) {
		case R.id.img_music:
//			mProgressView.startAnim();
//			alertDialog.show();
			ArrayList<Content> musics = Utils.getMusic(this);
			listAdapter = new ClassifyListAdapter(musics, this, R.id.img_music);
			lv_classify.setAdapter(listAdapter);
			tv_count.setText(musics.size() + " 项");
//			Message msg = new Message();
//			Bundle data = new Bundle();
//			data.putInt("tag", 123);
//			msg.setData(data);
//			handler.sendMessage(msg);
			break;
		case R.id.img_txt:
			mProgressView.startAnim();
			alertDialog.show();
			new Thread(new GetRunnable(DOC, false)).start();

			break;
		case R.id.img_zip:
			mProgressView.startAnim();
			alertDialog.show();
			new Thread(new GetRunnable(ZIP, false)).start();

			break;
		case R.id.img_apk:
			mProgressView.startAnim();
			alertDialog.show();
			new Thread(new GetRunnable(APK, false)).start();

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
		lv_classify.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				final Content content = (Content) parent.getItemAtPosition(position);
				final File file = new File(content.getDir());
				String[] data = { "打开", "删除", "共享", "推送" };
				new AlertDialog.Builder(ClassifyListActivity.this).setTitle("选择操作")
						.setItems(data, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							Intent intent = Utils.openFile(file);
							startActivity(intent);
							break;
						case 1:
							if (file.exists()) {
								if (file.delete()) {
									results.remove(content);
									listAdapter.updateList(results);
									Toast.makeText(ClassifyListActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
									// initView();
								} else {
									Toast.makeText(ClassifyListActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
								}
							} else {
								Toast.makeText(ClassifyListActivity.this, "文件不存在", Toast.LENGTH_SHORT).show();
							}
							break;
						case 2:
							if (CoreApp.mBinder.isBinderAlive()) {
								String s = CoreApp.mBinder.AddShareFile(file.getPath());
								Toast.makeText(ClassifyListActivity.this, "AddShareFile  " + s, Toast.LENGTH_SHORT)
										.show();
							} else {
								Toast.makeText(ClassifyListActivity.this, "服务未开启", Toast.LENGTH_SHORT).show();
							}
							break;
						case 3:
							ArrayList<String> pushList = new ArrayList<String>();
							if (!file.isDirectory()) {
								MyApp myapp = (MyApp) getApplication();
								String ip = myapp.getIp();
								int port = myapp.getPort();
								pushList.add("http://" + ip + ":" + port + file.getPath());
							} else {
								Toast.makeText(ClassifyListActivity.this, "文件夹暂不支持推送", Toast.LENGTH_SHORT).show();
							}

							intent = new Intent();
							Bundle b = new Bundle();
							b.putStringArrayList("pushList", pushList);
							intent.putExtra("pushList", b);
							intent.setClass(ClassifyListActivity.this, ShowNetDevActivity.class);
							startActivity(intent);

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

	private void findView() {
		inflater = getLayoutInflater();
		lv_classify = (ListView) findViewById(R.id.file_list);
		tv_dir = (TextView) findViewById(R.id.dir);
		tv_count = (TextView) findViewById(R.id.item_count);
		layout = inflater.inflate(R.layout.circle_progress, (ViewGroup) findViewById(R.id.rl_progress));
		builder = new AlertDialog.Builder(ClassifyListActivity.this).setView(layout);
		alertDialog = builder.create();
		mProgressView = (CircleProgress) layout.findViewById(R.id.progress);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
		}
		return super.onKeyDown(keyCode, event);
	}

	class GetRunnable implements Runnable {
		int type;
		boolean reseach;

		GetRunnable(int type, boolean reseach) {
			this.type = type;
			this.reseach = reseach;
		}

		@Override
		public void run() {

			Message msg = new Message();
			Bundle data = new Bundle();
		
			if (type == APK) {
				results = Utils.getApk(ClassifyListActivity.this, reseach);
				data.putSerializable("data", results);
				data.putInt("tag", APK);
			} else if (type == DOC) {
				results = Utils.getDoc(ClassifyListActivity.this, reseach);
				data.putSerializable("data", results);
				data.putInt("tag", DOC);
			} else if (type == ZIP) {
				results = Utils.getZip(ClassifyListActivity.this, reseach);
				data.putSerializable("data", results);
				data.putInt("tag", ZIP);
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
				mProgressView.startAnim();
				alertDialog.show();
				new Thread(new GetRunnable(DOC, true)).start();

				break;
			case R.id.img_zip:
				mProgressView.startAnim();
				alertDialog.show();
				new Thread(new GetRunnable(ZIP, true)).start();

				break;
			case R.id.img_apk:
				mProgressView.startAnim();
				alertDialog.show();
				new Thread(new GetRunnable(APK, true)).start();

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
