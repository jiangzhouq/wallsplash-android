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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
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

public class SimpleStreamPlayerActivity extends FragmentActivity implements PFAssetObserver, OnSeekBarChangeListener {

	PFView				_pfview;
	PFAsset 			_pfasset;
    PFNavigationMode 	_currentNavigationMode = PFNavigationMode.TOUCH;

	boolean 			_updateThumb = true;;
	boolean				_choose_resolution = false;
    Timer 				_scrubberMonitorTimer;

    ViewGroup 			_frameContainer;
	Button				_stopButton;
	Button				_playButton;
	Button				_touchButton;
	Button				_resolution;
	Button				_resolution_1080;
	Button				_resolution_720;
	Button				_resolution_640;
	Button				_resolution_origin;
	SeekBar				_scrubber;
	//0 for not ready, 1 for ready, 2 for , 3 for playing, 4 for stoped
	private int state = 0;
	private String uuurl_www = "";
	private String uuurl_name = "";
	private ProgressBar _loadingProgress;
	/**
	 * Creation and initalization of the Activitiy.
	 * Initializes variables, listeners, and starts request of a movie list.
	 *
	 * @param  savedInstanceState  a saved instance of the Bundle
	 */
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
				case 0:
					_loadingProgress.setVisibility(View.GONE);
					break;
				case 1:
					_loadingProgress.setVisibility(View.VISIBLE);
					break;
			}
		}
	};
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main_panframe);

        _frameContainer = (ViewGroup) findViewById(R.id.framecontainer);
        _frameContainer.setBackgroundColor(0xFF000000);

		_playButton = (Button)findViewById(R.id.playbutton);
		_stopButton = (Button)findViewById(R.id.stopbutton);
		_touchButton = (Button)findViewById(R.id.touchbutton);
		_resolution = (Button)findViewById(R.id.resolution);
		_resolution_1080 = (Button)findViewById(R.id.resolution_1080);
		_resolution_720 = (Button)findViewById(R.id.resolution_720);
		_resolution_640 = (Button)findViewById(R.id.resolution_640);
		_resolution_origin = (Button)findViewById(R.id.resolution_origin);
		_scrubber = (SeekBar)findViewById(R.id.scrubber);
		_playButton.setClickable(false);
		_playButton.setOnClickListener(playListener);
		_stopButton.setOnClickListener(stopListener);
		_touchButton.setOnClickListener(touchListener);
		_scrubber.setOnSeekBarChangeListener(this);
		_resolution.setOnClickListener(resolutionListener);
		_resolution_1080.setOnClickListener(resolution1080pListener);
		_resolution_720.setOnClickListener(resolution720pListener);
		_resolution_640.setOnClickListener(resolution640pListener);
		_resolution_origin.setOnClickListener(resolutionOriginListener);
		_loadingProgress = (ProgressBar) findViewById(R.id.video_progress);

		_scrubber.setEnabled(false);

		String[] splitStrings = getIntent().getStringExtra("url").split("/");
		uuurl_www = "http://view.iyun720.com/";
		uuurl_name = splitStrings[splitStrings.length - 1].replace("short_", "");
		loadVideo(uuurl_www + "1280_" + uuurl_name);
//		loadVideo("http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8");

		showControls(true);

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
		_resolution_1080.setVisibility(View.GONE);
		_resolution_640.setVisibility(View.GONE);
		_resolution_720.setVisibility(View.GONE);
		_resolution_origin.setVisibility(View.GONE);
		_loadingProgress.setVisibility(visibility);
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
		Log.d("qiqi",filename);
		_pfview = PFObjectFactory.view(this);
		_pfasset = PFObjectFactory.assetFromUri(this, Uri.parse("http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8"), this);

        _pfview.displayAsset(_pfasset);
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
	private float last_playback = 0;
	public void onStatusMessage(final PFAsset asset, PFAssetStatus status) {
		Log.d("qiqi", "changed");
		switch (status)
		{
			case LOADED:

				Log.d("SimplePlayer", "Loaded");
				_playButton.setClickable(true);
				_pfasset.play();
				break;
			case DOWNLOADING:
				Log.d("SimplePlayer", "Downloading 360� movie: "+_pfasset.getDownloadProgress()+" percent complete");
				break;
			case DOWNLOADED:
				Log.d("SimplePlayer", "Downloaded to "+asset.getUrl());
				break;
			case DOWNLOADCANCELLED:
				Log.d("SimplePlayer", "Download cancelled");
				break;
			case PLAYING:
				handler.sendEmptyMessage(0);
				Log.d("SimplePlayer", "Playing");
		        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				_scrubber.setEnabled(true);
				_playButton.setText("pause");
				_scrubberMonitorTimer = new Timer();
				final TimerTask task = new TimerTask() {
					public void run() {
						if (_updateThumb)
						{

							Log.d("qiqi", "_scrubber.getProgress():" + _scrubber.getProgress() + " asset.getPlaybackTime():" + asset.getPlaybackTime());
							if(asset.getPlaybackTime() == last_playback){
								if(_loadingProgress.getVisibility() == View.GONE)
									handler.sendEmptyMessage(1);
							}else{
								if(_loadingProgress.getVisibility() == View.VISIBLE)
									handler.sendEmptyMessage(0);
							}
							last_playback = asset.getPlaybackTime();
							_scrubber.setMax((int) asset.getDuration());
							_scrubber.setProgress((int) asset.getPlaybackTime());
						}
					}
				};
				_scrubberMonitorTimer.schedule(task, 0, 500);
				break;
			case PAUSED:
				Log.d("SimplePlayer", "Paused");
				_playButton.setText("play");
				break;
			case STOPPED:
				Log.d("SimplePlayer", "Stopped");
				_playButton.setText("play");
				if(_scrubberMonitorTimer != null)
					_scrubberMonitorTimer.cancel();
				_scrubberMonitorTimer = null;
				_scrubber.setProgress(0);
				_scrubber.setEnabled(false);
		        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				break;
			case COMPLETE:
				Log.d("SimplePlayer", "Complete");
				_playButton.setText("play");
				if(_scrubberMonitorTimer != null)
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
			if (_pfasset.getStatus() == PFAssetStatus.PLAYING)
			{
				_pfasset.pause();
			}
			else
				_pfasset.play();
		}
	};

	/**
	 * Click listener for the stop/back button
	 *
	 */
	private OnClickListener stopListener = new OnClickListener() {
		public void onClick(View v) {
			_pfasset.stop();
		}
	};

	private OnClickListener resolutionListener = new OnClickListener() {
		public void onClick(View v) {
			if(_choose_resolution){
				_choose_resolution = false;
				_resolution_1080.setVisibility(View.GONE);
				_resolution_640.setVisibility(View.GONE);
				_resolution_720.setVisibility(View.GONE);
				_resolution_origin.setVisibility(View.GONE);
			}else{
				_choose_resolution = true;
				_resolution_1080.setVisibility(View.VISIBLE);
				_resolution_640.setVisibility(View.VISIBLE);
				_resolution_720.setVisibility(View.VISIBLE);
				_resolution_origin.setVisibility(View.VISIBLE);
			}
		}
	};
	private void hideResolutions(){
		_choose_resolution = false;
		_resolution_1080.setVisibility(View.GONE);
		_resolution_640.setVisibility(View.GONE);
		_resolution_720.setVisibility(View.GONE);
		_resolution_origin.setVisibility(View.GONE);
	}
	private OnClickListener resolution1080pListener = new OnClickListener() {
		public void onClick(View v) {
			hideResolutions();
			handler.sendEmptyMessage(1);
			float dur = _pfasset.getPlaybackTime();
			if (_pfasset != null)
			{
				_pfasset.stop();
				_pfasset.release();
			}
			if(_pfview != null){
				_pfview.release();
			}
			_frameContainer.removeViewAt(0);
			loadVideo(uuurl_www + "1920_" + uuurl_name);
			_pfasset.setPLaybackTime(dur);
		}
	};

	private OnClickListener resolution720pListener = new OnClickListener() {
		public void onClick(View v) {
			hideResolutions();
			handler.sendEmptyMessage(1);
			float dur = _pfasset.getPlaybackTime();
			if (_pfasset != null)
			{
				_pfasset.stop();
				_pfasset.release();
			}
			if(_pfview != null){
				_pfview.release();
			}
			_frameContainer.removeViewAt(0);
			loadVideo(uuurl_www + "1280_" + uuurl_name);
			_pfasset.setPLaybackTime(dur);
		}
	};

	private OnClickListener resolution640pListener = new OnClickListener() {
		public void onClick(View v) {
			hideResolutions();
			handler.sendEmptyMessage(1);
			float dur = _pfasset.getPlaybackTime();
			if (_pfasset != null)
			{
				_pfasset.stop();
				_pfasset.release();
			}
			if(_pfview != null){
				_pfview.release();
			}
			_frameContainer.removeViewAt(0);
			loadVideo(uuurl_www + "854_" + uuurl_name);
			_pfasset.setPLaybackTime(dur);
		}
	};
	private OnClickListener resolutionOriginListener = new OnClickListener() {
		public void onClick(View v) {
			hideResolutions();
			handler.sendEmptyMessage(1);
			float dur = _pfasset.getPlaybackTime();
			if (_pfasset != null)
			{
				_pfasset.stop();
				_pfasset.release();
			}
			if(_pfview != null){
				_pfview.release();
			}
			_frameContainer.removeViewAt(0);
			loadVideo(uuurl_www + uuurl_name);
			_pfasset.setPLaybackTime(dur);
		}
	};
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (_pfasset != null)
		{
			_pfasset.stop();
			_pfasset.release();
		}
		if(_pfview != null){
			_pfview.release();
		}
	}

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
        if (_pfasset != null)
        {
	        if (_pfasset.getStatus() == PFAssetStatus.PLAYING)
	        	_pfasset.pause();
        }
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
				showControls(true);
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
				showControls(false);
				_currentNavigationMode = PFNavigationMode.MOTION;
				_touchButton.setText("motion");
				_pfview.setNavigationMode(_currentNavigationMode);
				_pfview.handleOrientationChange();
			}
		}
	}
}
