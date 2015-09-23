package com.changhong.fileplore.application;

import java.io.File;
import java.util.ArrayList;

import com.chobit.corestorage.CoreApp;

import android.content.Context;

public class MyApp extends CoreApp {
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
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
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

}
