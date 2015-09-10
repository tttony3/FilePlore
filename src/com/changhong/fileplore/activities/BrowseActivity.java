package com.changhong.fileplore.activities;

import com.changhong.fileplore.utils.Utils;
import com.example.fileplore.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BrowseActivity extends Activity {
	TextView tv_phoneTotal;
	TextView tv_phoneNotUse;
	TextView tv_sdTotal;
	TextView tv_sdNotUse;
	ProgressBar pb_phone;
	ProgressBar pb_sd;
	ImageView iv_apk;
	ImageView iv_movie;
	ImageView iv_music;
	ImageView iv_photo;
	ImageView iv_txt;
	ImageView iv_zip;

	TextView tv_apk;
	TextView tv_movie;
	TextView tv_music;
	TextView tv_photo;
	TextView tv_doc;
	TextView tv_zip;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browse);

		findView();
		setView();
		setOnClickListener();

	}

	private void setOnClickListener() {
		MyClickListener myclick = new MyClickListener();
		iv_apk.setOnClickListener(myclick);
		iv_movie.setOnClickListener(myclick);
		iv_music.setOnClickListener(myclick);
		iv_photo.setOnClickListener(myclick);
		iv_txt.setOnClickListener(myclick);
		iv_zip.setOnClickListener(myclick);

	}

	void findView() {

		iv_apk = (ImageView) findViewById(R.id.img_apk);
		iv_movie = (ImageView) findViewById(R.id.img_movie);
		iv_music = (ImageView) findViewById(R.id.img_music);
		iv_photo = (ImageView) findViewById(R.id.img_photo);
		iv_txt = (ImageView) findViewById(R.id.img_txt);
		iv_zip = (ImageView) findViewById(R.id.img_zip);

		tv_apk = (TextView) findViewById(R.id.tv_apk);
		tv_movie = (TextView) findViewById(R.id.tv_movie);
		tv_music = (TextView) findViewById(R.id.tv_music);
		tv_photo = (TextView) findViewById(R.id.tv_photo);
		tv_doc = (TextView) findViewById(R.id.tv_doc);
		tv_zip = (TextView) findViewById(R.id.tv_zip);

		pb_phone = (ProgressBar) findViewById(R.id.browse_phone);
		pb_sd = (ProgressBar) findViewById(R.id.browse_sd);

		tv_phoneTotal = (TextView) findViewById(R.id.tv_phonetotal);
		tv_phoneNotUse = (TextView) findViewById(R.id.tv_phonenotuse);
		tv_sdTotal = (TextView) findViewById(R.id.tv_sdtotal);
		tv_sdNotUse = (TextView) findViewById(R.id.tv_sdnotuse);
	}

	void setView() {
		String[] phoneSpace = Utils.getPhoneSpace(this);
		String[] sdSpace = Utils.getSdSpace(this);
		tv_phoneTotal.setText(phoneSpace[0]);
		tv_phoneNotUse.setText(phoneSpace[1]);
		tv_sdTotal.setText(sdSpace[0]);
		tv_sdNotUse.setText(sdSpace[1]);
		pb_sd.setProgress(100-Integer.parseInt(sdSpace[2].substring(0, 2)));
		pb_phone.setProgress(100-Integer.parseInt(phoneSpace[2].substring(0, 2)));
		tv_apk.append("(" + Utils.getCount("result_apk", BrowseActivity.this) + ")");
		tv_movie.append("(" + Utils.getCount("result_movie", BrowseActivity.this) + ")");
		tv_music.append("(" + Utils.getCount("result_music", BrowseActivity.this) + ")");
		tv_photo.append("(" + Utils.getCount("result_photo", BrowseActivity.this) + ")");
		tv_doc.append("(" + Utils.getCount("result_doc", BrowseActivity.this) + ")");
		tv_zip.append("(" + Utils.getCount("result_zip", BrowseActivity.this) + ")");
	}

	class MyClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			switch (v.getId()) {
			case R.id.img_apk:

				intent.setClass(BrowseActivity.this, ClassifyListActivity.class);
				intent.setFlags(R.id.img_apk);

				break;
			case R.id.img_movie:

				intent.setClass(BrowseActivity.this, ClassifyGridActivity.class);
				intent.setFlags(R.id.img_movie);
				break;
			case R.id.img_music:

				intent.setClass(BrowseActivity.this, ClassifyListActivity.class);
				intent.setFlags(R.id.img_music);
				break;
			case R.id.img_photo:

				intent.setClass(BrowseActivity.this, ClassifyGridActivity.class);
				intent.setFlags(R.id.img_photo);
				break;
			case R.id.img_txt:

				intent.setClass(BrowseActivity.this, ClassifyListActivity.class);
				intent.setFlags(R.id.img_txt);
				break;
			case R.id.img_zip:

				intent.setClass(BrowseActivity.this, ClassifyListActivity.class);
				intent.setFlags(R.id.img_zip);
				break;
			default:
				break;
			}
			startActivity(intent);
			getParent().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

		}
	}
}
