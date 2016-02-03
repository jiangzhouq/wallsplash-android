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

import android.animation.Animator;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.iyun.unsplash.R;
import com.iyun.unsplash.models.Image;
import com.iyun.unsplash.other.CustomAnimatorListener;
import com.iyun.unsplash.other.Utils;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.koushikdutta.ion.future.ResponseFuture;
import com.panframe.android.lib.PFAsset;
import com.panframe.android.lib.PFAssetObserver;
import com.panframe.android.lib.PFAssetStatus;
import com.panframe.android.lib.PFHotspot;
import com.panframe.android.lib.PFNavigationMode;
import com.panframe.android.lib.PFObjectFactory;
import com.panframe.android.lib.PFView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class SimplePicPlayerActivity extends AppCompatActivity implements PFAssetObserver, OnSeekBarChangeListener {

	PFView				_pfview;
	PFAsset 			_pfasset;
    PFNavigationMode 	_currentNavigationMode = PFNavigationMode.TOUCH;

	boolean 			_updateThumb = true;;
    Timer 				_scrubberMonitorTimer;
	//All
    ViewGroup 			_frameContainer;
	RelativeLayout _controlerContainer;
	//video progress bar
	private ProgressBar _loadingProgress;
	LinearLayout _imageList;
	TextView _infoText;
	//
	LinearLayout _resolutionChooser;
	Button _1080p;
	Button _720p;
	Button _640p;
	//
	LinearLayout _videoControler;
	Button				_playButton;
	Button				_resolution;
	SeekBar				_scrubber;
	//
	LinearLayout _controler;
	Button _music;
	Button _more;
	ImageButton _info;

	ImageView mRightIyun;
	Bitmap bitmapTmp;

	private String curLoadUrl;
	//avialible for controler , videocontroler , resolution , imagelist , info
	private boolean[] CUR_STATE ;
	private boolean[] STATE_PIC = new boolean[]{true,false,false,false,false,false};
	private boolean[] STATE_PIC_IMAGELIST = new boolean[]{true,false,false,true,false,false};
	private boolean[] STATE_PIC_INFO = new boolean[]{true,false,false,false,true,false};
	private boolean[] STATE_VID = new boolean[]{true,true,false,false,false,false};
	private boolean[] STATE_VID_RESO = new boolean[]{true,true,true,false,false,false};
	private boolean[] STATE_VID_IMAGELIST = new boolean[]{true,true,false,true,false,false};
	private boolean[] STATE_VID_INFO = new boolean[]{true,true,false,false,true,false};
	private boolean[] STATE_NONE = new boolean[]{false,false,false,false,false,false};

	private ArrayList<Image> picList;
	//0 for not ready, 1 for ready, 2 for , 3 for playing, 4 for stoped
	private int state = 0;

	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
				case 0:
					loadVideo(msg.getData().getString("url"));
					break;
				case 1:
					break;
				case 2:
					break;
			}
		}
	};
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
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("qiqi", "back pressed");
				onBackPressed();
			}
		});

		_frameContainer = (ViewGroup) findViewById(R.id.framecontainer);
        _frameContainer.setBackgroundColor(0xFF000000);
		_controlerContainer = (RelativeLayout) findViewById(R.id.controler_container);
		_controlerContainer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("qiqi", "contrller container clicked");
				Random random = new Random();
				int i = random.nextInt(picList.size());
				restartPic(i);
			}
		});
		_controlerContainer.setClickable(false);
		_loadingProgress = (ProgressBar) findViewById(R.id.video_progress);
		_infoText = (TextView) findViewById(R.id.info_text);
		_imageList = (LinearLayout) findViewById(R.id.pic_list);
		DisplayMetrics displaymetrics = SimplePicPlayerActivity.this.getResources().getDisplayMetrics();
		//image.width .... image.height
		//device.width ... device
		int finalHeight = (int) (displaymetrics.widthPixels / 3);
		Log.d("qiqi", "finalHeight:" + finalHeight);
		ViewGroup.LayoutParams linearParams = (ViewGroup.LayoutParams) _imageList
				.getLayoutParams();
		linearParams.width = (int) (displaymetrics.widthPixels);
		linearParams.height = (int) (displaymetrics.widthPixels / 6);
		_imageList.setLayoutParams(linearParams);



		_resolutionChooser = (LinearLayout) findViewById(R.id.resolution_layout);

		_1080p = (Button) findViewById(R.id.resolution_1080);
		_1080p.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				float dur = _pfasset.getPlaybackTime();
//				if (_pfasset != null)
//				{
//					_pfasset.stop();
//					_pfasset.release();
//				}
//				if(_pfview != null){
//					_pfview.release();
//				}
//				_frameContainer.removeViewAt(0);
				loadVideo(curLoadUrl.replace(curLoadUrl.split("_")[0].split("/")[3], "1920"));
				_pfasset.setPLaybackTime(dur);
			}
		});
		_720p = (Button) findViewById(R.id.resolution_720);
		_720p.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				float dur = _pfasset.getPlaybackTime();
//				if (_pfasset != null)
//				{
//					_pfasset.stop();
//					_pfasset.release();
//				}
//				if(_pfview != null){
//					_pfview.release();
//				}
//				_frameContainer.removeViewAt(0);
				loadVideo(curLoadUrl.replace(curLoadUrl.split("_")[0].split("/")[3], "1280"));
				_pfasset.setPLaybackTime(dur);
			}
		});
		_640p = (Button) findViewById(R.id.resolution_640);
		_640p.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				float dur = _pfasset.getPlaybackTime();
//				if (_pfasset != null)
//				{
//					_pfasset.stop();
//					_pfasset.release();
//				}
//				if(_pfview != null){
//					_pfview.release();
//				}
//				_frameContainer.removeViewAt(0);
				loadVideo(curLoadUrl.replace(curLoadUrl.split("_")[0].split("/")[3], "854"));
				_pfasset.setPLaybackTime(dur);
			}
		});
		_videoControler = (LinearLayout) findViewById(R.id.video_controler);
		_playButton = (Button)findViewById(R.id.playbutton);
		_resolution = (Button)findViewById(R.id.resolution);
		_resolution.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (CUR_STATE.equals(STATE_VID_RESO)) {
					showControls(STATE_VID);
				} else {
					showControls(STATE_VID_RESO);
				}

			}
		});
		_scrubber = (SeekBar)findViewById(R.id.scrubber);

		_playButton.setOnClickListener(playListener);
		_scrubber.setOnSeekBarChangeListener(this);

		_scrubber.setEnabled(false);

		_controler = (LinearLayout) findViewById(R.id.controler);
		_music = (Button) findViewById(R.id.music);
		_more = (Button) findViewById(R.id.more);
		_more.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(CUR_STATE.equals(STATE_PIC)){
					showControls(STATE_PIC_IMAGELIST);
				}else if (CUR_STATE.equals(STATE_PIC_IMAGELIST)){
					showControls(STATE_PIC);
				}else if(CUR_STATE.equals(STATE_VID)){
					showControls(STATE_VID_IMAGELIST);
				}else if (CUR_STATE.equals(STATE_VID_IMAGELIST)){
					showControls(STATE_VID);
				}
			}
		});
		_info = (ImageButton) findViewById(R.id.info);
		_info.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (CUR_STATE.equals(STATE_NONE)) {
					if(_pfasset != null){
						showControls(STATE_VID);
					}else{
						showControls(STATE_PIC);

					}
				} else {
					showControls(STATE_NONE);
				}
			}
		});
		mRightIyun = (ImageView) findViewById(R.id.right_iyun);
//		loadVideo("http://view.iyun720.com/iyun720_1450764126000_92688495.mp4");
		loadVideo(getIntent().getStringExtra("url"));

		picList = (ArrayList<Image>)getIntent().getSerializableExtra("list");
		Log.d("qiqi", "picList.length:" + picList.size());
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		((RecyclerView)_imageList.getChildAt(0)).setLayoutManager(linearLayoutManager);
		((RecyclerView)_imageList.getChildAt(0)).setAdapter(new Picdapter(picList));
		showControls(STATE_NONE);

	}

	class Picdapter extends RecyclerView.Adapter<Picdapter.MyViewHolder>
	{
		public ArrayList<Image> picList;
		public Picdapter(ArrayList<Image> picList){
			this.picList = picList;
		}

		@Override
		public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
		{
			MyViewHolder holder = new MyViewHolder(LayoutInflater.from(SimplePicPlayerActivity.this).inflate(R.layout.item_pic, parent,
					false));
			return holder;
		}

		@Override
		public void onBindViewHolder(MyViewHolder holder, int position)
		{
			if(picList.get(position).getLow_resolution().contains(".mp4")){
				holder.play.setVisibility(View.VISIBLE);
			}else{
				holder.play.setVisibility(View.GONE);
			}
			Picasso.with(SimplePicPlayerActivity.this).load(picList.get(position).getThumbnail()).into(holder.pic);
			DisplayMetrics displaymetrics = SimplePicPlayerActivity.this.getResources().getDisplayMetrics();
			//image.width .... image.height
			//device.width ... device
			int finalHeight = (int) (displaymetrics.widthPixels / 3);
			Log.d("qiqi", "finalHeight:" + finalHeight);
			ViewGroup.LayoutParams linearParams = (ViewGroup.LayoutParams) holder.container
					.getLayoutParams();
			linearParams.width = (int) (displaymetrics.widthPixels / 3);
			linearParams.height = (int) (displaymetrics.widthPixels / 6);
			holder.container.setLayoutParams(linearParams);
		}

		@Override
		public int getItemCount()
		{
			return picList.size();
		}

		class MyViewHolder extends RecyclerView.ViewHolder
		{

			ImageView pic;
			ImageView play;
			RelativeLayout container;

			public MyViewHolder(View view)
			{
				super(view);
				view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						restartPic(getPosition());

					}
				});
				pic = (ImageView) view.findViewById(R.id.image);
				play = (ImageView) view.findViewById(R.id.play);
				container = (RelativeLayout) view.findViewById(R.id.image_container);
			}

		}
	}
	private void restartPic(int pos){
		if(picList.get(pos).getType() == 1){
			downloadPic(picList.get(pos).getLow_resolution());
		}else{
			Message msg = new Message();
			Bundle bundle = new Bundle();
			bundle.putString("url",picList.get(pos).getStandard_resolution());
			msg.setData(bundle);
			msg.what = 0;
			handler.sendMessage(msg);
		}
	}
	/**
	 * Show/Hide the playback controls
	 *
	 * @param  state  Show or hide the controls. Pass either true or false.
	 */
    public void showControls(boolean[] state)
    {
		CUR_STATE = state;
		_controler.setVisibility(state[0]?View.VISIBLE:View.INVISIBLE);
		_videoControler.setVisibility(state[1]?View.VISIBLE:View.GONE);
		_resolutionChooser.setVisibility(state[2]?View.VISIBLE:View.GONE);
		_imageList.setVisibility(state[3]?View.VISIBLE:View.GONE);
		_infoText.setVisibility(state[4]?View.VISIBLE:View.GONE);
		_loadingProgress.setVisibility(state[5]?View.VISIBLE:View.GONE);
    }


	/**
	 * Start the video with a local file path
	 *
	 * @param  filename  The file path on device storage
	 */
    public void loadVideo(String filename)
    {
		curLoadUrl = filename;
		if(CUR_STATE != null && !CUR_STATE.equals(STATE_NONE)){
			if(filename.contains(".mp4")){
				showControls(STATE_VID);
			}else{
				showControls(STATE_PIC);
			}
		}
		Log.d("qiqi","filename:" + filename);
		if(_pfview != null) {
			_pfview.release();
			_frameContainer.removeViewAt(0);
		}
		if(_pfasset != null){
			_pfasset.stop();
			_pfasset.release();
			_pfasset = null;
		}
		if(_scrubberMonitorTimer!= null)
			_scrubberMonitorTimer.cancel();
		_pfview = PFObjectFactory.view(this);
		int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
		switch (rotation) {
			case Surface.ROTATION_0:
			case Surface.ROTATION_180:
				mRightIyun.setVisibility(View.INVISIBLE);
				getSupportActionBar().show();
				_info.setVisibility(View.VISIBLE);
				_pfview.setMode(0,1);
				break;
			case Surface.ROTATION_90:
			case Surface.ROTATION_270:
				mRightIyun.setVisibility(View.VISIBLE);
				getSupportActionBar().hide();
				showControls(STATE_NONE);
				_info.setVisibility(View.INVISIBLE);
				_pfview.setMode(2, 1);
				break;
		}
		if(!filename.contains(".mp4")){
			try {
				BitmapFactory.Options options = new BitmapFactory.Options();
				if(bitmapTmp != null && !bitmapTmp.isRecycled()){
					bitmapTmp.recycle();
				}
				bitmapTmp = BitmapFactory.decodeFile(filename, options);
				_pfview.injectImage(bitmapTmp);
			} catch (Exception e){
			}
		}else{
			_pfasset = PFObjectFactory.assetFromUri(this, Uri.parse(filename), this);
			_pfview.displayAsset(_pfasset);
		}

        _pfview.setNavigationMode(_currentNavigationMode);
        _frameContainer.addView(_pfview.getView(), 0);

    }

	public void downloadPic(final String wwwFile){
		ResponseFuture<InputStream> future = null;
		if (future == null) {
			//prepare the call
			future = Ion.with(this)
					.load(wwwFile)
					.asInputStream();
		}
		future.withResponse().setCallback(new FutureCallback<Response<InputStream>>() {
			@Override
			public void onCompleted(Exception e, Response<InputStream> result) {
				boolean success = false;
				if (e == null && result != null && result.getResult() != null) {
					try {
						//create a temporary directory within the cache folder
						File dir = new File(getCacheDir() + "/images");
						if (!dir.exists()) {
							dir.mkdirs();
						}

						//create the file
						File file = new File(dir, "unsplash.jpg");
						if (!file.exists()) {
							file.createNewFile();
						}

						//copy the image onto this file
						Utils.copyInputStreamToFile(result.getResult(), file);
						Uri contentUri = FileProvider.getUriForFile(SimplePicPlayerActivity.this, "com.mikepenz.fileprovider", file);
						Message msg = new Message();
						Bundle bundle = new Bundle();
						bundle.putString("url",file.getAbsolutePath());
						msg.setData(bundle);
						msg.what = 0;
						handler.sendMessage(msg);
						success = true;
					} catch (Exception ex) {
						Log.e("un:splash", ex.toString());
					}

					//animate after complete
				} else {
				}
			}
		});

	}
	private float last_playback = 0;
	/**
	 * Status callback from the PFAsset instance.
	 * Based on the status this function selects the appropriate action.
	 *
	 * @param  asset  The asset who is calling the function
	 * @param  status The current status of the asset.
	 */
	public void onStatusMessage(final PFAsset asset, PFAssetStatus status) {
		switch (status) {
			case LOADED:

				Log.d("SimplePlayer", "Loaded");
//				_playButton.setClickable(true);
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
									handler.sendEmptyMessage(2);
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
			if(_pfasset != null ){
				if (_pfasset.getStatus() == PFAssetStatus.PLAYING)
				{
					_pfasset.pause();
				}
				else
					_pfasset.play();
			}
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
		if (_pfasset != null)
		{
			if (_pfasset.getStatus() == PFAssetStatus.PLAYING)
				_pfasset.pause();
		}
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

			mRightIyun.setVisibility(View.INVISIBLE);
			_controlerContainer.setClickable(false);
			getSupportActionBar().show();
			_info.setVisibility(View.VISIBLE);
//			Toast.makeText(MainActivity.this, "现在是竖屏", Toast.LENGTH_SHORT).show();
			if(_pfview != null){
				_pfview.setMode(0,1);
				_currentNavigationMode = PFNavigationMode.TOUCH;
				_pfview.setNavigationMode(_currentNavigationMode);
				_pfview.handleOrientationChange();
			}
		}
		if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
//			Toast.makeText(MainActivity.this, "现在是横屏", Toast.LENGTH_SHORT).show();
			Log.d("qiqi", "现在是横屏");
			mRightIyun.setVisibility(View.VISIBLE);
			_controlerContainer.setClickable(true);
			getSupportActionBar().hide();
			showControls(STATE_NONE);
			_info.setVisibility(View.INVISIBLE);
			if(_pfview != null){
//				PFHotspot _pfHotspot = _pfview.createHotspot(BitmapFactory.decodeResource(getResources(), R.drawable.refresh));
//				_pfHotspot.setCoordinates(-90,0,0);
//				_pfHotspot.setEnabled(true);
				_pfview.setMode(2, 1);
				_currentNavigationMode = PFNavigationMode.MOTION;
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
		if (_pfasset != null)
		{
			_pfasset.stop();
			_pfasset.release();
		}
		if(_pfview != null){
			_pfview.release();
		}
	}

}
