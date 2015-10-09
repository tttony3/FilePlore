package com.changhong.fileplore.activities;

import com.changhong.fileplore.R;
import com.changhong.fileplore.application.MyApp;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoActivity extends Activity  {  
    String uri;
    /** Called when the activity is first created. */  
//    @Override  
//    public void onCreate(Bundle savedInstanceState) {
//    	Intent intent =getIntent();
//    	 uri =intent.getStringExtra("uri");
//        super.onCreate(savedInstanceState);  
//        setContentView(R.layout.video_player);  
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);  
//        surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView1);  
//  
//        btnPlayUrl = (Button) this.findViewById(R.id.btnPlayUrl);  
//        btnPlayUrl.setOnClickListener(new ClickEvent());  
//  
//        btnPause = (Button) this.findViewById(R.id.btnPause);  
//        btnPause.setOnClickListener(new ClickEvent());  
//  
//        btnStop = (Button) this.findViewById(R.id.btnStop);  
//        btnStop.setOnClickListener(new ClickEvent());  
//  
//        skbProgress = (SeekBar) this.findViewById(R.id.skbProgress);  
//        skbProgress.setOnSeekBarChangeListener(new SeekBarChangeEvent());  
//        player = new Player(surfaceView, skbProgress);  
//  
//    }  
//  
//    class ClickEvent implements OnClickListener {  
//  
//        @Override  
//        public void onClick(View arg0) {  
//            if (arg0 == btnPause) {  
//                player.pause();  
//            } else if (arg0 == btnPlayUrl) {  
//             //   String url="http://daily3gp.com/vids/family_guy_penis_car.3gp";  
//                player.playUrl(uri);  
//            } else if (arg0 == btnStop) {  
//                player.stop();  
//            }  
//  
//        }  
//    }  
//  
//    class SeekBarChangeEvent implements SeekBar.OnSeekBarChangeListener {  
//        int progress;  
//  
//        @Override  
//        public void onProgressChanged(SeekBar seekBar, int progress,  
//                boolean fromUser) {  
//            // 原本是(progress/seekBar.getMax())*player.mediaPlayer.getDuration()  
//            this.progress = progress * player.mediaPlayer.getDuration()  
//                    / seekBar.getMax();  
//        }  
//  
//        @Override  
//        public void onStartTrackingTouch(SeekBar seekBar) {  
//  
//        }  
//  
//        @Override  
//        public void onStopTrackingTouch(SeekBar seekBar) {  
//            // seekTo()的参数是相对与影片时间的数字，而不是与seekBar.getMax()相对的数字  
//            player.mediaPlayer.seekTo(progress);  
//        }  
//    }  
//  
//}  
//
	private VideoView video1;
	MediaController mediaco;
//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		MyApp myapp = (MyApp) getApplication();
		myapp.setContext(this);
		Intent intent = getIntent();
		String uri =intent.getStringExtra("uri");
		setContentView(R.layout.activity_video);
		video1 = (VideoView) findViewById(R.id.video1);
		
		mediaco = new MediaController(this);
		// File file = new File("/mnt/sdcard/通话录音/1.mp4");

		// VideoView与MediaController进行关联
		video1.setVideoURI(Uri.parse(uri));		
		video1.setMediaController(mediaco);
		mediaco.setAnchorView(video1);
		mediaco.setMediaPlayer(video1);
		mediaco.setVisibility(View.VISIBLE);
		video1.requestFocus();		
		video1.start();

	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}

}