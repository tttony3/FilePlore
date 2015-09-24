package com.changhong.fileplore.thread;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ProgressBar;

public class SetMediaProgressBarThread extends Thread {
	static public final int UPDATE_BAR = 5;
	Handler handler;
	ProgressBar pb_media;
	int time;

	public SetMediaProgressBarThread(Handler handler, ProgressBar pb_media, int time) {
		this.handler = handler;
		this.pb_media = pb_media;
		this.time = time;
	}

	private Boolean isvalid = true;

	public Boolean getIsvalid() {
		return isvalid;
	}

	public void setIsvalid(Boolean isvalid) {
		this.isvalid = isvalid;
	}

	@Override
	public void run() {

		super.run();
		while (pb_media.getProgress() <= 10010 && isvalid) {
			try {
				Thread.sleep(1000);
				Message msg = new Message();
				Bundle bundle = new Bundle();
				bundle.putInt("key", UPDATE_BAR);
				bundle.putInt("value", pb_media.getProgress() + 10000 / time);
				msg.setData(bundle);
				handler.sendMessage(msg);
			} catch (InterruptedException e) {
				//

				e.printStackTrace();
			}

		}
	}

}