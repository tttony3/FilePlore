package com.changhong.fileplore.activities;

import java.io.File;
import java.util.ArrayList;

import com.changhong.fileplore.adapter.ClassifyGridAdapter;
import com.changhong.fileplore.adapter.PhotoGirdAdapter;
import com.changhong.fileplore.base.AbsListViewBaseActivity;
import com.changhong.fileplore.utils.Content;
import com.changhong.fileplore.utils.Utils;
import com.example.fileplore.R;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

public class PhotoGridActivity extends AbsListViewBaseActivity {
	GridView gv_classify;
	TextView tv_dir;
	int flg;
	Handler handler;
	ClassifyGridAdapter gridAdapter;
	PhotoGirdAdapter imageAdapter;
	String[] content;
	ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		imageLoader.init(ImageLoaderConfiguration.createDefault(this));
		setContentView(R.layout.activity_classify_grid);
		content = getIntent().getStringArrayExtra("content");

		findView();
		initView();
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				Content content = (Content) parent.getItemAtPosition(position);
				File file = new File(content.getDir());
				Intent intent = Utils.openFile(file);
				startActivity(intent);

			}
		});
	}

	private void initView() {
		showDialog();
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				@SuppressWarnings("unchecked")
				ArrayList<Content> results = (ArrayList<Content>) msg.getData().get("txts");
				imageAdapter = new PhotoGirdAdapter(results, PhotoGridActivity.this, imageLoader);
				dialog.dismiss();
				listView.setAdapter(imageAdapter);
				super.handleMessage(msg);
			}

		};
		new Thread() {
			@Override
			public void run() {
				ContentResolver cr = getContentResolver();
				ArrayList<Content> results = new ArrayList<Content>();
				File file = new File(content[0].substring(0, content[0].lastIndexOf("/")));

				if (file.isDirectory()) {
					String whereClause1 = MediaStore.Images.Media.DATA + " like '" + file.getPath() + "%'";
					Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, whereClause1, null,
							null);
					cursor.moveToFirst();

					for (int i = 0; i < cursor.getCount(); i++) {

						int origId = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
						Content result = new Content(
								cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)),
								cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)), null,
								origId);

						results.add(result);
						cursor.moveToNext();

					}
					cursor.close();

				}
				Message msg = new Message();
				Bundle data = new Bundle();
				data.putSerializable("txts", results);
				msg.setData(data);
				handler.sendMessage(msg);
			}
		}.start();

	}

	void findView() {
		listView = (AbsListView) findViewById(R.id.gv_classify);
		// gv_classify = (GridView) findViewById(R.id.gv_classify);
		tv_dir = (TextView) findViewById(R.id.tv_classify_dir);
	}

	private void showDialog() {
		dialog = new ProgressDialog(PhotoGridActivity.this);
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

}
