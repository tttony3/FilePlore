package com.changhong.fileplore.activities;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import com.changhong.alljoyn.simpleclient.DeviceInfo;
import com.changhong.alljoyn.simpleservice.FC_GetShareFile;
import com.changhong.fileplore.R;

import com.changhong.fileplore.adapter.NetShareFileListAdapter;
import com.changhong.fileplore.view.CircleProgress;
import com.changhong.synergystorage.javadata.JavaFile;
import com.changhong.synergystorage.javadata.JavaFolder;
import com.chobit.corestorage.CoreApp;
import com.chobit.corestorage.CoreDownloadProgressCB;
import com.chobit.corestorage.CoreShareFileListener;
import com.example.libevent2.UpdateDownloadPress;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ShowNetFileActivity extends Activity {

	static private final int UPDATE_LIST = 1;
	static private final int SHOW_DIALOG = 2;
	static private final int DISMISS_DIALOG = 3;
	static private final int SHOW_PREVIEW_DIALOG = 4;
	static private final int UPDATE_BAR = 5;
	static private final int RESET_BAR = 6;
	static private final int SET_CURTIME = 7;
	static private final int SET_TOTALTIME = 8;
	// private static ProgressDialog dialog;
	private List<JavaFile> shareFileList;
	private List<JavaFolder> shareFolderList;
	static private ListView lv_sharepath;
	private TextView tv_path;
	private NetShareFileListAdapter netShareFileListAdapter;
	private MyOnItemClickListener myOnItemClickListener;
	private Handler handler;
	private TextView filenum;
	AlertDialog alertDialog_progress;
	AlertDialog.Builder builder_progress;
	CircleProgress mProgressView;
	View layout_progress;

	View layout_preview;
	AlertDialog alertDialog_preview;
	AlertDialog.Builder builder_preview;
	ImageView iv_preview;

	View layout_mediaplayer;
	AlertDialog alertDialog_mediaplayer;
	AlertDialog.Builder builder_mediaplayer;
	ImageButton ib_start;
	ImageButton ib_stop;
	ProgressBar pb_media;
	TextView tv_curtime;
	TextView tv_totaltime;
	int curtime = 0;
	LayoutInflater inflater;
	DeviceInfo devInfo;
	private MediaPlayer mp = new MediaPlayer();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_net_file);
		findView();

		Intent intent = getIntent();
		int position = intent.getFlags();
		devInfo = CoreApp.mBinder.GetDeviceList().get(position);
		CoreApp.mBinder.setShareFileListener(shareListener);
		CoreApp.mBinder.ConnectDeivce(devInfo);
		CoreApp.mBinder.setDownloadCBInterface(downloadCB);
		netShareFileListAdapter = new NetShareFileListAdapter(shareFileList, shareFolderList, devInfo, this);
		myOnItemClickListener = new MyOnItemClickListener();

		lv_sharepath.setAdapter(netShareFileListAdapter);
		lv_sharepath.setOnItemClickListener(myOnItemClickListener);
	}

	private void findView() {
		handler = new MyNetHandler(this);
		tv_path = (TextView) findViewById(R.id.path);
		filenum = (TextView) findViewById(R.id.netfile_num);
		lv_sharepath = (ListView) findViewById(R.id.lv_netsharepath);
		// dialog = new MyProgressDialog(ShowNetFileActivity.this).getDialog();
		// path = "文件路径 : ";
		// tv_path.setText(path);
		inflater = getLayoutInflater();
		layout_progress = inflater.inflate(R.layout.circle_progress, (ViewGroup) findViewById(R.id.rl_progress));
		builder_progress = new AlertDialog.Builder(this).setView(layout_progress);
		alertDialog_progress = builder_progress.create();
		mProgressView = (CircleProgress) layout_progress.findViewById(R.id.progress);

		layout_preview = inflater.inflate(R.layout.dialog_preview, (ViewGroup) findViewById(R.id.rl_preview));
		builder_preview = new AlertDialog.Builder(this).setView(layout_preview);
		alertDialog_preview = builder_preview.create();
		iv_preview = (ImageView) layout_preview.findViewById(R.id.iv_preview);

		layout_mediaplayer = inflater.inflate(R.layout.media_player, (ViewGroup) findViewById(R.id.rl_mediaplayer));
		builder_mediaplayer = new AlertDialog.Builder(this).setView(layout_mediaplayer);
		alertDialog_mediaplayer = builder_mediaplayer.create();
		ib_start = (ImageButton) layout_mediaplayer.findViewById(R.id.media_play);
		ib_stop = (ImageButton) layout_mediaplayer.findViewById(R.id.media_stop);
		pb_media = (ProgressBar) layout_mediaplayer.findViewById(R.id.media_bar);
		tv_curtime = (TextView) layout_mediaplayer.findViewById(R.id.tv_curtime);
		tv_totaltime = (TextView) layout_mediaplayer.findViewById(R.id.tv_totaltime);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			CoreApp.mBinder.DisSession();
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private CoreShareFileListener shareListener = new CoreShareFileListener() {

		@Override
		public void updateShareContent(JavaFolder folder) {
			if (folder != null) {
				tv_path.setText(folder.getLocation());

				Log.e("this rootlv", folder.getRootLevel() + "");
				if (null != folder.getParent()) {
					Log.e("parents rootlv", folder.getParent().getRootLevel() + "");
					Log.e("parents getLocation", folder.getParent().getLocation() + "");
				}
				shareFileList = folder.getSubFileList();
				shareFolderList = folder.getSubFolderList();
				int n = 0;
				if (shareFileList != null) {
					n = n + shareFileList.size();
					Log.e("shareFileList", shareFileList.toString());
				}
				if (shareFolderList != null) {
					n = n + shareFolderList.size();
					Log.e("shareFolderList", shareFolderList.toString());
				}
				filenum.setText(n + "项");
				if (shareFileList != null || shareFolderList != null) {
					Message msg = new Message();
					Bundle bundle = new Bundle();
					bundle.putInt("key", UPDATE_LIST);
					msg.setData(bundle);
					handler.sendMessage(msg);
				}
			} else {
				Toast.makeText(ShowNetFileActivity.this, "空文件夹", Toast.LENGTH_SHORT).show();
				;
				Log.e("null!!", "folder is null");
			}

			dismissDialog();
		}

		@Override
		public void updateImageThumbNails(Bitmap bt) {

		}

		@Override
		public void stopWaiting() {
			dismissDialog();

		}

		@Override
		public void startWaiting() {
			showDialog();

		}
	};

	class MyNetHandler extends Handler {
		private WeakReference<ShowNetFileActivity> mActivity;

		public MyNetHandler(ShowNetFileActivity activity) {
			mActivity = new WeakReference<ShowNetFileActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			ShowNetFileActivity theActivity = mActivity.get();
			if (theActivity != null) {
				super.handleMessage(msg);
				int key = msg.getData().getInt("key");
				switch (key) {
				case UPDATE_LIST:
					netShareFileListAdapter.updatelistview(shareFileList, shareFolderList);
					break;

				case SHOW_DIALOG:
					mProgressView.startAnim();
					alertDialog_progress.show();
					break;

				case DISMISS_DIALOG:
					if (alertDialog_progress.isShowing()) {
						mProgressView.stopAnim();
						alertDialog_progress.dismiss();
					}
					break;

				case SHOW_PREVIEW_DIALOG:
					String path = msg.getData().getString("path");
					Bitmap bm = CoreApp.mBinder.getThumbnails(devInfo, path);
					Log.e("bm", bm + "");
					if (bm == null)
						iv_preview.setImageResource(R.drawable.file_icon_photo);
					else
						iv_preview.setImageBitmap(bm);

					alertDialog_preview.show();
					// WindowManager.LayoutParams params =
					// alertDialog_preview.getWindow().getAttributes();
					// params.width = bm.getWidth();
					// params.height = bm.getHeight();
					// alertDialog_preview.getWindow().setAttributes(params);
					break;
				case UPDATE_BAR:
					Log.e("value", msg.getData().getInt("value") + "");
					pb_media.setProgress(msg.getData().getInt("value"));
					curtime = curtime + 1;
					tv_curtime.setText(curtime / 60 + ":" + (curtime - (curtime / 60) * 60));
					break;
				case RESET_BAR:
					if (thread != null)
						thread.isvalid = false;
				//	alertDialog_mediaplayer.dismiss();
					pb_media.setProgress(0);
					tv_curtime.setText("00:00");
					tv_totaltime.setText("00:00");
					curtime = 0;
					break;
				case SET_TOTALTIME:
					tv_totaltime.setText(time / 60 + ":" + (time - (time / 60) * 60));
					break;
				case SET_CURTIME:
					// tv_curtime.setText(text);
					break;
				default:
					break;
				}

			}
		}
	};

	int time = 0;
	MyThread thread;

	class MyOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			final JavaFile file;
			JavaFolder folder;

			if (shareFolderList != null)
				if (position < shareFolderList.size()) {
					showDialog();
					folder = (JavaFolder) parent.getItemAtPosition(position);
					CoreApp.mBinder.getFolderChildren(devInfo, folder, folder);
				} else {
					file = (JavaFile) parent.getItemAtPosition(position);
					AlertDialog.Builder dialog = new AlertDialog.Builder(ShowNetFileActivity.this);
					dialog.setTitle("");
					String[] dataArray = new String[] { "打开", "下载" };
					dialog.setItems(dataArray, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							switch (which) {
							case 0:
								if (file.getFileType() == JavaFile.FileType.IMAGE) {
									showPreviewDialog(file.getLocation());
								} else if (file.getFileType() == JavaFile.FileType.AUDIO) {

									alertDialog_mediaplayer.show();
									ib_stop.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View v) {
											if (mp.isPlaying())
												mp.stop();
											thread.isvalid = false;
											Message msg = new Message();
											Bundle bundle = new Bundle();
											bundle.putInt("key", RESET_BAR);
											msg.setData(bundle);
											handler.sendMessage(msg);
										}
									});
									ib_start.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View v) {
											try {
												Message msg = new Message();
												Bundle bundle = new Bundle();
												bundle.putInt("key", RESET_BAR);
												msg.setData(bundle);
												handler.sendMessage(msg);
												
												mp.reset();
												mp.setDataSource(devInfo.getM_httpserverurl() + file.getLocation());
												mp.prepare();
												mp.start();
												thread = new MyThread();
												thread.start();
											} catch (IllegalArgumentException e) {

												e.printStackTrace();
											} catch (SecurityException e) {

												e.printStackTrace();
											} catch (IllegalStateException e) {

												e.printStackTrace();
											} catch (IOException e) {

												e.printStackTrace();
											}
											mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
												public void onPrepared(MediaPlayer mp) {
													time = mp.getDuration() / 1000;
													Message msg = new Message();
													Bundle bundle = new Bundle();
													bundle.putInt("key", SET_TOTALTIME);
													bundle.putInt("value", time);
													msg.setData(bundle);
													handler.sendMessage(msg);

												}
											});
											mp.setOnCompletionListener(new OnCompletionListener() {
												@Override
												public void onCompletion(MediaPlayer mp) {
													mp.release();
													thread.isvalid = false;
													Message msg = new Message();
													Bundle bundle = new Bundle();
													bundle.putInt("key", RESET_BAR);
													msg.setData(bundle);
													handler.sendMessage(msg);
												}
											});

										}
									});

								} else if (file.getFileType() == JavaFile.FileType.VIDEO)

								{

								}
								break;
							case 1:

								CoreApp.mBinder.getShareFileDownload(devInfo, file.getLocation(), null);
								break;
							default:
								break;
							}

						}

					}).show();

				}

		}

	}

	private void showPreviewDialog(String path) {
		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putInt("key", SHOW_PREVIEW_DIALOG);
		bundle.putString("path", path);
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	private void showDialog() {
		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putInt("key", SHOW_DIALOG);
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	private void dismissDialog() {
		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putInt("key", DISMISS_DIALOG);
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	CoreDownloadProgressCB downloadCB = new CoreDownloadProgressCB() {

		@Override
		public void onDownloadOK(String fileuri) {

			dismissDialog();
			String download_Path = Environment.getExternalStorageDirectory().getAbsolutePath();
			String appname = FC_GetShareFile.getApplicationName(ShowNetFileActivity.this);
			Toast.makeText(ShowNetFileActivity.this, "下载成功,保存在" + download_Path + "/" + appname + "/download/ 目录下",
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onDowloadProgress(UpdateDownloadPress press) {
			Log.e("Progress", "Progress");
			showDialog();

		}

		@Override
		public void onDowloaStop(String fileuri) {
			Log.e("Progress", "stop" + fileuri);
			Toast.makeText(ShowNetFileActivity.this, "下载停止", Toast.LENGTH_SHORT).show();
			dismissDialog();

		}

		@Override
		public void onDowloaFailt(String fileuri) {
			Log.e("Progress", "failt" + fileuri);
			Toast.makeText(ShowNetFileActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
			dismissDialog();

		}

		@Override
		public void onDowloaCancel(String fileuri) {
			Toast.makeText(ShowNetFileActivity.this, "下载取消", Toast.LENGTH_SHORT).show();
			dismissDialog();

		}

		@Override
		public void onConnectError(String fileuri) {
			Log.e("Progress", "error" + fileuri);
			Toast.makeText(ShowNetFileActivity.this, "下载错误", Toast.LENGTH_SHORT).show();
			dismissDialog();
		}
	};

	class MyThread extends Thread {

		public Boolean isvalid = true;

		@Override
		public void run() {

			super.run();
			while (pb_media.getProgress() < 10100 && isvalid) {
				try {
					Thread.sleep(1000);
					Message msg = new Message();
					Bundle bundle = new Bundle();
					bundle.putInt("key", UPDATE_BAR);
					bundle.putInt("value", pb_media.getProgress() + 10000 / time);
					msg.setData(bundle);
					handler.sendMessage(msg);
				} catch (InterruptedException e) {
					//

					e.printStackTrace();
				}

			}
		}

	}
}
