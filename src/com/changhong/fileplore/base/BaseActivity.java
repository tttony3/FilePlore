package com.changhong.fileplore.base;

import android.app.Activity;
import android.view.View;

public abstract class BaseActivity   extends Activity {  
	/**
	 * 加载布局中的控件
	 */	
	protected abstract void findView();


	@SuppressWarnings("unchecked")
	public <T extends View> T findView(int id) {
	        return (T) findViewById(id);
	   
	}
	
	
}
