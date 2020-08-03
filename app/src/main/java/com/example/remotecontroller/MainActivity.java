package com.example.remotecontroller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.airbnb.lottie.LottieAnimationView;
import com.example.remotecontroller.Component.CustomVideoView;
import com.example.remotecontroller.Component.LightSensor;
import com.example.remotecontroller.bleconnection.MyService;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity  {
	private int currentSession = 0;
	private static final int TIME_UPDATE_MESSAGE=0;


	private static final String TAG = "Mike Remote Learning";

	private TextView deviceConnSate, tabletConnSate, mobileConnSate, receivingData;
	private TextView clockTextView,clockClassTextView;
	private CustomVideoView customVideoView;
	private RelativeLayout []  sessionLayouts=new RelativeLayout[5];
	private LightSensor lightSensor;
	private Button audiable_play_button;
	private LottieAnimationView lottieAnimationView;
	private RelativeLayout classInfoLayout;
	private LinearLayout bleDebugLayout,btnDebugLayout;



	private boolean isPlay =true;
	private boolean isDebugMode =false;
	private boolean isNextLock =false;
	private MediaPlayer topInAudio,botInAudio;






	final Handler handler =new Handler(){
		@Override
		public void handleMessage(@NonNull Message msg) {
			super.handleMessage(msg);
			if (msg.what ==TIME_UPDATE_MESSAGE)
			{
				clockTextView.setText((String) msg.obj);
			}
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setActivityInfo();
		linkUserInterfaceAndCreateCustomVideoview();
		registerBoardcast();
		startClockTimer();
		hideBottomUIMenu2();
		topInAudio=MediaPlayer.create(this,Resource.tableConnectTopAudioId);
		botInAudio=MediaPlayer.create(this,Resource.tableConnectBotAudioId);

		clockClassTextView.setText("Math Class at "+ExtraTools.getClassTime());


		//mediaPlayer = MediaPlayer.create(this, R.raw.video1);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		int x = (int)event.getX();
		int y = (int)event.getY();
		Log.i(TAG,"Position "+x +" "+y);


		return false;
	}

	private void registerBoardcast ()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.CMD_SHUTDOWN);
		filter.addAction(Constant.RECEIVE);
		filter.addAction(Constant.STATE_ARDUINO);
		filter.addAction(Constant.STATE_TABLET);
		filter.addAction(Constant.STATE_MOBILE);
		registerReceiver(receiver, filter);
	}
	private void startClockTimer ()
	{
		Timer clockTimer =new Timer(true);
		clockTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				Message msg = handler.obtainMessage();
				msg.what = TIME_UPDATE_MESSAGE;
				msg.obj=ExtraTools.getCurrentTime();
				msg.sendToTarget();
				//Log.i(TAG,"Video position "+videoView.getCurrentPosition());
			}
		},0,1000);
	}


	private void linkUserInterfaceAndCreateCustomVideoview()
	{

		/*
			Link user interface between xml with java code and create custom videoview
		 */
		deviceConnSate = findViewById(R.id.text_device_conn_state);
		tabletConnSate = findViewById(R.id.text_tablet_conn_state);
		mobileConnSate = findViewById(R.id.text_mobile_conn_state);
		receivingData = findViewById(R.id.text_receiving_data);
		//paintingView =findViewById(R.id.PaintingView);
		clockTextView=findViewById(R.id.clock_TextView);
		clockClassTextView=findViewById(R.id.class_clock_textview);
		customVideoView=new CustomVideoView(this,findViewById(R.id.videoview), findViewById(R.id.imageView), new CustomVideoView.AlexaFinishCallback() {
			@Override
			public void onCompletion() {
				changeSession(ExtraTools.S1);
			}
		});
		lightSensor =new LightSensor((SensorManager)getSystemService(Context.SENSOR_SERVICE),findViewById(R.id.light_mask));
		sessionLayouts[0] =findViewById(R.id.s1lyaout);
		sessionLayouts[1] =findViewById(R.id.s2layout);
		sessionLayouts[2] =findViewById(R.id.s3layout);
		sessionLayouts[3]=findViewById(R.id.s4layout);
		sessionLayouts[4]=findViewById(R.id.s5layout);
		audiable_play_button=findViewById(R.id.btn_audible_play);
		lottieAnimationView =findViewById(R.id.lottie_show_mode);
		customVideoView.setLottieAnimationView(lottieAnimationView);
		classInfoLayout =findViewById(R.id.class_info_layout);
		bleDebugLayout=findViewById(R.id.ble_debugLayout);
		btnDebugLayout=findViewById(R.id.btn_debug_layout);
	}

	@SuppressLint("SourceLockedOrientationActivity")
	private void setActivityInfo()// Lock screen orientation and full screen
	{
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
		getWindow().getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_FULLSCREEN
						| View.SYSTEM_UI_FLAG_IMMERSIVE);
	}
	@Override
	protected void onStart() {
		super.onStart();
		Log.i(TAG,"OnStart");
		Intent intent = new Intent(MainActivity.this, MyService.class);
		// start service
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startForegroundService(intent);
		} else {
			startService(intent);
		}
		Log.i(TAG,"getPackageName "+ getPackageName());
		changeSession(ExtraTools.S1);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}



	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		String state = "";
		switch (action) {
			case Constant.CMD_SHUTDOWN:
				finishAndRemoveTask();
				break;
			case Constant.RECEIVE:
				String data = new String(intent.getByteArrayExtra(Constant.RECEIVE_MSG));
				processBleMessage(data);
				receivingData.setText(data);
				triggerOnClick(data); //同 onclick(), 觸發changeSession事件
				break;
			case Constant.STATE_ARDUINO:
				state = new String(intent.getByteArrayExtra(Constant.CONN));
				deviceConnSate.setText(state);
				break;
			case Constant.STATE_TABLET:
				state = new String(intent.getByteArrayExtra(Constant.CONN));
				tabletConnSate.setText(state);
				break;
			case Constant.STATE_MOBILE:
				state = new String(intent.getByteArrayExtra(Constant.CONN));
				mobileConnSate.setText(state);
				break;
		}
		}
	};
	private void processBleMessage(String message)
	{
		String []dataSplit =message.split(",");
		if (dataSplit[0].equals("next"))
		{
			sendLockAppToService(true);
			customVideoView.nextClick();
		}
		else if (dataSplit[0].equals("session"))
		{

			changeSession(Integer.valueOf(dataSplit[1])-1);
			sendMessageToTabletServer(message.getBytes());

		}
		else if (dataSplit[0].equals("mode"))
		{
			if (currentSession ==ExtraTools.S4) {

				customVideoView.setIsSingle(dataSplit[1].equals("1"));
			}
			Log.e(TAG,"modemmmmmmmmmmmmmmmmmmmmmmmmmmmmmm"+currentSession);
			sendMessageToTabletServer(("session,"+(currentSession+1)+","+customVideoView.getCurrentCheckPointIndex()).getBytes());
		}
		else if (dataSplit[0].equals("single"))
		{
			//customVideoView.setIsSingle(dataSplit[1].equals("1"));
		}
		else if (dataSplit[0].equals("btm") )
		{
			if (dataSplit[1].equals("in")) {
				Log.e(TAG,"currentSession "+ currentSession +" "+customVideoView.getCurrentCheckPointIndex());
				if(currentSession==ExtraTools.S4) customVideoView.setIsSingle(false);
				TimerTask task = new TimerTask(){
					public void run(){
						sendMessageToTabletServer(("btm,in," + (currentSession + 1) + "," + (customVideoView.getCurrentCheckPointIndex())).getBytes());
						botInAudio.start();
					}
				};
				Timer timer = new Timer();
				timer.schedule(task, 500);
			}
			else if (dataSplit[1].equals("out"))
			{

				if(currentSession==ExtraTools.S4) customVideoView.setIsSingle(true);
//				sendMessageToTabletServer(("btm,out," + (currentSession + 1) + "," + (customVideoView.getCurrentCheckPointIndex())).getBytes());

			}
			Log.e(TAG, "dataSplit[1].equals(\"in\")) sendMessageToTabletServer");
		}
	}


	private void sendMessageToTabletServer(byte[] message){
		Intent intent = new Intent();
		intent.setAction(Constant.SEND);
		intent.putExtra(Constant.SEND_MSG, message);
		sendBroadcast(intent);
	}

	private void triggerOnClick(String data) {
		switch (data) {
			case "s1":
				changeSession(ExtraTools.S1);
				break;
			case "s2":
				changeSession(ExtraTools.S2);
				break;
			case "s3":
				changeSession(ExtraTools.S3);
				break;
			case "s4":
				changeSession(ExtraTools.S4);
				break;
			case "s5":
//				changeSession(ExtraTools.S5);
				break;
			case "next":
				break;
			default:
				break;
		}
	}

	public void onClick (View view)
	{
		/*
		*  Button click event
		*
		* */
		switch (view.getId())
		{
			case R.id.btn_video1: case  R.id.btn_s5back:	case R.id.btn_endclass:

				changeSession(ExtraTools.S1);
				break;
			case R.id.btn_video2:
				changeSession(ExtraTools.S2);
				break;
			case R.id.btn_video3:
				changeSession(ExtraTools.S3);
				break;
			case R.id.btn_video4:
				changeSession(ExtraTools.S4);
				break;
			case R.id.btn_video5:
				//customVideoView.playAlexaOkAudio("ff");
//				changeSession(ExtraTools.S5);
				break;
				//customVideoView.replayVideo();

			case R.id.btn_test:
				String testStr = "Hello world";
				sendMessageToTabletServer(testStr.getBytes());
				break;
			case R.id.btn_s4next: case R.id.btn_start: case R.id.btn_play:
				if (!isNextLock) {
					sendMessageToTabletServer("next,".getBytes());
					customVideoView.nextClick();
					sendLockAppToService(true);
					isNextLock =true;
					Timer timer =new Timer();
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							Log.e(TAG,"Timer Click");
							isNextLock =false;
							timer.cancel();
						}
					},500);
				}
				else
				{
					Log.e(TAG,"Can not next");
				}
				break;
			case R.id.btn_audible:
				isPlay =true;
				classInfoLayout.setVisibility(View.INVISIBLE);
				audiable_play_button.setVisibility(View.VISIBLE);

				sendMessageToTabletServer("audible,".getBytes());
				customVideoView.nextClick();
				sendLockAppToService(true);
				audiable_play_button.setBackground(isPlay? getResources().getDrawable(R.mipmap.pause): getResources().getDrawable(R.mipmap.play));

				break;
			case R.id.btn_audible_play:
				isPlay=!isPlay;

				if (isPlay)
				{
					customVideoView.s5PlayVideo();
				}
				else
				{
					customVideoView.s5PauseVideo();

				}

				audiable_play_button.setBackground(isPlay? getResources().getDrawable(R.mipmap.pause): getResources().getDrawable(R.mipmap.play));

				break;
			case R.id.btn_s5play_lottie:

				customVideoView.playLottieAnimation();
				break;
			case R.id.btn_s1_class_time:
				sendMessageToTabletServer(("session,"+(ExtraTools.S2+1)).getBytes());
				currentSession = ExtraTools.S2;
				audiable_play_button.setVisibility(View.INVISIBLE);
				Log.i(TAG,"Change Session To "+currentSession);
				Intent intent = new Intent();
				intent.setAction(Constant.LOCK_APPLICATION);
				intent.putExtra(Constant.LOCK_APPLICATION, currentSession);
				sendBroadcast(intent);
				customVideoView.jumpToClass();
				sessionLayouts[0].setVisibility(View.INVISIBLE);
				sessionLayouts[1].setVisibility(View.VISIBLE);
				break;
			case R.id.btn_debug:
				isDebugMode =!isDebugMode;
				int visablity =isDebugMode==true? View.VISIBLE:View.INVISIBLE;
				bleDebugLayout.setVisibility(visablity);
				btnDebugLayout.setVisibility(visablity);
				break;
				default:
				break;
		}

	}

	@Override
	protected void onRestart() {
		super.onRestart();
		topInAudio.start();
	}

	private void changeSession (int session)
	{

		if (session ==ExtraTools.S1) {
			classInfoLayout.setVisibility(View.VISIBLE);
			clockClassTextView.setText("Math Class at "+ExtraTools.getClassTime());
			sendLockAppToService(false);
		}
		else if (session==ExtraTools.S2 || session== ExtraTools.S4)
		{
			sendLockAppToService(false);
		}
		else if (session==ExtraTools.S3)
		{

			sendLockAppToService(true);
		}


		sendMessageToTabletServer(("session,"+(session+1)).getBytes());
		currentSession = session;
		customVideoView.changeSession(session);
		audiable_play_button.setVisibility(View.INVISIBLE);
		Log.i(TAG,"Change Session To "+currentSession);
//		Intent intent = new Intent();
//		intent.setAction(Constant.LOCK_APPLICATION);
//		intent.putExtra(Constant.LOCK_APPLICATION, currentSession);
//		sendBroadcast(intent);
		for (int sessionLayoutIndex =0;sessionLayoutIndex<sessionLayouts.length ;sessionLayoutIndex++)
		{
			if (sessionLayouts[sessionLayoutIndex] != null)
			{
				if (sessionLayoutIndex ==session)
				{
					sessionLayouts[sessionLayoutIndex].setVisibility(View.VISIBLE);
				}
				else
				{
					sessionLayouts[sessionLayoutIndex].setVisibility(View.INVISIBLE);
				}

			}

		}

	}

	protected void hideBottomUIMenu() {
		//隱藏虛擬按鍵，並且全屏
		if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
			View v = this.getWindow().getDecorView();
			v.setSystemUiVisibility(View.GONE);
		} else if (Build.VERSION.SDK_INT >= 19) {
			//for new api versions.
			View decorView = getWindow().getDecorView();
			int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
			decorView.setSystemUiVisibility(uiOptions);

		}
	}
	protected void hideBottomUIMenu2() {
		//隱藏虛擬按鍵，並且全屏
		if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
			View v = this.getWindow().getDecorView();
			v.setSystemUiVisibility(View.GONE);
		} else if (Build.VERSION.SDK_INT >= 19) {

			Window _window = getWindow();
			WindowManager.LayoutParams params = _window.getAttributes();
			params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE;
			_window.setAttributes(params);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		lightSensor.registerListener();
	}

	@Override
	protected void onPause() {
		super.onPause();
		lightSensor.unregisterListener();
	}
	public void sendLockAppToService (boolean isLockApp)
	{
		Intent intent = new Intent();
		intent.setAction(Constant.LOCK_APPLICATION);
		intent.putExtra(Constant.LOCK_APPLICATION, isLockApp);
		sendBroadcast(intent);
	}
}
