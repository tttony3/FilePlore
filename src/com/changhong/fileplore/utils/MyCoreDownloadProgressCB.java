package com.changhong.fileplore.utils;

import com.changhong.alljoyn.simpleservice.FC_GetShareFile;
import com.changhong.fileplore.activities.ShowNetFileActivity;
import com.chobit.corestorage.CoreDownloadProgressCB;
import com.example.libevent2.UpdateDownloadPress;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class MyCoreDownloadProgressCB implements CoreDownloadProgressCB {
	static public final int UPDATE_DOWNLOAD = 10;
	static public final int DISMISS_DOWNLOAD= 12;
	Handler handler;
	Context context;
	public MyCoreDownloadProgressCB(Handler handler,Context context) {
		this.handler = handler;
		this.context = context;
	}

	@Override
	public void onDownloadOK(String fileuri) {

		dismissDownDialog();

		String download_Path = Environment.getExternalStorageDirectory().getAbsolutePath();
		String appname = FC_GetShareFile.getApplicationName(context);
		Toast.makeText(context, "下载成功,保存在" + download_Path + "/" + appname + "/download/ 目录下",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDowloadProgress(UpdateDownloadPress press) {
		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putInt("key", UPDATE_DOWNLOAD);
		bundle.putLong("part", press.part);
		bundle.putLong("total", press.total);
		msg.setData(bundle);
		handler.sendMessage(msg);

	}

	@Override
	public void onDowloaStop(String fileuri) {
		Log.e("Progress", "stop" + fileuri);
		Toast.makeText(context, "下载停止", Toast.LENGTH_SHORT).show();
		dismissDownDialog();

	}

	private void dismissDownDialog() {
		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putInt("key", DISMISS_DOWNLOAD);
		msg.setData(bundle);
		handler.sendMessage(msg);

	}

	@Override
	public void onDowloaFailt(String fileuri) {
		Log.e("Progress", "failt" + fileuri);
		Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show();
		dismissDownDialog();

	}

	@Override
	public void onDowloaCancel(String fileuri) {
		Toast.makeText(context, "下载取消", Toast.LENGTH_SHORT).show();
		dismissDownDialog();

	}

	@Override
	public void onConnectError(String fileuri) {
		Log.e("Progress", "error" + fileuri);
		Toast.makeText(context, "下载错误", Toast.LENGTH_SHORT).show();
		dismissDownDialog();

	}
}
