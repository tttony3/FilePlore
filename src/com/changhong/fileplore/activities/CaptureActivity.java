package com.changhong.fileplore.activities;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Vector;

import com.changhong.fileplore.R;
import com.changhong.fileplore.application.MyApp;
import com.changhong.fileplore.camera.BeepManager;
import com.changhong.fileplore.camera.CameraManager;
import com.changhong.fileplore.decode.CaptureActivityHandler;
import com.changhong.fileplore.decode.FinishListener;
import com.changhong.fileplore.decode.InactivityTimer;
import com.changhong.fileplore.utils.HPaConnector;
import com.changhong.fileplore.utils.WifiAutoConnectManager;
import com.changhong.fileplore.utils.WifiAutoConnectManager.WifiCipherType;
import com.changhong.fileplore.view.ViewfinderView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class CaptureActivity extends Activity implements SurfaceHolder.Callback {
	private static final String TAG = CaptureActivity.class.getSimpleName();

	private boolean hasSurface;
	private BeepManager beepManager;// 声音震动管理器。如果扫描成功后可以播放一段音频，也可以震动提醒，可以通过配置来决定扫描成功后的行为。
	public SharedPreferences mSharedPreferences;// 存储二维码条形码选择的状态
	public static String currentState;// 条形码二维码选择状态
	private String characterSet;

	private ViewfinderView viewfinderView;
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private TextView statusView;
	// private TextView scanTextView;
	// private View resultView;

	/**
	 * 活动监控器，用于省电，如果手机没有连接电源线，那么当相机开启后如果一直处于不被使用状态则该服务会将当前activity关闭。
	 * 活动监控器全程监控扫描活跃状态，与CaptureActivity生命周期相同.每一次扫描过后都会重置该监控，即重新倒计时。
	 */
	private InactivityTimer inactivityTimer;
	private CameraManager cameraManager;
	private Vector<BarcodeFormat> decodeFormats;// 编码格式
	private CaptureActivityHandler mHandler;// 解码线程

	private static final Collection<ResultMetadataType> DISPLAYABLE_METADATA_TYPES = EnumSet.of(
			ResultMetadataType.ISSUE_NUMBER, ResultMetadataType.SUGGESTED_PRICE,
			ResultMetadataType.ERROR_CORRECTION_LEVEL, ResultMetadataType.POSSIBLE_COUNTRY);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyApp myapp = (MyApp) getApplication();
		myapp.setContext(this);
		initSetting();
		setContentView(R.layout.activity_capture);
		initComponent();
		initView();
	}

	/**
	 * 初始化窗口设置
	 */
	private void initSetting() {
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 保持屏幕处于点亮状态
		// window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); // 全屏
		// requestWindowFeature(Window.FEATURE_NO_TITLE); // 隐藏标题栏
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 竖屏
	}

	/**
	 * 初始化功能组件
	 */
	private void initComponent() {
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		beepManager = new BeepManager(this);
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		currentState = this.mSharedPreferences.getString("currentState", "qrcode");
		cameraManager = new CameraManager(getApplication());
	}

	/**
	 * 初始化视图
	 */
	private void initView() {

		surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		// resultView = findViewById(R.id.result_view);
		// scanTextView = (TextView) findViewById(R.id.mtextview_title);
		statusView = (TextView) findViewById(R.id.status_view);

	}

	/**
	 * 初始设置扫描类型（最后一次使用类型）
	 */
	private void setScanType() {
		viewfinderView.setVisibility(View.VISIBLE);
		qrcodeSetting();
		statusView.setText(R.string.scan_qrcode);
	}

	/**
	 * 主要对相机进行初始化工作
	 */
	@Override
	protected void onResume() {
		super.onResume();
		inactivityTimer.onActivity();
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);
		surfaceHolder = surfaceView.getHolder();
		setScanType();
		resetStatusView();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			// 如果SurfaceView已经渲染完毕，会回调surfaceCreated，在surfaceCreated中调用initCamera()
			surfaceHolder.addCallback(this);
		}
		// 加载声音配置，其实在BeemManager的构造器中也会调用该方法，即在onCreate的时候会调用一次
		beepManager.updatePrefs();
		// 恢复活动监控器
		inactivityTimer.onResume();
	}

	/**
	 * 展示状态视图和扫描窗口，隐藏结果视图
	 */
	private void resetStatusView() {
		// resultView.setVisibility(View.GONE);
		statusView.setVisibility(View.GONE);
		viewfinderView.setVisibility(View.VISIBLE);
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}

	/**
	 * 初始化摄像头。打开摄像头，检查摄像头是否被开启及是否被占用
	 * 
	 * @param surfaceHolder
	 */
	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager.isOpen()) {
			Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
			return;
		}
		try {
			cameraManager.openDriver(surfaceHolder);
			// Creating the mHandler starts the preview, which can also throw a
			// RuntimeException.
			if (mHandler == null) {
				mHandler = new CaptureActivityHandler(this, decodeFormats, characterSet, cameraManager);
			}
			// decodeOrStoreSavedBitmap(null, null);
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
			displayFrameworkBugMessageAndExit();
		} catch (RuntimeException e) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.w(TAG, "Unexpected error initializing camera", e);
			displayFrameworkBugMessageAndExit();
		}
	}

	/**
	 * 若摄像头被占用或者摄像头有问题则跳出提示对话框
	 */
	private void displayFrameworkBugMessageAndExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.launcher_icon);
		builder.setTitle(getString(R.string.app_name));
		builder.setMessage(getString(R.string.msg_camera_framework_bug));
		builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
		builder.setOnCancelListener(new FinishListener(this));
		builder.show();
	}

	/**
	 * 暂停活动监控器,关闭摄像头
	 */
	@Override
	protected void onPause() {
		if (mHandler != null) {
			mHandler.quitSynchronously();
			mHandler = null;
		}
		// 暂停活动监控器
		inactivityTimer.onPause();
		// 关闭摄像头
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		super.onPause();
	}

	/**
	 * 停止活动监控器,保存最后选中的扫描类型
	 */
	@Override
	protected void onDestroy() {
		// 停止活动监控器
		inactivityTimer.shutdown();
		saveScanTypeToSp();
		super.onDestroy();
	}

	/**
	 * 保存退出进程前选中的二维码条形码的状态
	 */
	private void saveScanTypeToSp() {
		SharedPreferences.Editor localEditor = this.mSharedPreferences.edit();
		localEditor.putString("currentState", CaptureActivity.currentState);
		localEditor.commit();
	}

	/**
	 * 获取扫描结果
	 * 
	 * @param rawResult
	 * @param barcode
	 * @param scaleFactor
	 */
	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
		inactivityTimer.onActivity();

		boolean fromLiveScan = barcode != null;
		if (fromLiveScan) {

			// Then not from history, so beep/vibrate and we have an image to
			// draw on
			beepManager.playBeepSoundAndVibrate();
			drawResultPoints(barcode, scaleFactor, rawResult);
		}
		DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		Map<ResultMetadataType, Object> metadata = rawResult.getResultMetadata();
		StringBuilder metadataText = new StringBuilder(20);
		if (metadata != null) {
			for (Map.Entry<ResultMetadataType, Object> entry : metadata.entrySet()) {
				if (DISPLAYABLE_METADATA_TYPES.contains(entry.getKey())) {
					metadataText.append(entry.getValue()).append('\n');
				}
			}
			if (metadataText.length() > 0) {
				metadataText.setLength(metadataText.length() - 1);
			}
		}
		String[] r = rawResult.getText().split("\\|");
		if (r[0].equals("fileplore")) {
			
			WifiManager wifiManager = (WifiManager) CaptureActivity.this.getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = wifiManager.getConnectionInfo();
			WifiConfiguration hpc = HPaConnector.getInstance(CaptureActivity.this).getWifiApConfiguration();
			if (info != null) {
			//	Toast.makeText(this, r[1]+" info.getSSID() "+info.getSSID()+" hpc.SSID "+hpc.SSID, Toast.LENGTH_SHORT).show();
				//Log.e("ssid", info.SSID());
			
				
				if (info.getSSID().equals(r[1]) || info.getSSID().equals('"' + r[1] + '"')||hpc.SSID.equals('"' + r[1] + '"')||r[1].equals('"' + hpc.SSID + '"')) {
					ArrayList<String> qrlist = new ArrayList<String>();
					for (int i = 2; i < r.length; i++) {

						qrlist.add(r[i]);
						Log.e("resultString", r[i]);
					}
					Intent intent = new Intent();
					intent.putStringArrayListExtra("pushList", qrlist);
					intent.setClass(CaptureActivity.this, ShowPushFileActivity.class);
					startActivity(intent);
					finish();
				}

				else if (info.getSSID().equals("<unknown ssid>") || info.getSSID().equals("0x")) {
					Toast.makeText(this, r[1],Toast.LENGTH_SHORT).show();;
					if (r[1].equals("fileplore")) {
						Log.e("<unknown ssid>", "<unknown ssid>");
						WifiAutoConnectManager wificonnect = new WifiAutoConnectManager(wifiManager);
						wificonnect.connect("fileplore", "12345678", WifiCipherType.WIFICIPHER_WPA);
						int j =0;
						while (j<50){
							++j;
							try {
								Thread.sleep(300);
								if(wifiManager.getConnectionInfo().getSSID().equals('"' + "fileplore" + '"')){
									ArrayList<String> qrlist = new ArrayList<String>();
									for (int i = 2; i < r.length; i++) {

										qrlist.add(r[i]);
										Log.e("resultString "+i, r[i]);
									}
									Intent intent = new Intent();
									intent.putStringArrayListExtra("pushList", qrlist);
									intent.setClass(CaptureActivity.this, ShowPushFileActivity.class);
									startActivity(intent);
									finish();
									break;
								}
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							
						}
							
						
					}
					else {
						Toast.makeText(CaptureActivity.this, "不在同一局域网", Toast.LENGTH_SHORT).show();
					}

				}
			} else if (r[1].equals("fileplore")) {
				Log.e("null", "null");
				WifiAutoConnectManager wificonnect = new WifiAutoConnectManager(wifiManager);
				wificonnect.connect("fileplore", "12345678", WifiCipherType.WIFICIPHER_WPA);
				int j =0;
				while (j<50){
					++j;
					try {
						Thread.sleep(300);
						if(wifiManager.getConnectionInfo().getSSID().equals('"' + "fileplore" + '"')){
							ArrayList<String> qrlist = new ArrayList<String>();
							for (int i = 2; i < r.length; i++) {

								qrlist.add(r[i]);
								Log.e("resultString", r[i]);
							}
							Intent intent = new Intent();
							intent.putStringArrayListExtra("pushList", qrlist);
							intent.setClass(CaptureActivity.this, ShowPushFileActivity.class);
							startActivity(intent);
							finish();
							break;
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}
					
				
			}

		} else {
			Toast.makeText(CaptureActivity.this, "不支持此二维码", Toast.LENGTH_SHORT).show();
			finish();
		}

	}

	/**
	 * 在扫描图片结果中绘制绿色的点
	 * 
	 * @param barcode
	 * @param scaleFactor
	 * @param rawResult
	 */
	private void drawResultPoints(Bitmap barcode, float scaleFactor, Result rawResult) {
		ResultPoint[] points = rawResult.getResultPoints();
		if (points != null && points.length > 0) {
			Canvas canvas = new Canvas(barcode);
			Paint paint = new Paint();
			paint.setColor(getResources().getColor(R.color.result_points));
			if (points.length == 2) {
				paint.setStrokeWidth(4.0f);
				drawLine(canvas, paint, points[0], points[1], scaleFactor);
			} else if (points.length == 4 && (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A
					|| rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
				drawLine(canvas, paint, points[0], points[1], scaleFactor);
				drawLine(canvas, paint, points[2], points[3], scaleFactor);
			} else {
				paint.setStrokeWidth(10.0f);
				for (ResultPoint point : points) {
					if (point != null) {
						canvas.drawPoint(scaleFactor * point.getX(), scaleFactor * point.getY(), paint);
					}
				}
			}
		}
	}

	/**
	 * 在扫描图片结果中绘制绿色的线
	 * 
	 * @param canvas
	 * @param paint
	 * @param a
	 * @param b
	 * @param scaleFactor
	 */
	private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b, float scaleFactor) {
		if (a != null && b != null) {
			canvas.drawLine(scaleFactor * a.getX(), scaleFactor * a.getY(), scaleFactor * b.getX(),
					scaleFactor * b.getY(), paint);
		}
	}

	/**
	 * 点击响应二维码扫描
	 */

	private void qrcodeSetting() {
		decodeFormats = new Vector<BarcodeFormat>(2);
		decodeFormats.clear();
		decodeFormats.add(BarcodeFormat.QR_CODE);
		decodeFormats.add(BarcodeFormat.DATA_MATRIX);
		// scanTextView.setText(R.string.scan_qr);
		if (null != mHandler) {
			mHandler.setDecodeFormats(decodeFormats);
		}

		viewfinderView.refreshDrawableState();
		cameraManager.setManualFramingRect(450, 450);
		viewfinderView.refreshDrawableState();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
			Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	/**
	 * 闪光灯调节器。自动检测环境光线强弱并决定是否开启闪光灯
	 */
	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return mHandler;
	}

	public CameraManager getCameraManager() {
		return cameraManager;
	}

	/**
	 * 在经过一段延迟后重置相机以进行下一次扫描。 成功扫描过后可调用此方法立刻准备进行下次扫描
	 * 
	 * @param delayMS
	 */
	public void restartPreviewAfterDelay(long delayMS) {
		if (mHandler != null) {
			mHandler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
		}
		resetStatusView();
	}
	//
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// switch (keyCode) {
	// case KeyEvent.KEYCODE_BACK: // 拦截返回键
	//
	// restartPreviewAfterDelay(0L);
	// return true;
	// }
	// return super.onKeyDown(keyCode, event);
	// }

}
