package com.changhong.fileplore.activities;

import com.changhong.fileplore.R;
import com.changhong.fileplore.application.MyApp;
import com.changhong.fileplore.base.BaseActivity;

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

public class SetActivity extends BaseActivity implements OnCheckedChangeListener {
	ToggleButton tb_setnight;
	ToggleButton tb_setshare;
	ToggleButton tb_sethide;
	SharedPreferences sharedPreferences ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		setContentView(R.layout.activity_set);
		MyApp myapp = (MyApp) getApplication();
		myapp.setContext(this);
		findView();
		initView();

	}

	private void initView() {
		sharedPreferences = getSharedPreferences("set", Context.MODE_PRIVATE); //私有数据
		boolean night=sharedPreferences.getBoolean("night",false);
		boolean hide=sharedPreferences.getBoolean("hide",false);
		boolean share=sharedPreferences.getBoolean("share",true);
		tb_setnight.setChecked(night);
		tb_sethide.setChecked(hide);
		tb_setshare.setChecked(share);
		
		tb_setnight.setOnCheckedChangeListener(this);
		tb_sethide.setOnCheckedChangeListener(this);
		tb_setshare.setOnCheckedChangeListener(this);
		 sharedPreferences = getSharedPreferences("set", Context.MODE_PRIVATE); //私有数据
	}

	@Override
	protected void findView() {

		tb_setnight = findView(R.id.tb_setnight);
		tb_sethide = findView(R.id.tb_showhide);
		tb_setshare = findView(R.id.tb_setshare);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	
		Editor editor = sharedPreferences.edit();//获取编辑器
		switch (buttonView.getId()) {		
		case R.id.tb_setnight:		
			editor.putBoolean("night",isChecked);
			editor.commit();//提交修改
			break;
		case R.id.tb_showhide:
			editor.putBoolean("hide",isChecked);
			editor.commit();//提交修改
			break;
		case R.id.tb_setshare:
			editor.putBoolean("share",isChecked);
			editor.commit();//提交修改
			break;

		default:
			break;
		}
		buttonView.setChecked(isChecked);
	}

}
