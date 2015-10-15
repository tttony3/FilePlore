package com.changhong.fileplore.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class HPaConnector {

	private static final String SETUP_WIFIAP_METHOD = "setWifiApEnabled";
	 Context context = null;
	 WifiManager wifiManager = null;
	static HPaConnector hPaConnector = null;

	public static HPaConnector getInstance(Context context) {
		if (hPaConnector == null) {
			hPaConnector = new HPaConnector();
			hPaConnector.context = context.getApplicationContext();
			hPaConnector.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		}
		return hPaConnector;
	}
	public WifiConfiguration getWifiApConfiguration() {
		WifiConfiguration configuration = null;
	try {
		Method getWifiApMethod = wifiManager.getClass().getMethod("getWifiApConfiguration");
		
		try {
			configuration = (WifiConfiguration) getWifiApMethod.invoke(wifiManager);
			if(configuration!=null){
				//Log.e("wifiap", configuration.toString());
				return configuration;
			}
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	} catch (NoSuchMethodException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return configuration;
	
}
	public WifiManager getWifiManager(){
		return wifiManager;
	}
	public WifiConfiguration setupWifiAp(String name, String password) throws Exception {
		Method isEnable = wifiManager.getClass().getMethod("isWifiApEnabled");
		Boolean  obj =(Boolean) isEnable.invoke(wifiManager);
		if(obj.booleanValue()){
			return getWifiApConfiguration();
		//	Log.e("isEnable", "isEnable");
		//	return;
		}
		if (name == null || "".equals(name)) {
			throw new Exception("the name of the wifiap is cannot be null");
		}
		Method setupMethod = wifiManager.getClass().getMethod(SETUP_WIFIAP_METHOD, WifiConfiguration.class, boolean.class);
		Method setWifiApConfigurationMethod = wifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
		WifiConfiguration netConfig = new WifiConfiguration();
		// 设置wifi热点名称
		netConfig.SSID = name;

		netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
		netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
		netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
		netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

		if (password != null) {
			if (password.length() < 8) {
				throw new Exception("the length of wifi password must be 8 or longer");
			}
			// 设置wifi热点密码
			netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			netConfig.preSharedKey = password;
		}
		setWifiApConfigurationMethod.invoke(wifiManager, netConfig);
		setupMethod.invoke(wifiManager, netConfig, true);
		return getWifiApConfiguration();
	}

	
}
