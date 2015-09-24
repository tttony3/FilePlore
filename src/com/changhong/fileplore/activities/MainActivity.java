package com.changhong.fileplore.activities;

import java.util.ArrayList;
import java.util.List;

import com.chobit.corestorage.ConnectedService;
import com.chobit.corestorage.CoreApp;
import com.chobit.corestorage.CoreHttpServerCB;
import com.chobit.corestorage.CoreService.CoreServiceBinder;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.changhong.fileplore.R;
import com.changhong.fileplore.adapter.MainViewPagerAdapter;
import com.changhong.fileplore.application.MyApp;

import android.os.Binder;
import android.os.Bundle;
import android.os.Parcelable;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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
import android.view.WindowManager;
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
public class MainActivity extends SlidingFragmentActivity {

	final ArrayList<View> list = new ArrayList<View>();
	Context context = null;
	LocalActivityManager manager = null;
	ViewPager pager = null;
	TabHost tabHost = null;
	TextView t1, t2;
	TableLayout tl_brwloc;
	RelativeLayout rl_brwnet;
	MainViewPagerAdapter myPagerAdapter;
	private int offset = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int bmpW;// 动画图片宽度
	private ImageView cursor1;// 动画图片

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		setContentView(R.layout.activity_main);
		MyApp myapp = (MyApp) getApplication();
		myapp.setContext(this);
		myapp.setMainContext(this);
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
		setSlidingMenu();
		InitImageView();
		initTextView();
		initPagerViewer();

	}

	private void setSlidingMenu() {
		if (findViewById(R.id.menu_frame) == null) {
			setBehindContentView(R.layout.menu_frame);
			getSlidingMenu().setSlidingEnabled(true);
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		} else {
			// add a dummy view
			View v = new View(this);
			setBehindContentView(v);
			getSlidingMenu().setSlidingEnabled(false);
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		}

		MenuFragment menuFragment = new MenuFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame, menuFragment).commit();

		SlidingMenu sm = getSlidingMenu();
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeEnabled(false);
		sm.setBehindScrollScale(0.2f);
		sm.setFadeDegree(0.2f);

		sm.setBackgroundImage(R.drawable.star_back);
		sm.setBehindCanvasTransformer(new SlidingMenu.CanvasTransformer() {
			@Override
			public void transformCanvas(Canvas canvas, float percentOpen) {
				float scale = (float) (percentOpen * 0.25 + 0.75);
				canvas.scale(scale, scale, -canvas.getWidth() / 2, canvas.getHeight() / 2);
			}
		});

		sm.setAboveCanvasTransformer(new SlidingMenu.CanvasTransformer() {
			@Override
			public void transformCanvas(Canvas canvas, float percentOpen) {
				float scale = (float) (1 - percentOpen * 0.15);
				canvas.scale(scale, scale, 0, canvas.getHeight() / 2);
			}
		});

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

		Intent intent1 = new Intent(context, BrowseActivity.class);
		list.add(0, getView("A", intent1));
		Intent intent2 = new Intent(context, PloreActivity.class);
		list.add(1, getView("B", intent2));

		tl_brwloc = (TableLayout) list.get(0).findViewById(R.id.browse_tab_2);
		rl_brwnet = (RelativeLayout) list.get(0).findViewById(R.id.browse_rl_net);
		tl_brwloc.setOnClickListener(new MyOnClickListener(1));
		rl_brwnet.setOnClickListener(new MyOnClickListener(2));
		myPagerAdapter = new MainViewPagerAdapter(list);
		pager.setAdapter(myPagerAdapter);
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
		} else if (id == R.id.action_samba) {
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

		} else if (id == R.id.action_net) {
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, ShowNetDevActivity.class);
			startActivity(intent);
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
				getSlidingMenu().removeIgnoredView(pager);
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
				getSlidingMenu().addIgnoredView(pager);
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
			if (index == 0) {
				pager.setCurrentItem(index);
				getSlidingMenu().removeIgnoredView(pager);
			}
			if (index == 1) {
				pager.setCurrentItem(index);

				getSlidingMenu().addIgnoredView(pager);
			} else if (index == 2) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, ShowNetDevActivity.class);
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
			if (currIndex == 0) {
				if (System.currentTimeMillis() - curtime > 1000) {
					Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
					curtime = System.currentTimeMillis();
					return true;
				} else
					finish();
			} else if (currIndex == 1) {

				TextView tv = (TextView) myPagerAdapter.getView(1).findViewById(R.id.path);
				String str = tv.getText().toString();
				if (str.lastIndexOf("/") == 0) {
					tv.callOnClick();
					pager.setCurrentItem(0);
					return true;
				} else {
					return tv.callOnClick();
				}

			}
		}
		return super.onKeyDown(keyCode, event);

	}

	private CoreHttpServerCB httpServerCB = new CoreHttpServerCB() {

		@Override
		public void onTransportUpdata(String arg0, String arg1, long arg2, long arg3, long arg4) {
			Log.e("onTransportUpdata",
					"agr0 " + arg0 + " arg1 " + arg1 + " arg2 " + arg2 + " arg3 " + arg3 + " arg4  " + arg4);

		}

		@Override
		public void onHttpServerStop() {

		}

		@Override
		public void onHttpServerStart(String ip, int port) {
			MyApp myapp = (MyApp) getApplication();
			myapp.setIp(ip);
			myapp.setPort(port);
		//	Log.i("tl", ip + "port" + port);

		}

		@Override
		public String onGetRealFullPath(String arg0) {
			Log.e("onGetRealFullPath", arg0);
			return null;
		}

		// @Override
		// public void recivePushResources(List<String> resourceslist) {
		// final MyApp myapp = (MyApp) getApplication();
		// final List<String> list = resourceslist;
		// AlertDialog.Builder dialog = new
		// AlertDialog.Builder(myapp.getContext());
		//
		// AlertDialog alert =
		// dialog.setTitle("有推送文件，是否接收").setNegativeButton("查看", new
		// OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// Intent intent = new Intent();
		// intent.setClass(myapp.getContext(), ShowPushFileActivity.class);
		// intent.putStringArrayListExtra("pushList", (ArrayList<String>) list);
		// startActivity(intent);
		// }
		// }).setPositiveButton("取消", null).create();
		// alert.show();
		//
		// }

		@Override
		public void recivePushResources(List<String> pushlist) {
			final MyApp myapp = (MyApp) getApplication();
			final List<String> list = pushlist;
			AlertDialog.Builder dialog = new AlertDialog.Builder(myapp.getContext());

			AlertDialog alert = dialog.setTitle("有推送文件，是否接收").setNegativeButton("查看", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent();
					intent.setClass(myapp.getContext(), ShowPushFileActivity.class);
					intent.putStringArrayListExtra("pushList", (ArrayList<String>) list);
					startActivity(intent);
				}
			}).setPositiveButton("取消", null).create();
			alert.show();

		}
	};

	@Override
	protected void onResume() {
		MyApp myapp = (MyApp) getApplication();
		myapp.setContext(this);
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.exit(0);

	}

}