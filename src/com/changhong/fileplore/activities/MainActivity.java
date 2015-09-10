package com.changhong.fileplore.activities;

import java.util.ArrayList;
import java.util.List;

import com.changhong.alljoyn.simpleclient.ClientBusHandler;
import com.chobit.corestorage.ConnectedService;
import com.chobit.corestorage.CoreApp;
import com.chobit.corestorage.CoreHttpServerCB;
import com.chobit.corestorage.CoreService.CoreServiceBinder;
import com.example.fileplore.R;

import android.os.Binder;
import android.os.Bundle;
import android.os.Parcelable;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {
	Context context = null;
	LocalActivityManager manager = null;
	ViewPager pager = null;
	TabHost tabHost = null;
	TextView t1, t2;
	TableLayout tl_brwloc;
	RelativeLayout rl_brwnet;

	private int offset = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int bmpW;// 动画图片宽度
	private ImageView cursor1;// 动画图片

	@Override
	protected void onResume() {
		ClientBusHandler.List_DeviceInfo.clear();
		super.onResume();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		setContentView(R.layout.activity_main);

		//
		context = MainActivity.this;
		manager = new LocalActivityManager(this, true);
		manager.dispatchCreate(savedInstanceState);

		CoreApp app = (CoreApp) this.getApplicationContext();
		app.setConnectedService(new ConnectedService() {

			@Override
			public void onConnected(Binder b) {
				CoreServiceBinder binder = (CoreServiceBinder) b;
				binder.init();
				binder.setCoreHttpServerCBFunction(httpServerCB);	
				binder.StartHttpServer("/", context);
			}
		});

		InitImageView();
		initTextView();
		initPagerViewer();

	}

	/**
	 * 初始化标题
	 */
	private void initTextView() {
		t1 = (TextView) findViewById(R.id.text1);
		t2 = (TextView) findViewById(R.id.text2);

		t1.setOnClickListener(new MyOnClickListener(0));
		t2.setOnClickListener(new MyOnClickListener(1));

	}

	/**
	 * 初始化PageViewer
	 */
	private void initPagerViewer() {

		pager = (ViewPager) findViewById(R.id.viewpage);
		final ArrayList<View> list = new ArrayList<View>();
		Intent intent1 = new Intent(context, BrowseActivity.class);
		list.add(0, getView("A", intent1));
		Intent intent2 = new Intent(context, PloreActivity.class);
		list.add(1, getView("B", intent2));

		tl_brwloc = (TableLayout) list.get(0).findViewById(R.id.browse_tab_2);
		rl_brwnet = (RelativeLayout) list.get(0).findViewById(R.id.browse_rl_net);
		tl_brwloc.setOnClickListener(new MyOnClickListener(1));
		rl_brwnet.setOnClickListener(new MyOnClickListener(2));
		pager.setAdapter(new MyPagerAdapter(list));
		pager.setCurrentItem(0);
		pager.setOnPageChangeListener(new MyOnPageChangeListener());
	}

	/**
	 * 初始化动画
	 */
	private void InitImageView() {
		cursor1 = (ImageView) findViewById(R.id.cursor_1);
		bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.roller_1).getWidth();// 获取图片宽度
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;// 获取分辨率宽度
		offset = (screenW / 2 - bmpW) / 2;// 计算偏移量
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		cursor1.setImageMatrix(matrix);// 设置动画初始位置
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.plore, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_local) {
			pager.setCurrentItem(1);
		}
		if (id == R.id.action_samba) {
			LayoutInflater inflater = getLayoutInflater();
			final View layout = inflater.inflate(R.layout.samba_option, (ViewGroup) findViewById(R.id.samba_op));

			new AlertDialog.Builder(this).setTitle("samba设置").setView(layout)
					.setNegativeButton("确定", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							EditText ip = (EditText) layout.findViewById(R.id.ip);
							EditText user = (EditText) layout.findViewById(R.id.user);
							EditText password = (EditText) layout.findViewById(R.id.password);
							EditText dir = (EditText) layout.findViewById(R.id.dir);
							if (!ip.getText().toString().isEmpty() && !user.getText().toString().isEmpty()
									&& !password.getText().toString().isEmpty()) {
								Intent intent = new Intent();
								intent.putExtra("ip", ip.getText().toString());
								intent.putExtra("user", user.getText().toString());
								intent.putExtra("password", password.getText().toString());
								intent.putExtra("dir", dir.getText().toString());
								intent.setClass(MainActivity.this, SambaActivity.class);
								startActivity(intent);

							}

						}
					}).setPositiveButton("取消", null).show();

		}
		return super.onOptionsItemSelected(item);

	}

	/**
	 * 通过activity获取视图
	 * 
	 * @param id
	 * @param intent
	 * @return
	 */
	private View getView(String id, Intent intent) {
		return manager.startActivity(id, intent).getDecorView();
	}

	/**
	 * Pager适配器
	 */
	public class MyPagerAdapter extends PagerAdapter {
		List<View> list = new ArrayList<View>();

		public MyPagerAdapter(ArrayList<View> list) {
			this.list = list;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			ViewPager pViewPager = ((ViewPager) container);
			pViewPager.removeView(list.get(position));
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			ViewPager pViewPager = ((ViewPager) arg0);
			pViewPager.addView(list.get(arg1));
			return list.get(arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}
	}

	/**
	 * 页卡切换监听
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {

		int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量
		int two = -one;// 页卡1 -> 页卡3 偏移量

		@Override
		public void onPageSelected(int arg0) {
			Animation animation1 = null;
			Animation animation2 = null;
			switch (arg0) {
			case 0:
				t1.setTextColor(getResources().getColor(R.color.green));
				t2.setTextColor(getResources().getColor(R.color.black));
				if (currIndex == 1) {
					animation1 = new TranslateAnimation(one, 0, 0, 0);
					animation2 = new TranslateAnimation(two, 0, 0, 0);
				} else if (currIndex == 2) {
					animation1 = new TranslateAnimation(two, 0, 0, 0);
				}
				break;
			case 1:
				t2.setTextColor(getResources().getColor(R.color.green));
				t1.setTextColor(getResources().getColor(R.color.black));
				if (currIndex == 0) {
					animation1 = new TranslateAnimation(offset, one, 0, 0);
					animation2 = new TranslateAnimation(offset, two, 0, 0);
				} else if (currIndex == 2) {
					animation1 = new TranslateAnimation(two, one, 0, 0);
				}
				break;
			// case 2:
			// if (currIndex == 0) {
			// animation = new TranslateAnimation(offset, two, 0, 0);
			// } else if (currIndex == 1) {
			// animation = new TranslateAnimation(one, two, 0, 0);
			// }
			// break;
			}
			currIndex = arg0;
			animation1.setFillAfter(true);// True:图片停在动画结束位置
			animation1.setDuration(300);
			animation2.setFillAfter(true);// True:图片停在动画结束位置
			animation2.setDuration(300);
			cursor1.startAnimation(animation1);
			// cursor2.startAnimation(animation2);
			// t1.startAnimation(animation1);
			// t2.startAnimation(animation2);
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}
	}

	/**
	 * 头标点击监听
	 */
	public class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			if (index < 2)
				pager.setCurrentItem(index);
			else if (index == 2) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, NetActivity.class);
				startActivity(intent);

				// Toast.makeText(MainActivity.this, "net",
				// Toast.LENGTH_SHORT).show();
			}
		}
	};

	long curtime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (System.currentTimeMillis() - curtime > 1000) {
				Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
				curtime = System.currentTimeMillis();
				return true;
			}
			else finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	private CoreHttpServerCB httpServerCB = new CoreHttpServerCB() {

		

		@Override
		public void onTransportUpdata(String arg0, String arg1, long arg2, long arg3, long arg4) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onHttpServerStop() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onHttpServerStart(String ip, int port) {
			
			Log.i("tl", ip + "port" + port);

		}

		@Override
		public String onGetRealFullPath(String arg0) {
			return null;
		}

		@Override
		public void recivePushResources(List<String> resourceslist) {
			// TODO Auto-generated method stub
			
		}
	};
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		System.exit(0);
		//或者下面这种方式
		//android.os.Process.killProcess(android.os.Process.myPid()); 
	}
}