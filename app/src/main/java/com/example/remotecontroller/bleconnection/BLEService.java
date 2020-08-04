package com.example.remotecontroller.bleconnection;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.remotecontroller.Constant;
import com.example.remotecontroller.ExtraTools;
import com.example.remotecontroller.MainActivity;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BLEService {
	private static final String TAG = BLEService.class.getSimpleName();


	private BluetoothManager bluetoothManager;
	private BluetoothAdapter bluetoothAdapter;
	private List<BLEDevice> mBLEDeviceList = new ArrayList<BLEDevice>();
	private ArrayList<BluetoothDevice> mLeDevices = new ArrayList<BluetoothDevice>();
	private Handler scanhandler = new Handler(Looper.getMainLooper());
	MainActivity mainActivity;
	MyService myService;
	Timer timer;
	TimerTask timerTask;

	// check conn state
	public Map<String, Boolean> isConnMap;
	public int isConnSpeaker = Constant.SPEAKER_DISCONN;

	// test 0723 紀錄兩台平板現下狀態
	public Map<String, String> tabletsStatus;
	private ReentrantLock lock = new ReentrantLock();



	public BLEService(BluetoothManager btManager, MyService myService) {
		bluetoothManager = btManager;
		bluetoothAdapter = bluetoothManager.getAdapter();

		this.myService = myService;
		BLEDevice device1 = new BLEDevice( Constant.DEVICE_ARDUINO, Constant.SERVICE_UUID_ARDUINO, Constant.CHAR_UUID_ARDUINO);
		BLEDevice device2 = new BLEDevice( Constant.DEVICE_TABLET, Constant.SERVICE_UUID_TAB, Constant.CHAR_UUID_TAB);
		BLEDevice device3 = new BLEDevice( Constant.DEVICE_MOBILE, Constant.SERVICE_UUID_MOBILE, Constant.CHAR_UUID_MOBILE);
		mBLEDeviceList.add(device1);
		mBLEDeviceList.add(device2);
		mBLEDeviceList.add(device3);

		isConnMap = new HashMap<String, Boolean>();
		isConnMap.put(Constant.DEVICE_ARDUINO, false);
		isConnMap.put(Constant.DEVICE_TABLET, false);
		isConnMap.put(Constant.DEVICE_MOBILE, false);

		tabletsStatus = new HashMap<String, String>();
		tabletsStatus.put("top","out");
		tabletsStatus.put("btm","out");
	}


	private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			if (device.getName() != null) {
				if (!mLeDevices.contains(device)) {
					mLeDevices.add(device);
					Log.i(TAG, "Scan device name" + device.getName());
					for(int i = 0; i < mBLEDeviceList.size(); i++){
						if(mBLEDeviceList.get(i).getsDeviceName().equals(device.getName())){
							Log.i(TAG, "scan" + device.getName() + "Scanned");
							connectDevice(device.getName());
						}
					}
				}
			}
		}
	};


	public int connectDevice(String DeviceName) {
		int index = hasDevice(DeviceName);
		int GattIndex = getGattIndex(DeviceName);
//		Log.i(TAG, "hasDevice checkcode  " + index + " " + GattIndex);
		if (index == -1 || GattIndex == -1) {
			return -1;
		}
		Log.i(TAG, "GattIndex " + GattIndex + " getGattIndex() " + getGattIndex(DeviceName) + "  index  " + index);
		if (mBLEDeviceList.get(GattIndex).getmBluetoothGatt() == null){
			mBLEDeviceList.get(GattIndex).setmBluetoothGatt(
					mLeDevices.get(index).connectGatt(mainActivity,false, bluetoothGattCallback));
		}
		return index;
	}

	public int getGattIndex(String DeviceName) {
//		Log.i(TAG, "getGattIndex  mBLEDeviceList.size()  " + mBLEDeviceList.size());
		int Index = -1;
		for (int i = 0; i < mBLEDeviceList.size(); i++) {
//			Log.i(TAG, "mBLEDeviceList.get(i).sDeviceName  " + mBLEDeviceList.get(i).getsDeviceName());
			if (mBLEDeviceList.get(i).getsDeviceName().equals(DeviceName)) {
//				Log.i(TAG, "getGattIndex  OKOK");
				Index = i;
			}
		}
		return Index;
	}

	public int hasDevice(String sDeviceName) {
		int Num = -1;
		Log.i(TAG, "mLeDevices.size()  " + mLeDevices.size());
		for (int i = 0; i < mLeDevices.size(); i++) {
			if (mLeDevices.get(i).getName().equals(sDeviceName)) {
				Num = i;
//				Log.i(TAG, "mLeDevices " + i + "  " + mLeDevices.get(i).getName());
			}
		}
		return Num;
	}

	BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
											int newState) {
			String deviceName = gatt.getDevice().getName();
			String state = "";
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				Log.i(TAG, "STATE_CONNECTED  " + deviceName);
				state = "connected";

				gatt.discoverServices();
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				Log.i(TAG, "STATE_DISCONNECTED " + deviceName);
				state = "disconnected";

				int GattIndex = getGattIndex(gatt.getDevice().getName());
				if (mBLEDeviceList.get(GattIndex).getmBluetoothGatt() != null && GattIndex != -1) {
					mBLEDeviceList.get(GattIndex).getmBluetoothGatt().disconnect();
					mBLEDeviceList.get(GattIndex).getmBluetoothGatt().close();
					mBLEDeviceList.get(GattIndex).setmBluetoothGatt(null);
					mBLEDeviceList.get(GattIndex).setCharacteristic(null);
					mLeDevices.clear();
				}
			}

			myService.stateChanged(deviceName, state);
			sendStateToMobile();
		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			super.onDescriptorRead(gatt, descriptor, status);
			Log.i(TAG, "onDescriptorRead ");
		}

		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			super.onReadRemoteRssi(gatt, rssi, status);
			Log.i(TAG, "onReadRemoteRssi ");
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				Log.i(TAG, "onServicesDiscovered ");
				List<BluetoothGattService> supportedGattServices = gatt.getServices();
				Log.i(TAG, "" + "onServicesDiscovered  DeviceName:  " + gatt.getDevice().getName());
				int GattIndex = getGattIndex(gatt.getDevice().getName());
				mBLEDeviceList.get(GattIndex).setService(
						mBLEDeviceList.get(GattIndex).getmBluetoothGatt().getService(mBLEDeviceList.get(GattIndex).getuService()));
				mBLEDeviceList.get(GattIndex).setCharacteristic(
						mBLEDeviceList.get(GattIndex).getService().getCharacteristic(mBLEDeviceList.get(GattIndex).getuCharacteristic()));
				mBLEDeviceList.get(GattIndex).getmBluetoothGatt().setCharacteristicNotification(
						mBLEDeviceList.get(GattIndex).getCharacteristic(), true);
				Log.i(TAG, "state"+gatt.getDevice().getName()+"Connected");
			} else {
				Log.e(TAG, "onservicesdiscovered收到: " + status);

			}
		}

		@Override
		// Result of a characteristic read operation
		public void onCharacteristicRead(BluetoothGatt gatt,
										 BluetoothGattCharacteristic characteristic,
										 int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				Log.i(TAG, "onCharacteristicRead " + characteristic.getStringValue(0));
			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			super.onCharacteristicWrite(gatt, characteristic, status);
			Log.i(TAG, "onCharacteristicWrite " + characteristic.getStringValue(0));

		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			super.onCharacteristicChanged(gatt, characteristic);
			Log.e("TAG", "onCharacteristicChanged  " + "DeviceName:  " + gatt.getDevice().getName() + "  value:  " + characteristic.getStringValue(0));
			String val = characteristic.getStringValue(0).trim();

			// test
			if (gatt.getDevice().getName().contains(Constant.DEVICE_ARDUINO)){
				String[] strList = val.split(",");
//				if (myService.isForeground(myService.getPackageName())){
//					tabletsStatus.get("top").contentEquals("in");
//				} else {
//					tabletsStatus.get("top").contentEquals("out");
//				}
				// top
//				Log.e("Logan~~~~~~", "top old: "+ tabletsStatus.get("top") + ", top new: " + strList[0]);
				if ((myService.isForeground(myService.getPackageName()) || tabletsStatus.get("top").contentEquals("out")) && strList[0].contentEquals("in")){
					myService.triggerActivity();
				} else if ((!myService.isForeground(myService.getPackageName()) || tabletsStatus.get("top").contentEquals("in")) && strList[0].contentEquals("out")) {
					myService.triggerActivityToHome();
				}
				// btm
//				Log.e("Logan~~~~~~", "btm old: "+ tabletsStatus.get("btm"));
				if ((tabletsStatus.get("btm").contentEquals("out") && strList[1].contentEquals("in")) ||
						(tabletsStatus.get("btm").contentEquals("in") && strList[1].contentEquals("out"))){
					String msg = "btm," + strList[1];
					tabletsStatus.put("btm", strList[1]);
					if (strList[1].contentEquals("out")){
						Log.e("Logan~~~~~~", "btm out to UI ");
						sendData(Constant.DEVICE_TABLET, msg.getBytes());
					} else {
						Log.e("Logan~~~~~~", "btm in to UI ");
					}
					myService.serverDataToUI(msg.getBytes());
				}
			}
			if (gatt.getDevice().getName().contains(Constant.DEVICE_TABLET)){
				if (val.contains("mode")){
//					Log.e("Logan~~~~~~", "return mode!!!!!!!!!!!!!!!");
					String[] msgs = val.split(",");
					tabletsStatus.put("btm",msgs[2]);
//					Log.e("Logan~~~~~~", "btm old: "+ tabletsStatus.get("btm") + ", btm new: " + msgs[2]);
				}
			}
			// test





			switch (gatt.getDevice().getName()) {
				case Constant.DEVICE_TABLET:
					myService.serverDataToUI(characteristic.getValue());
					if (val.contentEquals("t")){
						isConnMap.put(Constant.DEVICE_TABLET, true);
					}
					break;
				case Constant.DEVICE_MOBILE:
					myService.serverDataToUI(characteristic.getValue());
					if (val.contentEquals("m")){
						isConnMap.put(Constant.DEVICE_MOBILE, true);
					} else if (val.contentEquals("update,")) {
						sendStateToMobile();
					} else if (val.contentEquals("reset,")) {
						if (myService.currentSession != ExtraTools.S1) return;
						myService.triggerActivityToHome();
						sendData(Constant.DEVICE_TABLET, characteristic.getValue());
						tabletsStatus.put("top", "out");
						tabletsStatus.put("btm", "out");
					} else {
						// when signal is reset && current session != s1, don't send 'reset' to DEVICE_TABLET
						sendData(Constant.DEVICE_TABLET, characteristic.getValue());
					}
					break;
//				case Constant.DEVICE_ARDUINO:
//					String[] strList = val.split(",");
//
//					lock.lock();
//					try{
//						// top
//						if (tabletsStatus.get("top") == "out" && strList[0] == "in"){
//							myService.triggerActivity();
//						} else if (tabletsStatus.get("top") == "in" && strList[0] == "out") {
//							myService.triggerActivityToHome();
//						}
//						tabletsStatus.put("top",strList[0]);
//						// btm
//						if ((tabletsStatus.get("btm").contentEquals("out") && strList[1].contentEquals("in")) ||
//								(tabletsStatus.get("btm").contentEquals("in") && strList[1].contentEquals("out"))){
//							String msg = "btm," + strList[1];
//							sendData(Constant.DEVICE_TABLET, msg.getBytes());
//						}
//					} finally {
//						lock.unlock();
//					}


//					switch (val)
//					{
//						case "top,in":
//							myService.triggerActivity();
//							break;
//						case "top,out":
//							myService.triggerActivityToHome();
//							break;
//						case "btm,in":
//						case "btm,out":
//							sendData(Constant.DEVICE_TABLET, characteristic.getValue());
//							break;
//						case "d":
//							isConnMap.put(Constant.DEVICE_ARDUINO, true);
//							break;
//					}
//					break;
			}
		}
	};

	public void sendData(String DeviceName, byte[] data) {
		int GattIndex = getGattIndex(DeviceName);
		if (GattIndex != -1 && mBLEDeviceList.get(GattIndex).getmBluetoothGatt() != null) {
			BluetoothGattCharacteristic cha = mBLEDeviceList.get(GattIndex).getCharacteristic();
			if (cha == null) {
				Log.e(TAG, "cha == null");
				return;
			}
			cha.setValue(data);
			Log.e(TAG, "~~~~~sendData: " + data.toString());
			mBLEDeviceList.get(GattIndex).getmBluetoothGatt().writeCharacteristic(cha);
		}
	}

	public void sendStateToMobile() {
		String speakerState = "d", tabletState = "d", deviceState = "d";
		if (isConnSpeaker == Constant.SPEAKER_CONN)speakerState = "c";
		if (myService.connTabletState.contentEquals("connected"))tabletState = "c";
		if (myService.connDeviceState.contentEquals("connected"))deviceState = "c";
		String msg = tabletState + "," + deviceState + "," + speakerState;
		sendData(Constant.DEVICE_MOBILE, msg.getBytes());
	}

	public void closeAllGatt() {
		for (BLEDevice device: mBLEDeviceList) {
			if (device.getmBluetoothGatt() != null) {
				device.getmBluetoothGatt().disconnect();
				device.getmBluetoothGatt().close();
				device.setmBluetoothGatt(null);
			}
		}
	}

	public void checkController(){
		timer = new Timer(true);
		timerTask = new TimerTask() {
			@Override
			public void run() {

//				checkConnState();

				boolean isAllConn = true;
				for ( BLEDevice bd : mBLEDeviceList) {
					if (bd.getmBluetoothGatt() == null || bd.getCharacteristic() == null){
						isAllConn = false;
						bd.setmBluetoothGatt(null);
						bd.setCharacteristic(null);
					}
				}
				if (!isAllConn) {
					bluetoothAdapter.startLeScan(leScanCallback);
					Log.e(TAG, "TimerTask timerTask: startLeScan()");
				} else{
					bluetoothAdapter.stopLeScan(leScanCallback);
					Log.e(TAG, "TimerTask timerTask: stopLeScan()");
				}

				isConnSpeaker = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
				Log.e(TAG, "Bluetooth speaker conn state: " + isConnSpeaker);

				sendStateToMobile();
				Log.e(TAG, "TimerTask timerTask: sendStateToMobile()");
			}
		};

		timer.schedule(timerTask, 0,Constant.SCAN_PERIOD);
		Log.e(TAG, "timer.schedule(timerTask, 0, 30000)");
	}

	public void checkConnState() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				for (Map.Entry<String, Boolean> entry : isConnMap.entrySet()) {
					// check code
					if (entry.getKey().contentEquals(Constant.DEVICE_ARDUINO)) {
						sendData(entry.getKey(), "d".getBytes());
						Log.e(TAG, "checkConnState: " + entry.getKey());
					} else if (entry.getKey().contentEquals(Constant.DEVICE_TABLET)) {
						sendData(entry.getKey(), "t".getBytes());
						Log.e(TAG, "checkConnState: " + entry.getKey());
					} else if (entry.getKey().contentEquals(Constant.DEVICE_MOBILE)) {
						sendData(entry.getKey(), "m".getBytes());
						Log.e(TAG, "checkConnState: " + entry.getKey());
					}
				}

				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					Log.e(TAG, e.toString());
				}

				boolean isRescan = false;
				for (Map.Entry<String, Boolean> entry : isConnMap.entrySet()) {
					if (!entry.getValue()){
						isRescan = true;
						Log.e(TAG, "device: " + entry.getKey() + ", is Conn? " + entry.getValue());
						int i = getGattIndex(entry.getKey());
						if (i != -1){
							mBLEDeviceList.get(i).setmBluetoothGatt(null);
							mBLEDeviceList.get(i).setCharacteristic(null);
							mLeDevices.clear();
						}
					}
				}
				if (isRescan){
					bluetoothAdapter.startLeScan(leScanCallback);
				} else {
					bluetoothAdapter.stopLeScan(leScanCallback);
				}
			}
		});
		thread.start();
	}
}
