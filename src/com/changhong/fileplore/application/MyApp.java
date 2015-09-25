package com.changhong.fileplore.application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.chobit.corestorage.CoreApp;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.view.View;

public class MyApp extends CoreApp {
	static String download_Path = Environment.getExternalStorageDirectory().getAbsolutePath();
	static public String storagepath = download_Path + "/FilePlore/cache/";
	ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);
	File[] folderfiles;
	static public Set<String> fileSet;

	public Set<String> getFileSet() {
		return fileSet;
	}

	Context context;
	Context mainContext;
	String ip;
	int port;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Context getMainContext() {
		return mainContext;
	}

	public void setMainContext(Context mainContext) {
		this.mainContext = mainContext;
	}

	ArrayList<File> fileList = new ArrayList<File>();

	/**
	 * 获取复制剪贴的文件列表
	 * 
	 * @return ArrayList
	 */
	public ArrayList<File> getFileList() {
		return fileList;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public void setFileList(ArrayList<File> fileList) {
		this.fileList = fileList;
	}

	/**
	 * 清空复制剪贴的文件列表
	 */
	public void clearFileList() {
		fileList.clear();
		;
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}

	public void onCreate() {
		super.onCreate();
		File folder = new File(storagepath);
		if (!folder.exists())
			folder.mkdir();

		folderfiles = folder.listFiles();
		//fileSet = new HashSet<String>();
		fileSet =Collections.synchronizedSet(new HashSet<String>());
		for (int i = 0; i < folderfiles.length; i++) {
			fileSet.add(folderfiles[i].getName());
		}
		ImageLoader imageLoader = ImageLoader.getInstance();
		imageLoader.setDefaultLoadingListener(new ImageLoadingListener() {

			@Override
			public void onLoadingStarted(String imageUri, View view) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
				if (loadedImage != null) {

					final String filename = imageUri.substring(imageUri.lastIndexOf("/") + 1, imageUri.length());
					fixedThreadPool.execute(new Runnable() {

						@Override
						public void run() {
							saveBitmap2file(loadedImage, "cache" + filename);
							fileSet.add(filename);
						}
					});

				}

			}

			@Override
			public void onLoadingCancelled(String imageUri, View view) {
				// TODO Auto-generated method stub

			}
		});

	}

	boolean saveBitmap2file(Bitmap bmp, String filename) {
		CompressFormat format = Bitmap.CompressFormat.JPEG;
		int quality = 20;
		OutputStream stream = null;
		try {
			stream = new FileOutputStream(storagepath + filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return bmp.compress(format, quality, stream);
	}

}
