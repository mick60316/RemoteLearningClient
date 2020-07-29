package com.example.remotecontroller.bleconnection;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.remotecontroller.Constant;
import com.example.remotecontroller.MainActivity;
import com.example.remotecontroller.R;

import java.util.List;


public class MyService extends Service {
	private static final String TAG = Service.class.getSimpleName();
	private static final int SETUP_REQCODE = 100;
	private static final int CLOSE_REQCODE = 200;
	private static final int NOTIFICATION_ID = 543;
	private static final String CHANNEL_ID = "alive channel";

	private boolean isServiceOn = false;
	private NotificationManager notificationManager;
	private BLEService mBLEService;

	private boolean lockApp =false;

	public String connDeviceState = "disconnected", connTabletState = "disconnected", connMobileState = "disconnected";

	@Override
	public void onCreate() {
		super.onCreate();
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mBLEService = new BLEService((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE), this);

		// 註冊 Receiver
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.SEND);
		filter.addAction(Constant.LOCK_APPLICATION);
		registerReceiver(receiver, filter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand");

		if (!isServiceOn){ //首次啟動
			startForegroundService();
			mBLEService.checkController();
			isServiceOn = true;
		} else{
			stateChanged(Constant.DEVICE_TABLET, connTabletState);
			stateChanged(Constant.DEVICE_ARDUINO, connDeviceState);
			stateChanged(Constant.DEVICE_MOBILE, connMobileState);
		}

		if (intent.getAction() == Constant.CMD_SHUTDOWN) { //點擊狀態列的結束按鈕
			onDestroy();
		}

		return super.onStartCommand(intent, flags, startId);
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		try{
			unregisterReceiver(receiver);
		} catch (Exception e){
			Log.e(TAG, e.toString());
		}
		sendIntentBroadcast(Constant.CMD_SHUTDOWN,"",null);
		mBLEService.closeAllGatt();
		stopForegroundService();
	}


	private void startForegroundService() {
		Intent setupIntent = new Intent(this, MainActivity.class);
		PendingIntent setupPendingIntent = PendingIntent.getActivity(this, SETUP_REQCODE, setupIntent, 0);

		Intent closeIntent = new Intent(this, com.example.remotecontroller.bleconnection.MyService.class);
		closeIntent.setAction(Constant.CMD_SHUTDOWN);
		PendingIntent closePendingIntent = PendingIntent.getService(this, CLOSE_REQCODE, closeIntent, 0);


		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "RemoteLearning", NotificationManager.IMPORTANCE_HIGH);
			notificationChannel.setDescription("keep it alive");
			notificationManager.createNotificationChannel(notificationChannel);

			Notification.Action closeAction = new Notification.Action.Builder(Icon.createWithResource(getApplicationContext(), R.drawable.ic_launcher_background), "CLOSE", closePendingIntent).build();
			Notification.Action setupAction = new Notification.Action.Builder(Icon.createWithResource(getApplicationContext(), R.drawable.ic_launcher_background), "SETUP", setupPendingIntent).build();

			Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
					.setContentTitle(getString(R.string.app_name))
					.setContentText("Service is still alive...")
					.setSmallIcon(R.drawable.ic_launcher_foreground)
					.addAction(setupAction)
					.addAction(closeAction);

			Notification notification = builder.build();
			notificationManager.notify(NOTIFICATION_ID, notification);
			startForeground(NOTIFICATION_ID, notification);
		}else {
			Notification notification = new NotificationCompat.Builder(this)
					.setContentTitle(getResources().getString(R.string.app_name))
					.setContentText("Service is still alive...")
					.addAction(R.drawable.ic_launcher_background, "SETUP", setupPendingIntent)
					.addAction(R.drawable.ic_launcher_background, "CLOSE", closePendingIntent).build();

			startForeground(NOTIFICATION_ID, notification);
		}
	}


	private void stopForegroundService() {
		notificationManager.cancel(NOTIFICATION_ID);
		stopForeground(true);
		stopSelf();
	}


	/**
	 * 喚醒 Activity
	 */
	public void triggerActivity() {
		if (!isForeground(this.getPackageName())){
			Intent intent = new Intent(this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			mBLEService.tabletsStatus.put("top","in");
		}
	}
	public void triggerActivityToHome() {

		if (!lockApp&&isForeground(this.getPackageName())) {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			mBLEService.tabletsStatus.put("top","out");
		}
	}
	public boolean isForeground(String myPackage) {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
		ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
		return componentInfo.getPackageName().equals(myPackage);
	}


	/**
	 * 通知 Activity 連線狀態改變
	 * @param deviceName
	 * @param state
	 */
	public void stateChanged(String deviceName, String state) {
		Log.e(TAG, "Constant.DEVICE_MOBILE: "+Constant.DEVICE_MOBILE);
		Log.e(TAG, "String deviceName: "+deviceName);
		if (isForeground(this.getPackageName())){
			if (Constant.DEVICE_ARDUINO.contentEquals(deviceName)){
				connDeviceState = state;
				sendIntentBroadcast(Constant.STATE_ARDUINO, Constant.CONN, connDeviceState.getBytes());
			} else if (Constant.DEVICE_TABLET.contentEquals(deviceName)){
				connTabletState = state;
				sendIntentBroadcast(Constant.STATE_TABLET, Constant.CONN, connTabletState.getBytes());
			} else if (Constant.DEVICE_MOBILE.contentEquals(deviceName)){
				connMobileState = state;
				sendIntentBroadcast(Constant.STATE_MOBILE, Constant.CONN, connMobileState.getBytes());
			}
		}
	}

	/**
	 * 通知 Activity Server 傳來訊息
	 * @param data
	 */
	public void serverDataToUI(byte[] data) {
		sendIntentBroadcast(Constant.RECEIVE, Constant.RECEIVE_MSG, data);
	}





	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();


			if(action.equals(Constant.SEND)){
				Log.e(TAG,"action: client is sending data");
				byte[] msg = intent.getByteArrayExtra(Constant.SEND_MSG);
				mBLEService.sendData(Constant.DEVICE_TABLET, msg);
			}
			else if (action.equals(Constant.LOCK_APPLICATION))
			{

				lockApp=intent.getBooleanExtra(Constant.LOCK_APPLICATION,false);

				Log.i(TAG ,"Lock App "+ lockApp);




			}
		}
	};

	private void sendIntentBroadcast(String action, String key, byte[] val){
		Intent intent = new Intent();
		intent.setAction(action);
		intent.putExtra(key, val);

		sendBroadcast(intent);
	}


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

}



















