package com.changhong.fileplore.utils;

import android.app.ProgressDialog;
import android.content.Context;

public class MyProgressDialog {
	ProgressDialog dialog;
	public MyProgressDialog(Context context)
	{
		dialog = new ProgressDialog(context);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setTitle("查找中");
		dialog.setMessage("请稍等...");
		dialog.setIndeterminate(false);
		dialog.setCancelable(true);
	}
	public ProgressDialog getDialog(){
		return dialog;
	}
}
