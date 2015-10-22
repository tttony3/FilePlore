package com.changhong.fileplore.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import com.tencent.open.utils.HttpUtils.HttpStatusException;
import com.tencent.open.utils.HttpUtils.NetworkUnavailableException;
import com.tencent.tauth.IRequestListener;

public class BaseApiListener implements IRequestListener {
	
	//////////////////////////////////////
	@Override
	public void onComplete(JSONObject arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onHttpStatusException(HttpStatusException arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onIOException(IOException arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onJSONException(JSONException arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onMalformedURLException(MalformedURLException arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onNetworkUnavailableException(NetworkUnavailableException arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onSocketTimeoutException(SocketTimeoutException arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onUnknowException(Exception arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectTimeoutException(ConnectTimeoutException arg0) {
		// TODO Auto-generated method stub
		
	}
	}
