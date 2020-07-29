package com.example.remotecontroller;

public class Constant {

	public static final String CMD_SHUTDOWN = "shutdown"; // action

	public static final String SEND = "sendBytes"; // action
	public static final String SEND_MSG = "data"; // key
	public static final String RECEIVE = "receiving"; // action
	public static final String RECEIVE_MSG = "data"; // key
	public static final String STATE_ARDUINO = "stateArduino"; // action
	public static final String STATE_TABLET = "stateTablet"; // action
	public static final String STATE_MOBILE = "stateMobile"; // action
	public static final String STATE_SESSION = "stateSession";
	public static final String CONN = "conn"; // key
	public static final String LOCK_APPLICATION ="lockApp";



	public static final String DEVICE_ARDUINO = "RemoteLearning";
	public static final String DEVICE_TABLET = "LearningTabServer";
	public static final String DEVICE_MOBILE = "LearningMobServer";

	public static final String SERVICE_UUID_ARDUINO = "0000ffe0-0000-1000-8000-00805f9b34fb";
	public static final String CHAR_UUID_ARDUINO = "0000ffe1-0000-1000-8000-00805f9b34fb";

	public static final String SERVICE_UUID_TAB = "0000fff2-0000-1000-8000-00805f9b34fb";
	public static final String CHAR_UUID_TAB = "0000ffe4-0000-1000-8000-00805f9b34fb";

	public static final String SERVICE_UUID_MOBILE = "0000fff5-0000-1000-8000-00805f9b34fb";
	public static final String CHAR_UUID_MOBILE = "0000ffe7-0000-1000-8000-00805f9b34fb";

	// BT speaker's connection status => 0: 未連線, 1: 連線中, 2: 已連線
	public static final int SPEAKER_DISCONN = 0;
	public static final int SPEAKER_CONN = 2;


	public static final int SCAN_PERIOD = 15000;
}
