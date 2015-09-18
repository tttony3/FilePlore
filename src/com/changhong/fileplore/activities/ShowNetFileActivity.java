package com.changhong.fileplore.activities;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.changhong.alljoyn.simpleclient.DeviceInfo;
import com.changhong.alljoyn.simpleservice.FC_GetShareFile;
import com.changhong.fileplore.R;

import com.changhong.fileplore.adapter.NetShareFileListAdapter;
import com.changhong.fileplore.utils.Player;
import com.changhong.fileplore.utils.StreamTool;
import com.changhong.fileplore.view.CircleProgress;
import com.changhong.synergystorage.javadata.JavaFile;
import com.changhong.synergystorage.javadata.JavaFolder;
import com.changhong.synergystorage.javadata.JavaVideoFile;
import com.chobit.corestorage.CoreApp;
import com.chobit.corestorage.CoreDownloadProgressCB;
import com.chobit.corestorage.CoreShareFileListener;
import com.example.libevent2.UpdateDownloadPress;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class ShowNetFileActivity extends Activity {

	static private final int UPDATE_LIST = 1;
	static private final int SHOW_DIALOG = 2;
	static private final int DISMISS_DIALOG = 3;
	static private final int SHOW_PREVIEW_DIALOG = 4;
	static private final int UPDATE_BAR = 5;
	static private final int RESET_BAR = 6;
	static private final int SET_CURTIME = 7;
	static private final int SET_TOTALTIME = 8;
	static private final int UPDATE_PREVIEW = 9;
	static private final int UPDATE_DOWNLOAD_BAR = 10;
	static private final int SHOW_DOWN_BAR = 11;
	static private final int DISMISS_DOWN_BAR = 12;
	// private static ProgressDialog dialog;
	JavaFile file;
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
	LinkedList<JavaFolder> fatherList = new LinkedList<JavaFolder>();
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

	View layout_videoplayer;
	AlertDialog alertDialog_videoplayer;
	AlertDialog.Builder builder_videoplayer;
	private VideoView video1;
	MediaController mediaco;
	private SurfaceView surfaceView;
	private Button btnPause, btnPlayUrl, btnStop;
	private SeekBar skbProgress;
	private Player player;

	View layout_download;
	AlertDialog alertDialog_download;
	AlertDialog.Builder builder_download;
	Button btn_stop;
	ProgressBar pb_download;
	TextView tv_download;
	boolean isremove = false;
	boolean isadd = true;
	boolean isexit = false;
	JavaFolder lastremove;

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

		layout_download = inflater.inflate(R.layout.dialog_download, null);
		builder_download = new AlertDialog.Builder(this).setView(layout_download);
		alertDialog_download = builder_download.create();
		btn_stop = (Button) layout_download.findViewById(R.id.download_stop);
		pb_download = (ProgressBar) layout_download.findViewById(R.id.download_bar);
		tv_download = (TextView) layout_download.findViewById(R.id.tv_process);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			isadd = false;
			Log.e("fatherList", "aa");
			if (isremove && fatherList.size() != 1) {

				Log.e("remove", fatherList.removeLast().getLocation());
				isremove = false;
			}

			if (fatherList.size() >= 1 && !isexit) {
				JavaFolder folder;

				Log.e("fatherList", "bb");
				Log.e("fatherList", fatherList.toString());
				if (fatherList.size() == 1) {
					folder = fatherList.getLast();
					isexit = true;
				} else {
					folder = fatherList.removeLast();
					Log.e("remove", folder.getLocation());
					lastremove = folder;
				}
				showDialog();
				CoreApp.mBinder.getFolderChildren(devInfo, folder, folder);

			} else {
				CoreApp.mBinder.DisSession();
				finish();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private CoreShareFileListener shareListener = new CoreShareFileListener() {

		@Override
		public void updateShareContent(JavaFolder folder) {
			if (folder != null) {
				tv_path.setText(folder.getLocation());
				if (!fatherList.contains(folder) && isadd) {
					fatherList.addLast(folder);
					Log.e("add", folder.getLocation());
				}
				if (null != folder.getParent()) {

				}
				shareFileList = folder.getSubFileList();
				shareFolderList = folder.getSubFolderList();
				int n = 0;
				if (shareFileList != null) {
					n = n + shareFileList.size();
				}
				if (shareFolderList != null) {
					n = n + shareFolderList.size();
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
					lv_sharepath.invalidate();
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
					iv_preview.setImageResource(R.drawable.picload);
					String path = msg.getData().getString("path");
					new DownloadImageTask(iv_preview).execute(devInfo.getM_httpserverurl() + path);
					alertDialog_preview.show();
					break;
				case UPDATE_BAR:
					Log.e("value", msg.getData().getInt("value") + "");
					pb_media.setProgress(msg.getData().getInt("value"));
					curtime = curtime + 1;
					tv_curtime.setText(curtime / 60 + ":" + (curtime - (curtime / 60) * 60));
					break;
				case RESET_BAR:

					pb_media.setProgress(0);
					tv_curtime.setText("00:00");
					tv_totaltime.setText("00:00");
					curtime = 0;
					break;
				case SET_TOTALTIME:
					tv_totaltime.setText(time / 60 + ":" + (time - (time / 60) * 60));
					break;
				case UPDATE_DOWNLOAD_BAR:
					if (!alertDialog_download.isShowing()) {
						long total = msg.getData().getLong("total");
						int max = (int) (total / 100);
						pb_download.setMax(max);
						tv_download.setText("0%");
						alertDialog_download.show();
					} else {
						long part = msg.getData().getLong("part");
						pb_download.setProgress((int) (part / 100));
						int p = (int) (part / pb_download.getMax());
						tv_download.setText(p + "%");
					}
					break;
				case DISMISS_DOWN_BAR:
					if (alertDialog_download.isShowing())
						alertDialog_download.dismiss();
					break;
				default:
					break;
				}

			}
		}
	};

	int time = 0;
	MyBarThread thread;

	class MyOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if(filenum.getText().equals("0项")){
				Toast.makeText(ShowNetFileActivity.this, "空文件夹", Toast.LENGTH_SHORT).show();
				return;
			}
			JavaFolder folder;

			if (shareFolderList != null) {
				if (position < shareFolderList.size()) {
					showDialog();
					if (!isadd&&!isexit) {
						fatherList.addLast(lastremove);
						isadd = true;
					}
					isexit = false;
					
					folder = (JavaFolder) parent.getItemAtPosition(position);
					// if (!fatherList.contains(folder)) {
					// fatherList.addLast(folder);
					// Log.e("add", folder.getLocation());
					// }
				
					CoreApp.mBinder.getFolderChildren(devInfo, folder, folder);
					isremove = true;

				} else
					setFileOnClick(parent, position);
			} else {
				setFileOnClick(parent, position);
			}

		}

		private void setFileOnClick(AdapterView<?> parent, int position) {
			if(filenum.getText().equals("0项")){
				Toast.makeText(ShowNetFileActivity.this, "空文件夹", Toast.LENGTH_SHORT).show();
				return;
			}
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
							MediaButtonListener mediaButtonListener = new MediaButtonListener(file);
							ib_stop.setOnClickListener(mediaButtonListener);
							ib_start.setOnClickListener(mediaButtonListener);

						} else if (file.getFileType() == JavaFile.FileType.VIDEO) {

							// Uri uri = Uri.parse(
							// devInfo.getM_httpserverurl() +
							// file.getLocation() );
							// Intent intent = new
							// Intent(Intent.ACTION_VIEW,uri);
							// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							// intent.setType("video/*");
							// intent.setDataAndType(uri , "video/*");
							// startActivity(intent);

							Intent intent = new Intent();
							intent.putExtra("uri", devInfo.getM_httpserverurl() + file.getLocation());
							intent.setClass(ShowNetFileActivity.this, VideoActivity.class);
							startActivity(intent);
						}
						break;
					case 1:
						btn_stop.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								CoreApp.mBinder.cancelDownload(devInfo.getM_httpserverurl() + file.getLocation());

							}
						});
						CoreApp.mBinder.getShareFileDownload(devInfo, file.getLocation(), null);
						break;
					default:
						break;
					}

				}

			}).show();

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

			dismissDownDialog();

			String download_Path = Environment.getExternalStorageDirectory().getAbsolutePath();
			String appname = FC_GetShareFile.getApplicationName(ShowNetFileActivity.this);
			Toast.makeText(ShowNetFileActivity.this, "下载成功,保存在" + download_Path + "/" + appname + "/download/ 目录下",
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onDowloadProgress(UpdateDownloadPress press) {
			Message msg = new Message();
			Bundle bundle = new Bundle();
			bundle.putInt("key", UPDATE_DOWNLOAD_BAR);
			bundle.putLong("part", press.part);
			bundle.putLong("total", press.total);
			msg.setData(bundle);
			handler.sendMessage(msg);

		}

		@Override
		public void onDowloaStop(String fileuri) {
			Log.e("Progress", "stop" + fileuri);
			Toast.makeText(ShowNetFileActivity.this, "下载停止", Toast.LENGTH_SHORT).show();
			dismissDownDialog();

		}

		private void dismissDownDialog() {
			Message msg = new Message();
			Bundle bundle = new Bundle();
			bundle.putInt("key", DISMISS_DOWN_BAR);
			msg.setData(bundle);
			handler.sendMessage(msg);

		}

		@Override
		public void onDowloaFailt(String fileuri) {
			Log.e("Progress", "failt" + fileuri);
			Toast.makeText(ShowNetFileActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
			dismissDownDialog();

		}

		@Override
		public void onDowloaCancel(String fileuri) {
			Toast.makeText(ShowNetFileActivity.this, "下载取消", Toast.LENGTH_SHORT).show();
			dismissDownDialog();

		}

		@Override
		public void onConnectError(String fileuri) {
			Log.e("Progress", "error" + fileuri);
			Toast.makeText(ShowNetFileActivity.this, "下载错误", Toast.LENGTH_SHORT).show();
			dismissDownDialog();

		}
	};

	class MediaButtonListener implements OnClickListener {
		JavaFile file;

		MediaButtonListener(JavaFile file) {
			this.file = file;
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {

			case R.id.media_play:
				try {
					if (thread != null)
						thread.isvalid = false;
					Message msg = new Message();
					Bundle bundle = new Bundle();
					bundle.putInt("key", RESET_BAR);
					msg.setData(bundle);
					handler.sendMessage(msg);

					mp.reset();
					mp.setDataSource(devInfo.getM_httpserverurl() + file.getLocation());
					mp.prepare();
					mp.start();
					thread = new MyBarThread();
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

				break;
			case R.id.media_stop:

				if (mp.isPlaying())
					mp.stop();
				thread.isvalid = false;
				Message msg = new Message();
				Bundle bundle = new Bundle();
				bundle.putInt("key", RESET_BAR);
				msg.setData(bundle);
				handler.sendMessage(msg);

				break;
			default:
				break;
			}

		}

	}

	class MyBarThread extends Thread {

		public Boolean isvalid = true;

		@Override
		public void run() {

			super.run();
			while (pb_media.getProgress() <= 10010 && isvalid) {
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

	class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		ImageView im;

		public DownloadImageTask(ImageView im) {
			this.im = im;
		}

		protected Bitmap doInBackground(String... urls) {

			return loadImageFromNetwork(urls[0]);
		}

		private Bitmap loadImageFromNetwork(String string) {
			Bitmap bitmap = null;
			URL url;
			try {
				url = new URL(string);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET"); // 设置请求方法为GET
				conn.setReadTimeout(5 * 1000); // 设置请求过时时间为5秒
				InputStream inputStream = conn.getInputStream(); // 通过输入流获得图片数据
				byte[] data = StreamTool.readInputStream(inputStream);
				Options op = new Options();
				op.inSampleSize = 4;
				bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, op);
			} catch (MalformedURLException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}

			return bitmap;
		}

		protected void onPostExecute(Bitmap result) {
			im.setImageBitmap(result);
		}
	}
}
