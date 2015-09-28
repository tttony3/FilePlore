package com.changhong.fileplore.data;

public class DownData {
	String name;
	long totalPart;
	long curPart;
	String uri;
	boolean done = false;
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
}
