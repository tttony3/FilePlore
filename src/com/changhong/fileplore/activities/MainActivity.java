package com.changhong.fileplore.activities;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import com.chobit.corestorage.ConnectedService;
import com.chobit.corestorage.CoreApp;
import com.chobit.corestorage.CoreHttpServerCB;
import com.chobit.corestorage.CoreService.CoreServiceBinder;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.Tencent;
import com.changhong.fileplore.R;
import com.changhong.fileplore.adapter.MainViewPagerAdapter;
import com.changhong.fileplore.application.MyApp;
import com.changhong.fileplore.fragment.DetailDialogFragment;
import com.changhong.fileplore.utils.BaseUiListener;
import com.changhong.fileplore.utils.Utils;

import android.os.Binder;
import android.os.Bundle;

import android.app.ActionBar;

import android.app.AlertDialog;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;

import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class MainActivity extends SlidingFragmentActivity
		implements android.view.View.OnClickListener, OnLongClickListener {
	DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.file_icon_photo)
			.showImageForEmptyUri(R.drawable.file_icon_photo).showImageOnFail(R.drawable.file_icon_photo)
			.cacheInMemory(true).cacheOnDisk(true).bitmapConfig(Bitmap.Config.RGB_565)
			.displayer(new RoundedBitmapDisplayer(10)) // 设置图片的解码类型
			.build();
	Tencent mTencent ;
	SharedPreferences sharedPreferences;
	View view0;
	View view1;
	ImageLoader imageLoader = ImageLoader.getInstance();
	ImageView iv_apk;
	ImageView iv_movie;
	ImageView iv_music;
	ImageView iv_photo;
	ImageView iv_txt;
	ImageView iv_zip;
	ImageView iv_qq;
	ImageView iv_wechat;
	ImageView iv_app;
	MyApp myapp;
	static final private int DOC = 1;
	static final private int MUSIC = 2;
	static final private int PHOTO = 3;
	static final private int ZIP = 4;
	static final private int MOVIE = 5;
	static final private int UNKNOW = 6;
	final ArrayList<View> list = new ArrayList<View>();
	Context context = null;
	LocalActivityManager manager = null;
	ViewPager pager = null;
	TabHost tabHost = null;
	TextView t1, t2;
	TableLayout tl_brwloc;
	RelativeLayout rl_brwnet;
	RelativeLayout rl_showdown;
	MainViewPagerAdapter myPagerAdapter;
	private int offset = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int bmpW;// 动画图片宽度
	private ImageView cursor1;// 动画图片
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		super.onCreate(savedInstanceState);
		mTencent = Tencent.createInstance("1104922716", this.getApplicationContext());
		setContentView(R.layout.activity_main);
		myapp = (MyApp) getApplication();
		myapp.setContext(this);
		myapp.setMainContext(this);
		context = MainActivity.this;
		manager = new LocalActivityManager(this, true);
		manager.dispatchCreate(savedInstanceState);
		sharedPreferences =getSharedPreferences("set", Context.MODE_PRIVATE);
		MyApp app = (MyApp) this.getApplicationContext();
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
		 view0 = list.get(0);
		 view1 = list.get(1);
		tl_brwloc = (TableLayout) view0.findViewById(R.id.browse_tab_2);
		rl_brwnet = (RelativeLayout) view0.findViewById(R.id.browse_rl_net);
		rl_showdown = (RelativeLayout) view0.findViewById(R.id.browse_rl_downlist);
		tl_brwloc.setOnClickListener(new MyOnClickListener(1));
		rl_brwnet.setOnClickListener(this);
		rl_showdown.setOnClickListener(this);
		iv_apk = (ImageView) view0.findViewById(R.id.img_apk);
		iv_movie = (ImageView) view0.findViewById(R.id.img_movie);
		iv_music = (ImageView) view0.findViewById(R.id.img_music);
		iv_photo = (ImageView) view0.findViewById(R.id.img_photo);
		iv_txt = (ImageView) view0.findViewById(R.id.img_txt);
		iv_zip = (ImageView) view0.findViewById(R.id.img_zip);
		iv_app = (ImageView) view0.findViewById(R.id.img_app);
		iv_qq = (ImageView) view0.findViewById(R.id.img_qq);
		iv_wechat = (ImageView) view0.findViewById(R.id.img_wechat);
		myPagerAdapter = new MainViewPagerAdapter(list);
		pager.setAdapter(myPagerAdapter);
		pager.setCurrentItem(0);
		pager.setOnPageChangeListener(new MyOnPageChangeListener());

		iv_apk.setOnClickListener(this);
		iv_apk.setOnLongClickListener(this);
		iv_movie.setOnClickListener(this);
		iv_movie.setOnLongClickListener(this);
		iv_music.setOnClickListener(this);
		iv_music.setOnLongClickListener(this);
		iv_photo.setOnClickListener(this);
		iv_photo.setOnLongClickListener(this);
		iv_txt.setOnClickListener(this);
		iv_txt.setOnLongClickListener(this);
		iv_zip.setOnClickListener(this);
		iv_zip.setOnLongClickListener(this);
		iv_qq.setOnClickListener(this);
		iv_wechat.setOnClickListener(this);
		iv_app.setOnClickListener(this);
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
		if (id == R.id.action_set) {
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, SetActivity.class);
			startActivity(intent);
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
		} else if (id == R.id.action_scanner) {
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, CaptureActivity.class);
			startActivity(intent);
		}
		else if (id == R.id.action_sharefile) {
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, ShowSharefileActivity.class);
			startActivity(intent);
		}
		else if (id == R.id.action_share){
			ArrayList<File> detailList = new ArrayList<File>();
			pager.setCurrentItem(1);
			if (!((PloreActivity)view1.getContext()).mFileAdpter.isShow_cb()) {
				((PloreActivity)view1.getContext()).mFileAdpter.setShow_cb(true);
				((PloreActivity)view1.getContext()).mFileAdpter.notifyDataSetChanged();
			} else {
				Boolean[] mlist = ((PloreActivity)view1.getContext()).mFileAdpter.getCheckBox_List();
				for (int i = 0; i < mlist.length; i++) {
					if (mlist[i]) {
						File file = (File) ((PloreActivity)view1.getContext()).mFileAdpter.getItem(i);
						if (!file.isDirectory()) {
							detailList.add(file);
						} else {
							//Toast.makeText(PloreActivity.this, "文件夹暂不支持推送", Toast.LENGTH_SHORT).show();
						}
					}
				}
				if (detailList.size() == 1) {
					File detailfile =detailList.get(0);					
					onClickShare(detailfile);

				}

			}
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
			// Log.i("tl", ip + "port" + port);

		}

		@Override
		public String onGetRealFullPath(String arg0) {
			Log.e("onGetRealFullPath", arg0);
			return null;
		}

		@Override
		public void recivePushResources(List<String> pushlist) {
			final MyApp myapp = (MyApp) getApplication();
			final List<String> list = pushlist;
			AlertDialog.Builder dialog = new AlertDialog.Builder(myapp.getContext());

			AlertDialog alert = dialog.setTitle("有推送文件").setMessage(pushlist.remove(0).substring(8))
					.setNegativeButton("查看", new OnClickListener() {

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
		((PloreActivity)list.get(1).getContext()).hide = sharedPreferences.getBoolean("hide", false);
		 myapp = (MyApp) getApplication();
		myapp.setContext(this);
		((BrowseActivity)list.get(0).getContext()).callupdate();
		super.onResume();
	}



	@Override
	protected void onStart() {
		
		((PloreActivity)list.get(1).getContext()).hide = sharedPreferences.getBoolean("hide", false);
		 myapp = (MyApp) getApplication();
			myapp.setContext(this);
			((BrowseActivity)list.get(0).getContext()).callupdate();
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		
		stopService(new Intent("com.chobit.corestorage.CoreService"));	
		stopService(new Intent("com.changhong.fileplore.service.DownLoadService"));
		super.onDestroy();
		System.exit(0);
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.img_apk:

			intent.setClass(MainActivity.this, ClassifyListActivity.class);
			intent.putExtra("key", R.id.img_apk);
			startActivity(intent);

			break;
		case R.id.img_movie:

			intent.setClass(MainActivity.this, ClassifyGridActivity.class);
			intent.putExtra("key", R.id.img_movie);
			startActivity(intent);
			break;
		case R.id.img_music:

			intent.setClass(MainActivity.this, ClassifyListActivity.class);
			intent.putExtra("key", R.id.img_music);
			startActivity(intent);
			break;
		case R.id.img_photo:

			intent.setClass(MainActivity.this, ClassifyGridActivity.class);
			intent.putExtra("key", R.id.img_photo);
			startActivity(intent);
			break;
		case R.id.img_txt:

			intent.setClass(MainActivity.this, ClassifyListActivity.class);
			intent.putExtra("key", R.id.img_txt);
			startActivity(intent);
			break;
		case R.id.img_zip:

			intent.setClass(MainActivity.this, ClassifyListActivity.class);
			intent.putExtra("key", R.id.img_zip);
			startActivity(intent);
			break;
		case R.id.browse_rl_net:
			intent.setClass(MainActivity.this, ShowNetDevActivity.class);
			startActivity(intent);
			break;
		case R.id.browse_rl_downlist:
			intent.setClass(MainActivity.this, ShowDownFileActivity.class);
			startActivity(intent);
			break;
			
		case R.id.img_qq:

			intent.setClass(MainActivity.this, QQListActivity.class);
			intent.putExtra("key", R.id.img_qq);
			startActivity(intent);

			break;
		case R.id.img_wechat:

			intent.setClass(MainActivity.this, QQListActivity.class);
			intent.putExtra("key", R.id.img_wechat);
			startActivity(intent);

			break;
		case R.id.img_app:

			intent.setClass(MainActivity.this, QQListActivity.class);
			intent.putExtra("key", R.id.img_app);
			startActivity(intent);

			break;
		default:
			break;
		}

	}

	@Override
	public boolean onLongClick(View v) {
		switch (v.getId()) {
		case R.id.img_apk:

			break;
		case R.id.img_movie:
			onLongClick("/Movies","/DCIM/Camera", "video/*");
			break;
		case R.id.img_music:
			onLongClick("/Music", null,"audio/*");
			break;
		case R.id.img_photo:
			onLongClick("/DCIM/Camera","/Pictures/Saved Pictures", "image/*");
			break;
		case R.id.img_txt:

			break;
		case R.id.img_zip:

			break;
		case R.id.browse_rl_net:

			break;
		case R.id.browse_rl_downlist:

			break;
		default:
			break;
		}
		return true;
	}

	private void onLongClick(String foldername1, String foldername2, String type) {
		ArrayList<File> filelist = new ArrayList<File>();
		if (null != foldername1) {
			File file1 = new File("/storage/sdcard1" + foldername1);
			File file2 = new File("/storage/sdcard0" + foldername1);
			if (file1.exists() && file1.canRead() && file1.isDirectory()) {
				File[] files = file1.listFiles();
				for (int i = 0; i < files.length; i++) {
					if (Utils.getMIMEType(files[i]).equals(type))
						filelist.add(files[i]);
				}
			}
			if (file2.exists() && file2.canRead() && file2.isDirectory()) {
				File[] files = file2.listFiles();
				for (int i = 0; i < files.length; i++)
					if (Utils.getMIMEType(files[i]).equals(type))
						filelist.add(files[i]);
			}

		}
		if (null != foldername2) {
			File file3 = new File("/storage/sdcard1" + foldername2);
			File file4 = new File("/storage/sdcard0" + foldername2);
			if (file3.exists() && file3.canRead() && file3.isDirectory()) {
				File[] files = file3.listFiles();
				for (int i = 0; i < files.length; i++)
					if (Utils.getMIMEType(files[i]).equals(type))
						filelist.add(files[i]);
			}
			if (file4.exists() && file4.canRead() && file4.isDirectory()) {
				File[] files = file4.listFiles();
				for (int i = 0; i < files.length; i++)
					if (Utils.getMIMEType(files[i]).equals(type))
						filelist.add(files[i]);
			}
		}

		final File[] files1 = filelist.toArray(new File[filelist.size()]);
		for (int i = 0; i < 4 && i < files1.length - 1; ++i) {
			for (int j = i + 1; j < files1.length; ++j) {
				if (files1[i].lastModified() < files1[j].lastModified()) {
					File tmp = files1[j];
					files1[j] = files1[i];
					files1[i] = tmp;
				}
			}
		}

		LayoutInflater inflater = LayoutInflater.from(context);
		View layout = inflater.inflate(R.layout.dialog_filepreview, null);
		TableRow tr_1 = (TableRow) layout.findViewById(R.id.tr_filepreview_1);
		TableRow tr_2 = (TableRow) layout.findViewById(R.id.tr_filepreview_2);
		TableRow tr_3 = (TableRow) layout.findViewById(R.id.tr_filepreview_3);
		TableRow tr_4 = (TableRow) layout.findViewById(R.id.tr_filepreview_4);
		TextView tv_1 = (TextView) layout.findViewById(R.id.tv_filepreview_1);
		TextView tv_2 = (TextView) layout.findViewById(R.id.tv_filepreview_2);
		TextView tv_3 = (TextView) layout.findViewById(R.id.tv_filepreview_3);
		TextView tv_4 = (TextView) layout.findViewById(R.id.tv_filepreview_4);
		ImageView iv_1 = (ImageView) layout.findViewById(R.id.iv_filepreview_1);
		ImageView iv_2 = (ImageView) layout.findViewById(R.id.iv_filepreview_2);
		ImageView iv_3 = (ImageView) layout.findViewById(R.id.iv_filepreview_3);
		ImageView iv_4 = (ImageView) layout.findViewById(R.id.iv_filepreview_4);
		AlertDialog.Builder builder = new AlertDialog.Builder(context).setView(layout);
		if (files1.length > 1 && files1[0] != null) {
			tv_1.setText(files1[0].getName());
			setImage(iv_1, files1[0]);
			tr_1.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					context.startActivity(Utils.openFile(files1[0]));

				}
			});
		
		}
		if (files1.length > 2 && files1[1] != null) {
			tv_2.setText(files1[1].getName());
			setImage(iv_2, files1[1]);
		
			tr_2.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					context.startActivity(Utils.openFile(files1[1]));

				}
			});
		}
		if (files1.length > 3 && files1[2] != null) {
			tv_3.setText(files1[2].getName());
			setImage(iv_3, files1[2]);
		
			tr_3.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					context.startActivity(Utils.openFile(files1[2]));

				}
			});
		}
		if (files1.length > 4 && files1[3] != null) {
			tv_4.setText(files1[3].getName());
			setImage(iv_4, files1[3]);
		
			tr_4.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					context.startActivity(Utils.openFile(files1[3]));

				}
			});
		}

		AlertDialog dia = builder.create();
		dia.show();
		float scale = context.getResources().getDisplayMetrics().density;
		dia.getWindow().setLayout((int) (250 * scale + 0.5f), -2);

	}

	private void setImage(ImageView iv_1, File file) {
		switch (getMIMEType(file.getName())) {
		case MOVIE:
			final String path1 = file.getPath();
			imageLoader.displayImage("file://" + path1, iv_1, options);
			break;
		case MUSIC:
			iv_1.setImageResource(R.drawable.file_icon_music);
			break;
		case PHOTO:
			final String path = file.getPath();
			final String name = file.getName();
			imageLoader.displayImage("file://" + path, iv_1, options);
			break;
		case DOC:
			iv_1.setImageResource(R.drawable.file_icon_txt);
			break;
		case UNKNOW:
			iv_1.setImageResource(R.drawable.file_icon_unknown);
			break;
		case ZIP:
			iv_1.setImageResource(R.drawable.file_icon_zip);
			break;

		default:
			break;
		}
	}

	private int getMIMEType(String name) {

		String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
		if (end.equals("m4a") || end.equals("mp3") || end.equals("wav")) {
			return MUSIC;
		} else if (end.equals("mp4") || end.equals("3gp")) {
			return MOVIE;
		} else if (end.equals("jpg") || end.equals("png") || end.equals("jpeg") || end.equals("bmp")
				|| end.equals("gif")) {
			return PHOTO;
		} else if (end.equals("zip") || end.equals("rar")) {
			return ZIP;
		} else if (end.equals("doc") || end.equals("docx") || end.equals("txt")) {
			return DOC;
		}
		return UNKNOW;

	}
	private void onClickShare(File file) {
	    Bundle params = new Bundle();
	    params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL,file.getPath());
		switch (getMIMEType(file.getName())) {
		case PHOTO:
			params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
			break;
		default:
			Toast.makeText(this, "只支持图片分享", Toast.LENGTH_SHORT).show();
			return;
		}
	    
	    params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
	    mTencent.shareToQQ(MainActivity.this, params, new BaseUiListener());
	}
}