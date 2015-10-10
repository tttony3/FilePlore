package com.changhong.fileplore.activities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.changhong.fileplore.adapter.PloreListAdapter;
import com.changhong.fileplore.application.MyApp;
import com.changhong.fileplore.base.BaseActivity;
import com.changhong.fileplore.data.PloreData;
import com.changhong.fileplore.implement.PloreInterface;
import com.changhong.fileplore.view.RefreshListView;
import com.chobit.corestorage.CoreApp;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.changhong.fileplore.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.changhong.fileplore.utils.*;

public class PloreActivity extends BaseActivity implements RefreshListView.IOnRefreshListener, View.OnClickListener,
		PloreInterface, OnItemClickListener, OnItemLongClickListener {
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	protected static final String STATE_PAUSE_ON_SCROLL = "STATE_PAUSE_ON_SCROLL";
	protected static final String STATE_PAUSE_ON_FLING = "STATE_PAUSE_ON_FLING";
	protected boolean pauseOnScroll = false;
	protected boolean pauseOnFling = true;
	ArrayList<File> fileList;
	private RefreshDataAsynTask mRefreshAsynTask;
	RefreshListView mListView;
	TextView mPathView;
	ImageView iv_back;
	TextView mItemCount;
	Button btn_4;
	Button btn_1;
	Button btn_2;
	Button btn_3;
	Button btn_more;
	public PloreListAdapter mFileAdpter;
	public LinearLayout ll_btn;
	public AlertDialog.Builder builder;
	boolean isCopy = false;
	private View layout_qr;
	private Builder builder_qr;
	private AlertDialog alertDialog_qr;
	private ImageView iv_qr;

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		pauseOnScroll = savedInstanceState.getBoolean(STATE_PAUSE_ON_SCROLL, false);
		pauseOnFling = savedInstanceState.getBoolean(STATE_PAUSE_ON_FLING, true);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		imageLoader.init(ImageLoaderConfiguration.createDefault(this));
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
		btn_1 = findView(R.id.plore_btn_1);
		btn_2 = findView(R.id.plore_btn_2);
		btn_3 = findView(R.id.plore_btn_3);
		btn_more = findView(R.id.plore_btn_more);
		ll_btn = findView(R.id.ll_btn);

		layout_qr = this.getLayoutInflater().inflate(R.layout.dialog_qr, null);
		builder_qr = new AlertDialog.Builder(this).setView(layout_qr);
		alertDialog_qr = builder_qr.create();
		iv_qr = (ImageView) layout_qr.findViewById(R.id.iv_qr);
	}

	private void initView() {

		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		mListView.setOnRefreshListener(this);
		btn_1.setOnClickListener(this);
		btn_2.setOnClickListener(this);
		btn_3.setOnClickListener(this);
		btn_more.setOnClickListener(this);
		iv_back.setOnClickListener(this);
		mPathView.setOnClickListener(this);

		File folder = new File("/storage");
		loadData(folder);
	}

	@Override
	public void loadData(File folder) {
		ll_btn.setVisibility(View.GONE);
		boolean isRoot = (folder.getParent() == null);
		if (folder.canRead()) {
			String path = folder.getPath();
			String[] names = folder.list();
			mPathView.setText(path);
			mItemCount.setText(names.length + "项");

			PloreData mPloreData = new PloreData();
			List<File> files = mPloreData.lodaData(folder);
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

		case R.id.plore_btn_1:
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
						loadData(folder);
					}
				}
			}).setPositiveButton("取消", null).create().show();

			break;

		case R.id.plore_btn_2:
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
				Toast.makeText(PloreActivity.this, "剪切成功", Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.plore_btn_3:
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
					loadData(folder);
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
					loadData(folder);
				}
			}
			break;
		case R.id.plore_btn_more:

			AlertDialog.Builder dialog = new AlertDialog.Builder(PloreActivity.this);
			dialog.setTitle("");
			String[] dataArray = new String[] { "复制", "删除", "共享", "推送", "生成二维码" };
			dialog.setItems(dataArray, new DialogInterface.OnClickListener() {
				/***
				 * 我们这里传递给dialog.setItems方法的参数为数组，这就导致了我们下面的
				 * onclick方法中的which就跟数组下标是一样的，点击hello时返回0；点击baby返回1……
				 */
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:
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
							Toast.makeText(PloreActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
						}
						break;
					case 1:
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
							loadData(folder);
						}
						break;

					case 2:

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
							loadData(folder);

						}

						break;
					case 3:
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

					case 4:
						StringBuffer sb = new StringBuffer();
						if (!mFileAdpter.isShow_cb()) {
							mFileAdpter.setShow_cb(true);
							mFileAdpter.notifyDataSetChanged();
						} else {
							Boolean[] mlist = mFileAdpter.getCheckBox_List();
							sb.append("fileplore|");
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
						iv_qr.setImageBitmap(Utils.createImage(sb.toString(), 320, 320));
						alertDialog_qr.show();
						alertDialog_qr.getWindow().setLayout(370, 370);
						break;
					default:
						break;
					}

				}
			}).show();

			break;
		case R.id.iv_back:
			if (mFileAdpter.isShow_cb()) {
				mFileAdpter.setShow_cb(false);
				ll_btn.setVisibility(View.GONE);
				mFileAdpter.notifyDataSetChanged();
			} else {
				String str = (String) mPathView.getText();
				if (str.lastIndexOf("/") == 0) {

					loadData(new File("/storage"));
				} else {
					loadData(new File((String) str.subSequence(0, str.lastIndexOf("/"))));
				}
			}
			break;

		case R.id.path:
			if (mFileAdpter.isShow_cb()) {
				mFileAdpter.setShow_cb(false);
				ll_btn.setVisibility(View.GONE);
				mFileAdpter.notifyDataSetChanged();
			} else {
				String str = (String) mPathView.getText();
				if (str.lastIndexOf("/") == 0) {

					loadData(new File("/storage"));
				} else {
					loadData(new File((String) str.subSequence(0, str.lastIndexOf("/"))));
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
				loadData(file);
			} else {
				startActivity(Utils.openFile(file));
			}
			break;

		default:
			break;
		}

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
			loadData(new File(mPathView.getText().toString()));
			mListView.onRefreshComplete();
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

				if (java.lang.System.currentTimeMillis() - curtime > 1000) {
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
}
