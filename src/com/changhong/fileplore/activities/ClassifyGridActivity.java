package com.changhong.fileplore.activities;

import java.io.File;
import java.util.ArrayList;

import com.changhong.fileplore.utils.Utils;
import com.chobit.corestorage.CoreApp;
import com.changhong.fileplore.adapter.*;
import com.changhong.fileplore.application.MyApp;
import com.changhong.fileplore.R;
import com.changhong.fileplore.utils.Content;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
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
	ClassifyGridAdapter gridAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_classify_grid);
		MyApp myapp = (MyApp) getApplication();
		myapp.setContext(this);
		flg = getIntent().getFlags();
		findView();
		initView(flg);
	}

	void findView() {
		gv_classify = (GridView) findViewById(R.id.gv_classify);
		tv_dir = (TextView) findViewById(R.id.tv_classify_dir);
	}

	void initView(int flg) {

		switch (flg) {

		case R.id.img_movie:
			ArrayList<Content> videos = Utils.getVideo(this);
			gridAdapter = new ClassifyGridAdapter(videos, this, R.id.img_movie);
			gv_classify.setAdapter(gridAdapter);
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
					String[] data = { "打开", "删除", "共享" };
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
			ArrayList<Content> photos = Utils.getPhotoCata(this);
			gridAdapter = new ClassifyGridAdapter(photos, this, R.id.img_photo);
			gv_classify.setAdapter(gridAdapter);
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

	class OnPhotoItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			Content content = (Content) parent.getItemAtPosition(position);
			Intent intent = new Intent();
			String[] s = new String[] { content.getDir(), content.getTitle() };
			intent.putExtra("content", s);
			intent.setClass(ClassifyGridActivity.this, PhotoGridActivity.class);
			startActivity(intent);
			ClassifyGridActivity.this.overridePendingTransition(android.R.anim.slide_in_left,
					android.R.anim.slide_out_right);

		}
	}
}
