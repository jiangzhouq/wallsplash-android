/*
 * SimpleStreamPlayer
 * Android example of Panframe library
 * The example plays back an panoramic movie from a resource.
 *
 * (c) 2012-2013 Mindlight. All rights reserved.
 * Visit www.panframe.com for more information.
 *
 */

package com.iyun.unsplash.activities;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.iyun.unsplash.R;
import com.panframe.android.lib.PFAsset;
import com.panframe.android.lib.PFAssetObserver;
import com.panframe.android.lib.PFAssetStatus;
import com.panframe.android.lib.PFNavigationMode;
import com.panframe.android.lib.PFObjectFactory;
import com.panframe.android.lib.PFView;

import java.util.Timer;
import java.util.TimerTask;

public class SimplePicPlayerActivity extends AppCompatActivity implements PFAssetObserver, OnSeekBarChangeListener {

	PFView				_pfview;
//	PFAsset 			_pfasset;
    PFNavigationMode 	_currentNavigationMode = PFNavigationMode.TOUCH;

	boolean 			_updateThumb = true;;
    Timer 				_scrubberMonitorTimer;

    ViewGroup 			_frameContainer;
	Button				_stopButton;
	Button				_playButton;
	Button				_resolution;
	Button				_touchButton;
	SeekBar				_scrubber;
	Bitmap bitmapTmp;
	//0 for not ready, 1 for ready, 2 for , 3 for playing, 4 for stoped
	private int state = 0;
	/**
	 * Creation and initalization of the Activitiy.
	 * Initializes variables, listeners, and starts request of a movie list.
	 *
	 * @param  savedInstanceState  a saved instance of the Bundle
	 */
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main_panframe_pic);
		Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
		toolbar.setTitleTextColor(Color.WHITE);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		toolbar.setNavigationOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				Log.d("qiqi", "back pressed");
				onBackPressed();
			}
		});

		_frameContainer = (ViewGroup) findViewById(R.id.framecontainer);
        _frameContainer.setBackgroundColor(0xFF000000);

		_playButton = (Button)findViewById(R.id.playbutton);
		_stopButton = (Button)findViewById(R.id.stopbutton);
		_touchButton = (Button)findViewById(R.id.touchbutton);
		_resolution = (Button)findViewById(R.id.resolution);
		_scrubber = (SeekBar)findViewById(R.id.scrubber);

		_playButton.setOnClickListener(playListener);
		_stopButton.setOnClickListener(stopListener);
		_touchButton.setOnClickListener(touchListener);
		_scrubber.setOnSeekBarChangeListener(this);

		_scrubber.setEnabled(false);

//		loadVideo("http://view.iyun720.com/iyun720_1450764126000_92688495.mp4");
		loadVideo(getIntent().getStringExtra("url"));


		showControls(false);

	}
	/**
	 * Show/Hide the playback controls
	 *
	 * @param  bShow  Show or hide the controls. Pass either true or false.
	 */
    public void showControls(boolean bShow)
    {
    	int visibility = View.GONE;

    	if (bShow)
    		visibility = View.VISIBLE;

		_playButton.setVisibility(visibility);
		_stopButton.setVisibility(View.GONE);
		_touchButton.setVisibility(View.GONE);
		_scrubber.setVisibility(visibility);
		_resolution.setVisibility(visibility);
		if (_pfview != null)
		{
			if (!_pfview.supportsNavigationMode(PFNavigationMode.MOTION))
				_touchButton.setVisibility(View.GONE);
		}
    }


	/**
	 * Start the video with a local file path
	 *
	 * @param  filename  The file path on device storage
	 */
    public void loadVideo(String filename)
    {

		_pfview = PFObjectFactory.view(this);
//		_pfasset = PFObjectFactory.assetFromUri(this, Uri.parse(filename), this);
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(filename, options);
//			bitmapTmp = BitmapFactory.decodeStream(is);
			Log.d("qiqi", "options.outHeight:" + options.outHeight + " options.outWidth:" + options.outWidth);
			options.inSampleSize = options.outHeight/1024;
			options.inJustDecodeBounds = false;
			bitmapTmp = BitmapFactory.decodeFile(filename, options);
			bitmapTmp = Bitmap.createScaledBitmap(bitmapTmp, 4096, 2048,true);
//			int dstHeight = 0;
//			for(int i = 0; i < 14 ; i ++){
//				if (options.outHeight > Math.pow( 2, i) && options.outHeight < Math.pow( 2, i + 1)){
//					dstHeight = (int)Math.pow( 2, i);
//				}
//			}
////			double dstWidth = dstHeight/2;
////			Log.d("qiqi","dstWidth:" + dstWidth + " dstHeight:" + dstHeight);
////			options.inSampleSize = 3;
//			if(dstHeight > 2048){
//				dstHeight = 2048;
//			}else{
//				dstHeight = 1024;
//			}
//			options.outWidth = dstHeight*2;
//			options.outHeight = dstHeight;
//			Log.d("qiqi","dstWidth:" + dstHeight*2 + " dstHeight:" + dstHeight);
//			options.inJustDecodeBounds = false;
//			bitmapTmp = BitmapFactory.decodeFile(filename, options);
			_pfview.injectImage(bitmapTmp);
		} finally {
//			try {
//				is.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		}
//        _pfview.displayAsset(_pfasset);
        _pfview.setNavigationMode(_currentNavigationMode);
//		_pfview.handleOrientationChange();
        _frameContainer.addView(_pfview.getView(), 0);

    }

	/**
	 * Status callback from the PFAsset instance.
	 * Based on the status this function selects the appropriate action.
	 *
	 * @param  asset  The asset who is calling the function
	 * @param  status The current status of the asset.
	 */
	public void onStatusMessage(final PFAsset asset, PFAssetStatus status) {
		switch (status)
		{
			case LOADED:
				Log.d("SimplePlayer", "Loaded");
				break;
			case DOWNLOADING:
//				Log.d("SimplePlayer", "Downloading 360� movie: "+_pfasset.getDownloadProgress()+" percent complete");
				break;
			case DOWNLOADED:
				Log.d("SimplePlayer", "Downloaded to "+asset.getUrl());
				break;
			case DOWNLOADCANCELLED:
				Log.d("SimplePlayer", "Download cancelled");
				break;
			case PLAYING:
				Log.d("SimplePlayer", "Playing");
		        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				_scrubber.setEnabled(true);
				_playButton.setText("pause");
				_scrubberMonitorTimer = new Timer();
				final TimerTask task = new TimerTask() {
					public void run() {
						if (_updateThumb)
						{
							_scrubber.setMax((int) asset.getDuration());
							_scrubber.setProgress((int) asset.getPlaybackTime());
						}
					}
				};
				_scrubberMonitorTimer.schedule(task, 0, 33);
				break;
			case PAUSED:
				Log.d("SimplePlayer", "Paused");
				_playButton.setText("play");
				break;
			case STOPPED:
				Log.d("SimplePlayer", "Stopped");
				_playButton.setText("play");
				_scrubberMonitorTimer.cancel();
				_scrubberMonitorTimer = null;
				_scrubber.setProgress(0);
				_scrubber.setEnabled(false);
		        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				break;
			case COMPLETE:
				Log.d("SimplePlayer", "Complete");
				_playButton.setText("play");
				_scrubberMonitorTimer.cancel();
				_scrubberMonitorTimer = null;
		        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				break;
			case ERROR:
				Log.d("SimplePlayer", "Error");
				break;
		}
	}

	/**
	 * Click listener for the play/pause button
	 *
	 */
	private OnClickListener playListener = new OnClickListener() {
		public void onClick(View v) {
//			if (_pfasset.getStatus() == PFAssetStatus.PLAYING)
//			{
//				_pfasset.pause();
//			}
//			else
//				_pfasset.play();
		}
	};

	/**
	 * Click listener for the stop/back button
	 *
	 */
	private OnClickListener stopListener = new OnClickListener() {
		public void onClick(View v) {
//			_pfasset.stop();
		}
	};

	/**
	 * Click listener for the navigation mode (touch/motion (if available))
	 *
	 */
	private OnClickListener touchListener = new OnClickListener() {
		public void onClick(View v) {
			if (_pfview != null)
			{
				Button touchButton = (Button)findViewById(R.id.touchbutton);
				if (_currentNavigationMode == PFNavigationMode.TOUCH)
				{
					_currentNavigationMode = PFNavigationMode.MOTION;
					touchButton.setText("motion");
				}
				else
				{
					_currentNavigationMode = PFNavigationMode.TOUCH;
					touchButton.setText("touch");
				}
				_pfview.setNavigationMode(_currentNavigationMode);
//				_pfview.handleOrientationChange();
			}
		}
	};

	/**
	 * Setup the options menu
	 *
	 * @param menu The options menu
	 */
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	/**
	 * Called when pausing the app.
	 * This function pauses the playback of the asset when it is playing.
	 *
	 */
    public void onPause() {
        super.onPause();
//        if (_pfasset != null)
//        {
//	        if (_pfasset.getStatus() == PFAssetStatus.PLAYING)
//	        	_pfasset.pause();
//        }
    }

	/**
	 * Called when a previously created loader is being reset, and thus making its data unavailable.
	 *
	 * @param seekbar The SeekBar whose progress has changed
	 * @param progress The current progress level.
	 * @param fromUser True if the progress change was initiated by the user.
	 *
	 */
	public void onProgressChanged (SeekBar seekbar, int progress, boolean fromUser) {
	}

	/**
	 * Notification that the user has started a touch gesture.
	 * In this function we signal the timer not to update the playback thumb while we are adjusting it.
	 *
	 * @param seekbar The SeekBar in which the touch gesture began
	 *
	 */
	public void onStartTrackingTouch(SeekBar seekbar) {
		_updateThumb = false;
	}

	/**
	 * Notification that the user has finished a touch gesture.
	 * In this function we request the asset to seek until a specific time and signal the timer to resume the update of the playback thumb based on playback.
	 *
	 * @param seekbar The SeekBar in which the touch gesture began
	 *
	 */
	public void onStopTrackingTouch(SeekBar seekbar) {
		_updateThumb = true;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.d("qiqi", "newConfig");
		if(newConfig.orientation==Configuration.ORIENTATION_PORTRAIT){
			Log.d("qiqi", "现在是竖屏");
//			Toast.makeText(MainActivity.this, "现在是竖屏", Toast.LENGTH_SHORT).show();
			if(_pfview != null){
				_pfview.setMode(0,1);
				_currentNavigationMode = PFNavigationMode.TOUCH;
				_touchButton.setText("touch");
				_pfview.setNavigationMode(_currentNavigationMode);
				_pfview.handleOrientationChange();
			}
		}
		if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
//			Toast.makeText(MainActivity.this, "现在是横屏", Toast.LENGTH_SHORT).show();
			Log.d("qiqi", "现在是横屏");

			if(_pfview != null){
				_pfview.setMode(2,1);
				_currentNavigationMode = PFNavigationMode.MOTION;
				_touchButton.setText("motion");
				_pfview.setNavigationMode(_currentNavigationMode);
				_pfview.handleOrientationChange();
			}
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(bitmapTmp != null && ! bitmapTmp.isRecycled())
			bitmapTmp.recycle();
	}
}
