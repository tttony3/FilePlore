package com.changhong.fileplore.activities;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.changhong.fileplore.utils.Utils;
import com.changhong.fileplore.view.CircleProgress;
import com.chobit.corestorage.CoreApp;
import com.changhong.fileplore.adapter.*;
import com.changhong.fileplore.application.MyApp;
import com.changhong.fileplore.R;
import com.changhong.fileplore.utils.Content;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class ClassifyGridActivity extends Activity {
	GridView gv_classify;
	TextView tv_dir;
	int flg;
	AlertDialog alertDialog;
	AlertDialog.Builder builder;
	CircleProgress mProgressView;
	ClassifyGridAdapter gridAdapter;
	View layout;
	LayoutInflater inflater;
	ArrayList<Content> results;
	MyHandler handler ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_classify_grid);
		MyApp myapp = (MyApp) getApplication();
		myapp.setContext(this);
		flg = getIntent().getIntExtra("key", 0);
		findView();
		initView(flg);
	}

	void findView() {
		inflater = getLayoutInflater();
		gv_classify = (GridView) findViewById(R.id.gv_classify);
		tv_dir = (TextView) findViewById(R.id.tv_classify_dir);
		handler = new MyHandler(this);
		layout = inflater.inflate(R.layout.circle_progress, (ViewGroup) findViewById(R.id.rl_progress));
		builder = new AlertDialog.Builder(ClassifyGridActivity.this).setView(layout);
		alertDialog = builder.create();
		mProgressView = (CircleProgress) layout.findViewById(R.id.progress);
	}

	void initView(int flg) {
		mProgressView.startAnim();
		alertDialog.show();
		switch (flg) {

		case R.id.img_movie:
			new Thread(new GridRunnable(R.id.img_movie)).start();
			gv_classify.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

					Content content = (Content) parent.getItemAtPosition(position);
					File file = new File(content.getDir());
					Intent intent = Utils.openFile(file);
					startActivity(intent);

				}
			});
			gv_classify.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					final Content content = (Content) parent.getItemAtPosition(position);
					final File file = new File(content.getDir());
					String[] data = { "打开", "删除", "共享", "推送" };
					new AlertDialog.Builder(ClassifyGridActivity.this).setTitle("选择操作")
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
										Toast.makeText(ClassifyGridActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
										ArrayList<Content> videos = Utils.getVideo(ClassifyGridActivity.this);
										gridAdapter.updateList(videos);
									} else {
										Toast.makeText(ClassifyGridActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
									}
								} else {
									Toast.makeText(ClassifyGridActivity.this, "文件不存在", Toast.LENGTH_SHORT).show();
								}
								break;
							case 2:
								if (CoreApp.mBinder.isBinderAlive()) {
									String s = CoreApp.mBinder.AddShareFile(file.getPath());
									Toast.makeText(ClassifyGridActivity.this, "AddShareFile  " + s, Toast.LENGTH_SHORT)
											.show();
								} else {
									Toast.makeText(ClassifyGridActivity.this, "服务未开启", Toast.LENGTH_SHORT).show();
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
									Toast.makeText(ClassifyGridActivity.this, "文件夹暂不支持推送", Toast.LENGTH_SHORT).show();
								}

								intent = new Intent();
								Bundle b = new Bundle();
								b.putStringArrayList("pushList", pushList);
								intent.putExtra("pushList", b);
								intent.setClass(ClassifyGridActivity.this, ShowNetDevActivity.class);
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
			break;
		case R.id.img_photo:
			// CircleProgress cp = new CircleProgress(this);
			new Thread(new GridRunnable(R.id.img_photo)).start();
//			ArrayList<Content> photos = Utils.getPhotoCata(this);
//			gridAdapter = new ClassifyGridAdapter(photos, this, R.id.img_photo);
//			gv_classify.setAdapter(gridAdapter);
			gv_classify.setOnItemClickListener(new OnPhotoItemClickListener());
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {

		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	class OnPhotoItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			Content content = (Content) parent.getItemAtPosition(position);
			Intent intent = new Intent();
			String[] s = new String[] { content.getDir(), content.getTitle() };
			intent.putExtra("content", s);
			intent.setClass(ClassifyGridActivity.this, PhotoGridActivity.class);
			startActivity(intent);

		}
	}

	class GridRunnable implements Runnable {
		int id;

		public GridRunnable(int id) {
			this.id = id;
		}

		@Override
		public void run() {
			Message msg = new Message();
			Bundle data = new Bundle();
			switch (id) {
			case R.id.img_movie:
				results = Utils.getVideo(ClassifyGridActivity.this);
				data.putSerializable("data", results);
				data.putInt("tag", R.id.img_movie);
				break;
			case R.id.img_photo:
				results = Utils.getPhotoCata(ClassifyGridActivity.this);
				data.putSerializable("data", results);
				data.putInt("tag", R.id.img_photo);
				break;
			default:
				break;
			}
			msg.setData(data);
			handler.sendMessage(msg);
		}

	}
	
	class MyHandler extends Handler {
		WeakReference<ClassifyGridActivity> mActivity;

		MyHandler(ClassifyGridActivity activity) {
			mActivity = new WeakReference<ClassifyGridActivity>(activity);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			ClassifyGridActivity theActivity = mActivity.get();
			if (theActivity != null) {
				super.handleMessage(msg);
				ArrayList<Content> data = null;
				switch (msg.getData().getInt("tag")) {
				case R.id.img_movie:
					data = (ArrayList<Content>) msg.getData().get("data");
					gridAdapter = new ClassifyGridAdapter(data, theActivity, R.id.img_movie);
					gv_classify.setAdapter(gridAdapter);
					break;
				case R.id.img_photo:
					data = (ArrayList<Content>) msg.getData().get("data");
					gridAdapter = new ClassifyGridAdapter(data, theActivity, R.id.img_movie);
					gv_classify.setAdapter(gridAdapter);
					break;

				default:
					break;
				}
				if (alertDialog.isShowing()) {
					mProgressView.stopAnim();
					alertDialog.dismiss();
				}
			}

		}
	}
}
