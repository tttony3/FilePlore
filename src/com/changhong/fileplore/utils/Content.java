package com.changhong.fileplore.utils;

import java.io.Serializable;

import android.graphics.Bitmap;

public class Content implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String dir;
	String title;
	String time;
	Bitmap img;
	String singer;
	int id;

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getSinger() {
		return singer;
	}

	public void setSinger(String singer) {
		this.singer = singer;
	}

	public Content(String dir, String title, String singer, String time, Bitmap img) {
		this.dir = dir;
		this.title = title;
		this.singer = singer;
		this.time = time;
		this.img = img;
	}

	public Content(String dir, String title, String singer, Bitmap img) {
		this.dir = dir;
		this.title = title;
		this.singer = singer;
		this.img = img;
	}

	public Content(String dir, String title, Bitmap img) {
		this.dir = dir;
		this.title = title;
		this.img = img;
	}

	public Content(String dir, String title, Bitmap img, int origId) {
		this.dir = dir;
		this.title = title;
		this.img = img;
		this.id = origId;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Bitmap getImg() {
		return img;
	}

	public void setImg(Bitmap img) {
		this.img = img;
	}
}
