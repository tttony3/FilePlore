package com.changhong.fileplore.activities;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.changhong.fileplore.adapter.ClassifyListAdapter;
import com.changhong.fileplore.utils.Content;
import com.changhong.fileplore.utils.Utils;
import com.changhong.fileplore.view.CircleProgress;
import com.chobit.corestorage.CoreApp;
import com.changhong.fileplore.R;

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
				ArrayList<Content> txts = (ArrayList<Content>) msg.getData().get("docs");
				ArrayList<Content> zips = (ArrayList<Content>) msg.getData().get("zips");
				ArrayList<Content> apks = (ArrayList<Content>) msg.getData().get("apks");
				if (txts != null) {
					listAdapter = new ClassifyListAdapter(txts, theActivity, R.id.img_txt);
					// dialog.dismiss();
					if (alertDialog.isShowing()) {
						mProgressView.stopAnim();
						alertDialog.dismiss();
					}
					lv_classify.setAdapter(listAdapter);
					tv_count.setText(txts.size() + " 项");
				}
				if (zips != null) {
					listAdapter = new ClassifyListAdapter(zips, theActivity, R.id.img_zip);
					// dialog.dismiss();
					if (alertDialog.isShowing()) {
						mProgressView.stopAnim();
						alertDialog.dismiss();
					}
					lv_classify.setAdapter(listAdapter);
					tv_count.setText(zips.size() + " 项");
				}
				if (apks != null) {
					listAdapter = new ClassifyListAdapter(apks, theActivity, R.id.img_apk);
					// dialog.dismiss();
					if (alertDialog.isShowing()) {
						mProgressView.stopAnim();
						alertDialog.dismiss();
					}
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
			mProgressView.startAnim();
			alertDialog.show();
			new Thread(new GetRunnable("doc", false)).start();

			break;
		case R.id.img_zip:
			mProgressView.startAnim();
			alertDialog.show();
			new Thread(new GetRunnable("zip", false)).start();

			break;
		case R.id.img_apk:
			mProgressView.startAnim();
			alertDialog.show();
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
		lv_classify.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				final Content content = (Content) parent.getItemAtPosition(position);
				final File file = new File(content.getDir());
				String[] data = { "打开", "删除", "共享" };
				new AlertDialog.Builder(ClassifyListActivity.this).setTitle("选择操作").setItems(data, new OnClickListener() {

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
								//	initView();
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
								Toast.makeText(ClassifyListActivity.this, "AddShareFile  " + s, Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(ClassifyListActivity.this, "服务未开启", Toast.LENGTH_SHORT).show();
							}
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
		String type;
		boolean reseach;

		GetRunnable(String type, boolean reseach) {
			this.type = type;
			this.reseach = reseach;
		}

		@Override
		public void run() {
			
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
				mProgressView.startAnim();
				alertDialog.show();
				new Thread(new GetRunnable("doc", true)).start();

				break;
			case R.id.img_zip:
				mProgressView.startAnim();
				alertDialog.show();
				new Thread(new GetRunnable("zip", true)).start();

				break;
			case R.id.img_apk:
				mProgressView.startAnim();
				alertDialog.show();
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
