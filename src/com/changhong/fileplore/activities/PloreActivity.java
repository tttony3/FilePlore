package com.changhong.fileplore.activities;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.changhong.fileplore.adapter.PloreListAdapter;
import com.changhong.fileplore.adapter.PloreListAdapter.ImgOnClick;
import com.changhong.fileplore.application.MyApp;
import com.changhong.fileplore.base.BaseActivity;
import com.changhong.fileplore.data.PloreData;
import com.changhong.fileplore.fragment.DetailDialogFragment;
import com.changhong.fileplore.implement.PloreInterface;
import com.changhong.fileplore.view.RefreshListView;
import com.chobit.corestorage.ConnectedService;
import com.chobit.corestorage.CoreApp;
import com.chobit.corestorage.CoreHttpServerCB;
import com.chobit.corestorage.CoreService.CoreServiceBinder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.tencent.tauth.Tencent;
import com.changhong.fileplore.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.changhong.fileplore.utils.*;

public class PloreActivity extends BaseActivity implements RefreshListView.IOnRefreshListener, View.OnClickListener,
		PloreInterface, OnItemClickListener, OnItemLongClickListener, OnMenuItemClickListener ,ImgOnClick{
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	protected static final String STATE_PAUSE_ON_SCROLL = "STATE_PAUSE_ON_SCROLL";
	protected static final String STATE_PAUSE_ON_FLING = "STATE_PAUSE_ON_FLING";
	protected boolean pauseOnScroll = false;
	protected boolean pauseOnFling = true;
	private ArrayList<File> fileList;
	private RefreshDataAsynTask mRefreshAsynTask;
	private RefreshListView mListView;
	private TextView mPathView;
	private ImageView iv_back;
	private TextView mItemCount;
	private Button btn_newfile;
	private Button btn_paste;
	private Button btn_sort;
	private Button btn_seach;
	private Button btn_selectall;
	private Button btn_copy;
	private Button btn_cut;
	private Button btn_more;
	public PloreListAdapter mFileAdpter;
	private LinearLayout ll_btn;
	private LinearLayout ll_btn_default;
	public AlertDialog.Builder builder;
	boolean isCopy = false;
	private View layout_qr;
	private Builder builder_qr;
	private AlertDialog alertDialog_qr;
	private ImageView iv_qr;
	private SharedPreferences sharedPreferences;
	public boolean hide;
	private int sorttype = PloreData.NAME;

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		pauseOnScroll = savedInstanceState.getBoolean(STATE_PAUSE_ON_SCROLL, false);
		pauseOnFling = savedInstanceState.getBoolean(STATE_PAUSE_ON_FLING, true);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Tencent.createInstance("1104922716", this.getApplicationContext());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plore);
		MyApp myapp = (MyApp) getApplication();
		myapp.setContext(myapp.getMainContext());
		fileList = myapp.getFileList();
		findView();
		initView();

	}

	@Override
	protected void findView() {
		mListView = findView(R.id.file_list);
		mPathView = findView(R.id.path);
		mItemCount = findView(R.id.item_count);
		iv_back = findView(R.id.iv_back);
		btn_selectall = findView(R.id.plore_btn_selectall);
		btn_copy = findView(R.id.plore_btn_cut);
		btn_cut = findView(R.id.plore_btn_copy);
		btn_more = findView(R.id.plore_btn_more);
		btn_newfile = findView(R.id.plore_btn_newfile);
		btn_paste = findView(R.id.plore_btn_paste);
		btn_sort = findView(R.id.plore_btn_sort);
		btn_seach = findView(R.id.plore_btn_seach);
		ll_btn = findView(R.id.ll_btn);
		ll_btn_default = findView(R.id.ll_btn_default);
		layout_qr = this.getLayoutInflater().inflate(R.layout.dialog_qr, null);
		builder_qr = new AlertDialog.Builder(this).setView(layout_qr);
		alertDialog_qr = builder_qr.create();
		iv_qr = (ImageView) layout_qr.findViewById(R.id.iv_qr);
	}

	private void initView() {
		sharedPreferences = getSharedPreferences("set", Context.MODE_PRIVATE); // 私有数据
		hide = sharedPreferences.getBoolean("hide", false);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		mListView.setOnRefreshListener(this);
		btn_newfile.setOnClickListener(this);
		btn_paste.setOnClickListener(this);
		btn_sort.setOnClickListener(this);
		btn_seach.setOnClickListener(this);
		btn_selectall.setOnClickListener(this);
		btn_copy.setOnClickListener(this);
		btn_cut.setOnClickListener(this);
		btn_more.setOnClickListener(this);
		iv_back.setOnClickListener(this);
		mPathView.setOnClickListener(this);
		
		ImageView img =new ImageView(this);
		img.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, Utils.dpTopx(40, this)));
		mListView.addFooterView(img);
		File folder = new File("/storage");
		loadData(folder,sorttype);
	}

	@Override
	protected void onStart() {
		Log.e("hide", "hide");
		hide = sharedPreferences.getBoolean("hide", false);
		super.onStart();
	}

	@Override
	public void loadData(File folder,int sorttype) {
		ll_btn.setVisibility(View.GONE);
		ll_btn_default.setVisibility(View.VISIBLE);
		boolean isRoot = (folder.getParent() == null);
		if (folder.canRead()) {
			String path = folder.getPath();
			mPathView.setText(path);
			PloreData mPloreData = new PloreData(folder, hide,sorttype);
			List<File> files = mPloreData.getfiles();
			mItemCount.setText(files.size() + "项");
			mFileAdpter = new PloreListAdapter(this, files, isRoot, imageLoader);
			mListView.setAdapter(mFileAdpter);
		}

	}

	/**
	 * 底部button监听
	 */
	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.plore_btn_newfile:
			fileList.clear();
			final EditText ed = new EditText(PloreActivity.this);
			builder = new AlertDialog.Builder(PloreActivity.this);
			builder.setTitle("输入文件夹名称").setView(ed).setNegativeButton("确定", new DialogInterface.OnClickListener() {

				

				@Override
				public void onClick(DialogInterface dialog, int which) {
					File file = new File(mPathView.getText().toString() + "/" + ed.getText());
					if (file.exists())
						Toast.makeText(PloreActivity.this, "文件夹已存在", Toast.LENGTH_SHORT).show();
					else {
						if (file.mkdir())
							Toast.makeText(PloreActivity.this, "文件夹创建成功", Toast.LENGTH_SHORT).show();
						else {

						}
						File folder = new File(mPathView.getText().toString());
						loadData(folder,sorttype);
					}
				}
			}).setPositiveButton("取消", null).create().show();

			break;

		case R.id.plore_btn_cut:
			if (!mFileAdpter.isShow_cb()) {
				mFileAdpter.setShow_cb(true);
				mFileAdpter.notifyDataSetChanged();
			} else {
				Boolean[] mlist = mFileAdpter.getCheckBox_List();
				fileList.clear();
				if (mFileAdpter.isShow_cb()) {
					for (int i = 0; i < mlist.length; i++) {
						if (mlist[i]) {
							File file = (File) mFileAdpter.getItem(i);
							fileList.add(file);
						}
					}
				}
				mFileAdpter.setShow_cb(false);
				mFileAdpter.notifyDataSetChanged();
				isCopy = false;
				ll_btn.setVisibility(View.GONE);
				ll_btn_default.setVisibility(View.VISIBLE);
				Toast.makeText(PloreActivity.this, "剪切成功", Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.plore_btn_paste:
			if (fileList.isEmpty())
				Toast.makeText(PloreActivity.this, "没有选择文件", Toast.LENGTH_SHORT).show();
			else {
				String path = mPathView.getText().toString();
				if (!isCopy) {
					for (int i = 0; i < fileList.size(); i++) {
						File file = fileList.get(i);
						file.renameTo(new File(path + "/" + file.getName()));
					}
					fileList.clear();
					File folder = new File(mPathView.getText().toString());
					loadData(folder,sorttype);
				} else {
					for (int i = 0; i < fileList.size(); i++) {
						File file = fileList.get(i);
						File newFile = new File(path + "/" + file.getName());
						if (!newFile.exists()) {
							try {
								newFile.createNewFile();
							} catch (IOException e) {

								e.printStackTrace();
							}

							FileUtils.fileChannelCopy(file, newFile);
						} else {
							Toast.makeText(PloreActivity.this, "文件已存在", Toast.LENGTH_SHORT).show();
						}
					}
					fileList.clear();
					File folder = new File(mPathView.getText().toString());
					loadData(folder,sorttype);
				}
			}
			break;
		case R.id.plore_btn_sort:
			PopupMenu popupsort = new PopupMenu(PloreActivity.this, v);
			popupsort.getMenuInflater().inflate(R.menu.sort_menu, popupsort.getMenu());
			popupsort.setOnMenuItemClickListener(this);		
			popupsort.show();
			break;
		case R.id.plore_btn_selectall:
			mFileAdpter.setShow_cb(true);
			mFileAdpter.setAllSelect();
			mFileAdpter.notifyDataSetChanged();
			break;
		case R.id.plore_btn_copy:
			if (!mFileAdpter.isShow_cb()) {
				mFileAdpter.setShow_cb(true);
				mFileAdpter.notifyDataSetChanged();
			} else {
				Boolean[] mlist = mFileAdpter.getCheckBox_List();
				fileList.clear();
				if (mFileAdpter.isShow_cb()) {
					for (int i = 0; i < mlist.length; i++) {
						if (mlist[i]) {
							File file = (File) mFileAdpter.getItem(i);
							fileList.add(file);
						}
					}
				}
				mFileAdpter.setShow_cb(false);
				mFileAdpter.notifyDataSetChanged();
				isCopy = true;
				ll_btn.setVisibility(View.GONE);
				ll_btn_default.setVisibility(View.VISIBLE);
				Toast.makeText(PloreActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.plore_btn_more:
			PopupMenu popup = new PopupMenu(PloreActivity.this, v);
			popup.getMenuInflater().inflate(R.menu.more_menu, popup.getMenu());
			popup.setOnMenuItemClickListener(this);
			
			popup.show();
			break;
		case R.id.iv_back:
			if (mFileAdpter.isShow_cb()) {
				mFileAdpter.setShow_cb(false);
				ll_btn.setVisibility(View.GONE);
				ll_btn_default.setVisibility(View.VISIBLE);
				mFileAdpter.notifyDataSetChanged();
			} else {
				String str = (String) mPathView.getText();
				if (str.lastIndexOf("/") == 0) {

					loadData(new File("/storage"),sorttype);
				} else {
					loadData(new File((String) str.subSequence(0, str.lastIndexOf("/"))),sorttype);
				}
			}
			break;

		case R.id.path:
			if (mFileAdpter.isShow_cb()) {
				mFileAdpter.setShow_cb(false);
				ll_btn.setVisibility(View.GONE);
				ll_btn_default.setVisibility(View.VISIBLE);
				mFileAdpter.notifyDataSetChanged();
			} else {
				String str = (String) mPathView.getText();
				if (str.lastIndexOf("/") == 0) {

					loadData(new File("/storage"),sorttype);
				} else {
					loadData(new File((String) str.subSequence(0, str.lastIndexOf("/"))),sorttype);
				}
			}
			break;

		default:
			break;
		}

	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		switch (parent.getId()) {
		case R.id.file_list:
			if (!(view instanceof ImageView))
				if (!mFileAdpter.isShow_cb()) {
					mFileAdpter.setShow_cb(true);
					ll_btn_default.setVisibility(View.GONE);
					ll_btn.setVisibility(View.VISIBLE);
					mFileAdpter.notifyDataSetChanged();
				} else {
				}
			return true;

		default:
			break;
		}
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		switch (parent.getId()) {
		case R.id.file_list:
			File file = (File) parent.getItemAtPosition(position);
			if (!file.canRead()) {
				Toast.makeText(parent.getContext(), "打不开", Toast.LENGTH_SHORT).show();
			} else if (file.isDirectory()) {
				loadData(file,sorttype);
			} else {
				startActivity(Utils.openFile(file));
			}
			break;

		default:
			break;
		}

	}

	

	@Override
	public void OnRefresh() {
		mRefreshAsynTask = new RefreshDataAsynTask();
		mRefreshAsynTask.execute();

	}

	long curtime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mPathView.getText().toString().lastIndexOf("/") == 0) {
				if (mFileAdpter.isShow_cb()) {
					mFileAdpter.setShow_cb(false);
					ll_btn.setVisibility(View.GONE);
					ll_btn_default.setVisibility(View.VISIBLE);
					mFileAdpter.notifyDataSetChanged();
				} else if (java.lang.System.currentTimeMillis() - curtime > 1000) {
					Toast.makeText(PloreActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
					curtime = java.lang.System.currentTimeMillis();
					return true;
				} else {
					finish();
				}
				return true;
			} else {
				return iv_back.callOnClick();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void applyScrollListener() {
		mListView.setOnScrollListener(new PauseOnScrollListener(imageLoader, pauseOnScroll, pauseOnFling));
	}

	@Override
	public void onResume() {
		super.onResume();
		applyScrollListener();
	}

	

	@SuppressWarnings("deprecation")
	@Override
	public boolean onMenuItemClick(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.it_meun_delete:
			if (!mFileAdpter.isShow_cb()) {
				mFileAdpter.setShow_cb(true);
				mFileAdpter.notifyDataSetChanged();
			} else {
				Boolean[] mlist = mFileAdpter.getCheckBox_List();
				for (int i = 0; i < mlist.length; i++) {
					if (mlist[i]) {
						File file = (File) mFileAdpter.getItem(i);
						Log.e("file", file.getName());
						if (file.delete()) {

							Toast.makeText(PloreActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(PloreActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
						}
					}
				}
				File folder = new File(mPathView.getText().toString());
				loadData(folder,sorttype);
			}
			break;

		case R.id.it_meun_share:

			if (!mFileAdpter.isShow_cb()) {
				mFileAdpter.setShow_cb(true);
				mFileAdpter.notifyDataSetChanged();
			} else {
				Boolean[] mlist = mFileAdpter.getCheckBox_List();
				for (int i = 0; i < mlist.length; i++) {
					if (mlist[i]) {
						File file = (File) mFileAdpter.getItem(i);

						String s = CoreApp.mBinder.AddShareFile(file.getPath());
						Toast.makeText(PloreActivity.this, "AddShareFile  " + s, Toast.LENGTH_SHORT).show();
					}
				}
				File folder = new File(mPathView.getText().toString());
				loadData(folder,sorttype);

			}

			break;
		case R.id.it_meun_push:
			ArrayList<String> pushList = new ArrayList<String>();
			if (!mFileAdpter.isShow_cb()) {
				mFileAdpter.setShow_cb(true);
				mFileAdpter.notifyDataSetChanged();
			} else {
				Boolean[] mlist = mFileAdpter.getCheckBox_List();
				for (int i = 0; i < mlist.length; i++) {
					if (mlist[i]) {
						File file = (File) mFileAdpter.getItem(i);
						if (!file.isDirectory()) {

							MyApp myapp = (MyApp) getApplication();
							String ip = myapp.getIp();
							int port = myapp.getPort();
							pushList.add("http://" + ip + ":" + port + file.getPath());
						} else {
							Toast.makeText(PloreActivity.this, "文件夹暂不支持推送", Toast.LENGTH_SHORT).show();
						}
					}
				}
				if (pushList.size() > 0) {
					Intent intent = new Intent();
					Bundle b = new Bundle();
					b.putStringArrayList("pushList", pushList);
					intent.putExtra("pushList", b);
					intent.setClass(PloreActivity.this, ShowNetDevActivity.class);
					startActivity(intent);
				}

			}
			break;

		case R.id.it_meun_qr:
			String ssid = "~";
			WifiManager wifiManager = (WifiManager) PloreActivity.this.getSystemService(Context.WIFI_SERVICE);
			if (wifiManager.isWifiEnabled()) {
				WifiInfo info = wifiManager.getConnectionInfo();
				if (info != null) {
					ssid = info.getSSID();
				} else {
					HPaConnector hpc = HPaConnector.getInstance(PloreActivity.this);

					try {
						// hpc.setupWifiAp("hipa2014");

						WifiConfiguration wificonf = hpc.setupWifiAp("fileplore", "12345678");
						ssid = wificonf.SSID;
						Thread.sleep(500);
						MyApp app = (MyApp) PloreActivity.this.getApplicationContext();
						app.unbindService(new ServiceConnection() {

							@Override
							public void onServiceDisconnected(ComponentName name) {

							}

							@Override
							public void onServiceConnected(ComponentName name, IBinder service) {

							}
						});
						app.setConnectedService(new ConnectedService() {

							@Override
							public void onConnected(Binder b) {
								CoreServiceBinder binder = (CoreServiceBinder) b;
								binder.init();
								binder.setCoreHttpServerCBFunction(httpServerCB);
								binder.StartHttpServer("/", MyApp.context);
							}
						});
						// hpc.me();
					} catch (Exception e) {
						e.printStackTrace();
						Log.e("eee11", e.getMessage());
						Toast.makeText(PloreActivity.this, "开启wifi热点失败", Toast.LENGTH_LONG).show();
					}
				}
			} else {
				HPaConnector hpc = HPaConnector.getInstance(PloreActivity.this);

				try {

					WifiConfiguration wificonf = hpc.setupWifiAp("fileplore", "12345678");
					ssid = wificonf.SSID;
					Thread.sleep(500);
					CoreApp.mBinder.deinit();
					CoreApp.mBinder.init();

				} catch (Exception e) {
					e.printStackTrace();
					Log.e("eee22", e.getMessage());
					Toast.makeText(PloreActivity.this, "开启wifi热点失败", Toast.LENGTH_LONG).show();
				}
			}
			StringBuffer sb = new StringBuffer();
			if (!mFileAdpter.isShow_cb()) {
				mFileAdpter.setShow_cb(true);
				mFileAdpter.notifyDataSetChanged();
			} else {
				Boolean[] mlist = mFileAdpter.getCheckBox_List();
				sb.append("fileplore|" + ssid + "|");
				for (int i = 0; i < mlist.length; i++) {
					if (mlist[i]) {
						File file = (File) mFileAdpter.getItem(i);
						if (!file.isDirectory()) {
							MyApp myapp = (MyApp) getApplication();
							String ip = myapp.getIp();
							int port = myapp.getPort();
							sb.append("http://" + ip + ":" + port + file.getPath() + "|");
						} else {
							Toast.makeText(PloreActivity.this, "暂不支持文件夹", Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
			float scale = PloreActivity.this.getResources().getDisplayMetrics().density;
			iv_qr.setImageBitmap(
					Utils.createImage(sb.toString(), (int) (200 * scale + 0.5f), (int) (200 * scale + 0.5f)));
			alertDialog_qr.show();
			alertDialog_qr.getWindow().setLayout((int) (210 * scale + 0.5f), (int) (200 * scale + 0.5f));
			break;
		case R.id.it_meun_detail:
			ArrayList<File> detailList = new ArrayList<File>();
			if (!mFileAdpter.isShow_cb()) {
				mFileAdpter.setShow_cb(true);
				mFileAdpter.notifyDataSetChanged();
			} else {
				Boolean[] mlist = mFileAdpter.getCheckBox_List();
				for (int i = 0; i < mlist.length; i++) {
					if (mlist[i]) {
						File file = (File) mFileAdpter.getItem(i);
						if (!file.isDirectory()) {
							detailList.add(file);
						} else {
							// Toast.makeText(PloreActivity.this, "文件夹暂不支持推送",
							// Toast.LENGTH_SHORT).show();
						}
					}
				}
				if (detailList.size() == 1) {
					File detailfile = detailList.get(0);
					String space = Formatter.formatFileSize(PloreActivity.this, detailfile.getTotalSpace());
					String path = detailfile.getPath();
					String time = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(detailfile.lastModified());
					String name = detailfile.getName();

					DetailDialogFragment detailDialog = new DetailDialogFragment(name, path, time, space);
					detailDialog.show(getFragmentManager(), "detailDialog");

				}

			}
			break;
		case R.id.it_meun_sorttime:
			sorttype = PloreData.TIME;
			loadData(new File(mPathView.getText().toString()), sorttype);
			break;
		case R.id.it_meun_sortname:
			sorttype = PloreData.NAME;
			loadData(new File(mPathView.getText().toString()), sorttype);
			break;
		default:
			break;
		}

		return true;
	}
	class RefreshDataAsynTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {

			try {
				Thread.sleep(700);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			loadData(new File(mPathView.getText().toString()),sorttype);
			mListView.onRefreshComplete();
		}

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

			AlertDialog alert = dialog.setTitle("有推送文件").setMessage(pushlist.remove(0))
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
	public void onClick(View v, File file) {
		if (!file.canRead()) {
			Toast.makeText(this, "打不开", Toast.LENGTH_SHORT).show();
		} else if (file.isDirectory()) {
			loadData(file,sorttype);
		} else {
			startActivity(Utils.openFile(file));
		}
		
	}
}
