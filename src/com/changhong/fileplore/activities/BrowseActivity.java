package com.changhong.fileplore.activities;

import com.changhong.fileplore.utils.Utils;

import java.util.HashMap;
import java.util.Map;

import com.changhong.fileplore.R;
import com.changhong.fileplore.application.MyApp;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
		MyApp myapp = (MyApp) getApplication();
		myapp.setContext(this);
		findView();
		setView();

	}

	public void callupdate() {
		new MyAsyncTask().execute();
		new ProgressAsyncTask().execute();
	}

	@Override
	protected void onResume() {
		Log.e("onResume", "browse");
		new MyAsyncTask().execute();
		new ProgressAsyncTask().execute();
		super.onResume();
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
		new MyAsyncTask().execute();
		new ProgressAsyncTask().execute();
	}

	long curtime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (java.lang.System.currentTimeMillis() - curtime > 1000) {
				Toast.makeText(BrowseActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
				curtime = java.lang.System.currentTimeMillis();
				return true;
			} else {
				finish();
				return true;
			}

		}
		return super.onKeyDown(keyCode, event);
	}

	public class MyAsyncTask extends AsyncTask<Void, Void, Map<String, Integer>> {

		@Override
		protected void onPostExecute(Map<String, Integer> result) {
			tv_apk.setText("安装包(" + result.get("result_apk") + ")");
			tv_movie.setText("视频(" + result.get("result_movie") + ")");
			tv_music.setText("音乐(" + result.get("result_music") + ")");
			tv_photo.setText("照片(" + result.get("result_photo") + ")");
			tv_doc.setText("文档(" + result.get("result_doc") + ")");
			tv_zip.setText("压缩包(" + result.get("result_zip") + ")");
			super.onPostExecute(result);
		}

		@Override
		protected Map<String, Integer> doInBackground(Void... params) {
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put("result_apk", Utils.getCount("result_apk", BrowseActivity.this));
			map.put("result_music", Utils.getCount("result_music", BrowseActivity.this));
			map.put("result_photo", Utils.getCount("result_photo", BrowseActivity.this));
			map.put("result_movie", Utils.getCount("result_movie", BrowseActivity.this));
			map.put("result_zip", Utils.getCount("result_zip", BrowseActivity.this));
			map.put("result_doc", Utils.getCount("result_doc", BrowseActivity.this));
			return map;
		}

	}

	public class ProgressAsyncTask extends AsyncTask<Void, Void, Map<String, String[]>> {
		@Override
		protected void onPostExecute(Map<String, String[]> result) {
			String[] phoneSpace = result.get("phoneSpace");
			String[] sdSpace = result.get("sdSpace");

			tv_phoneTotal.setText(phoneSpace[0]);
			tv_phoneNotUse.setText(phoneSpace[1]);
			tv_sdTotal.setText(sdSpace[0]);
			tv_sdNotUse.setText(sdSpace[1]);

			pb_sd.setProgress(100 - Integer
					.parseInt(sdSpace[2].substring(0, (sdSpace[2].indexOf(".") == -1) ? 2 : sdSpace[2].indexOf("."))));
			pb_phone.setProgress(100 - Integer.parseInt(
					phoneSpace[2].substring(0, (phoneSpace[2].indexOf(".") == -1) ? 2 : phoneSpace[2].indexOf("."))));

			super.onPostExecute(result);
		}

		@Override
		protected Map<String, String[]> doInBackground(Void... params) {
			Map<String, String[]> map = new HashMap<String, String[]>();
			String[] phoneSpace = Utils.getPhoneSpace(BrowseActivity.this);
			String[] sdSpace = Utils.getSdSpace(BrowseActivity.this);
			map.put("phoneSpace", phoneSpace);
			map.put("sdSpace", sdSpace);
			return map;
		}

	}
}
