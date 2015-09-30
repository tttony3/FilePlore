package com.changhong.fileplore.data;

import java.io.Serializable;

public class DownData implements Serializable{
	

	private static final long serialVersionUID = 2768585787632081804L;
	String name;
	long totalPart = 0;
	long curPart = 0;
	String uri;
	boolean done = false;
	boolean cancel = false;

	public boolean isCancel() {
		return cancel;
	}

	public void setCancel(boolean cancel) {
		this.cancel = cancel;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public String getName() {
		return name;
	}

	public DownData setName(String name) {
		this.name = name;
		return this;
	}

	public long getTotalPart() {
		return totalPart;
	}

	public DownData setTotalPart(long totalPart) {
		this.totalPart = totalPart;
		return this;
	}

	public long getCurPart() {
		return curPart;
	}

	public DownData setCurPart(long curPart) {
		this.curPart = curPart;
		return this;
	}

	public String getUri() {
		return uri;
	}

	public DownData setUri(String uri) {
		this.uri = uri;
		return this;
	}

	@Override
	public String toString() {
		return " uri " + uri + " total " + totalPart + " cur " + curPart + " name " + name;
	}

}
