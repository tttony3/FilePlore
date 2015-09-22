package com.changhong.fileplore.activities;

import java.io.File;
import java.util.ArrayList;
import com.changhong.fileplore.adapter.ClassifyGridAdapter;
import com.changhong.fileplore.adapter.PhotoGirdAdapter;
import com.changhong.fileplore.application.MyApp;
import com.changhong.fileplore.base.AbsListViewBaseActivity;
import com.changhong.fileplore.utils.Content;
import com.changhong.fileplore.utils.Utils;
import com.chobit.corestorage.CoreApp;
import com.changhong.fileplore.R;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class PhotoGridActivity extends AbsListViewBaseActivity {

	TextView tv_dir;
	ArrayList<Content> results ;
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
		MyApp myapp = (MyApp) getApplication();
		myapp.setContext(this);
		setContentView(R.layout.activity_classify_grid);
		content = getIntent().getStringArrayExtra("content");
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
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				final Content content = (Content) parent.getItemAtPosition(position);
				final File file = new File(content.getDir());
				String[] data = { "打开", "删除", "共享" };
				new AlertDialog.Builder(PhotoGridActivity.this).setTitle("选择操作").setItems(data, new OnClickListener() {

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
									imageAdapter.updateList(results);
									Toast.makeText(PhotoGridActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
								//	initView();
								} else {
									Toast.makeText(PhotoGridActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
								}
							} else {
								Toast.makeText(PhotoGridActivity.this, "文件不存在", Toast.LENGTH_SHORT).show();
							}
							break;
						case 2:
							if (CoreApp.mBinder.isBinderAlive()) {
								String s = CoreApp.mBinder.AddShareFile(file.getPath());
								Toast.makeText(PhotoGridActivity.this, "AddShareFile  " + s, Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(PhotoGridActivity.this, "服务未开启", Toast.LENGTH_SHORT).show();
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

	private void initView() {
		showDialog();

		new Thread() {
			@Override
			public void run() {
				ContentResolver cr = getContentResolver();
				 results = new ArrayList<Content>();
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
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setTitle("查找中");
		dialog.setMessage("请稍等...");
		dialog.setIndeterminate(false);
		dialog.setCancelable(true);
		dialog.show();
		
	}

}
