package com.changhong.fileplore.application;

import java.io.File;
import java.util.ArrayList;

import com.chobit.corestorage.CoreApp;

public class MyApp extends CoreApp{
	ArrayList<File> fileList = new ArrayList<File>();

	public ArrayList<File> getFileList() {
		return fileList;
	}

	public void setFileList(ArrayList<File> fileList) {
		this.fileList = fileList;
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
