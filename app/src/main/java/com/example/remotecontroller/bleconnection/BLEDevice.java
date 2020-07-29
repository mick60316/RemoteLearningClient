package com.example.remotecontroller.bleconnection;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

public class BLEDevice {
	private String sDeviceName = null;
	private UUID uService = null;
	private UUID uCharacteristic = null;
	private BluetoothGattService service = null;
	private BluetoothGattCharacteristic characteristic = null;
	private BluetoothGatt mBluetoothGatt = null;

	public BLEDevice(String sDeviceName, String uService, String uCharacteristic) {
		this.sDeviceName = sDeviceName;
		this.uService = UUID.fromString(uService);
		this.uCharacteristic = UUID.fromString(uCharacteristic);
	}

	public String getsDeviceName() {
		return sDeviceName;
	}

	public void setsDeviceName(String sDeviceName) {
		this.sDeviceName = sDeviceName;
	}

	public UUID getuService() {
		return uService;
	}

	public void setuService(String uService) {
		this.uService = UUID.fromString(uService);
	}

	public UUID getuCharacteristic() {
		return uCharacteristic;
	}

	public void setuCharacteristic(String uCharacteristic) {
		this.uCharacteristic = UUID.fromString(uCharacteristic);
	}

	public BluetoothGattService getService() {
		return service;
	}

	public void setService(BluetoothGattService service) {
		this.service = service;
	}

	public BluetoothGattCharacteristic getCharacteristic() {
		return characteristic;
	}

	public void setCharacteristic(BluetoothGattCharacteristic characteristic) {
		this.characteristic = characteristic;
	}

	public BluetoothGatt getmBluetoothGatt() {
		return mBluetoothGatt;
	}

	public void setmBluetoothGatt(BluetoothGatt mBluetoothGatt) {
		this.mBluetoothGatt = mBluetoothGatt;
	}

}
