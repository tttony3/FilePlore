package com.changhong.fileplore.activities;

import java.io.File;
import java.util.ArrayList;

import com.changhong.fileplore.utils.Utils;
import com.changhong.fileplore.adapter.*;
import com.example.fileplore.R;
import com.changhong.fileplore.utils.Content;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

public class ClassifyGridActivity extends Activity {
	GridView gv_classify;
	TextView tv_dir;
	int flg;
	GridAdapter gridAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_classify_grid);
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
			gridAdapter = new GridAdapter(videos, this, R.id.img_movie);
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
			break;
		case R.id.img_photo:
			ArrayList<Content> photos = Utils.getPhotoCata(this);
			gridAdapter = new GridAdapter(photos, this, R.id.img_photo);
			gv_classify.setAdapter(gridAdapter);
			gv_classify.setOnItemClickListener(new myOnItemClickListener());
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

	class myOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			Content content = (Content) parent.getItemAtPosition(position);
			Intent intent = new Intent();
			String[] s = new String[] { content.getDir(), content.getTitle() };
			intent.putExtra("content", s);
			intent.setClass(ClassifyGridActivity.this, PhotoGridActivity.class);
			startActivity(intent);
			// onPause();
			ClassifyGridActivity.this.overridePendingTransition(android.R.anim.slide_in_left,
					android.R.anim.slide_out_right);

		}
	}
}
