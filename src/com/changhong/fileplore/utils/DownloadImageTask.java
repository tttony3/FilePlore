package com.changhong.fileplore.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import android.widget.ImageView;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	ImageView im;
	int opInt;
	public DownloadImageTask(ImageView im,int op) {
		this.im = im;
		this.opInt=op;
	}

	protected Bitmap doInBackground(String... urls) {

		return loadImageFromNetwork(urls[0]);
	}

	private Bitmap loadImageFromNetwork(String string) {
		Bitmap bitmap = null;
		URL url;
		try {
			url = new URL(string);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET"); // 设置请求方法为GET
			conn.setReadTimeout(5 * 1000); // 设置请求过时时间为5秒
			InputStream inputStream = conn.getInputStream(); // 通过输入流获得图片数据
			byte[] data = StreamTool.readInputStream(inputStream);
			Options op = new Options();
			op.inSampleSize = opInt;
			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, op);
		} catch (MalformedURLException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

		return bitmap;
	}

	protected void onPostExecute(Bitmap result) {
		im.setImageBitmap(result);
	}
}
