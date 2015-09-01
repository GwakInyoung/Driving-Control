package com.hbe.smartcarmove;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hbe.bluetooth.HBEBT;
import com.hbe.bluetooth.HBEBTListener;

public class MainActivity extends Activity implements HBEBTListener {

	public final static byte[] CMD_FORWARD = { 0x76, 0x00, 0x20, 0x00, 0x09,
			0x00, 0x00 };
	// public final static byte[] CMD_LEFT =
	// {0x76,0x00,0x20,0x00,0x0A,0x00,0x00};
	// public final static byte[] CMD_RIGHT =
	// {0x76,0x00,0x20,0x00,0x05,0x00,0x00};
	public final static byte[] CMD_BACKWARD = { 0x76, 0x00, 0x20, 0x00, 0x06,
			0x00, 0x00 };
	public final static byte[] CMD_FORWARD_LEFT = { 0x76, 0x00, 0x20, 0x00,
			0x08, 0x00, 0x00 };
	public final static byte[] CMD_FORWARD_RIGHT = { 0x76, 0x00, 0x20, 0x00,
			0x01, 0x00, 0x00 };
	// public final static byte[] CMD_BACKWARD_LEFT =
	// {0x76,0x00,0x20,0x00,0x04,0x00,0x00};
	// public final static byte[] CMD_BACKWARD_RIGHT =
	// {0x76,0x00,0x20,0x00,0x02,0x00,0x00};
	public final static byte[] CMD_STOP = { 0x76, 0x00, 0x20, 0x00, 0x00, 0x00,
			0x00 };
	private Button[] mMoveButton; // go_stop&stop버튼
	private TextView mState; // 블루투스연결상태
	private Button mConnectButton; // 블루투스버튼
	private byte[][] mCommands; // 제어패킷

	private Button mUpButton; // 앞으로가기버튼
	private Button mDownButton; // 뒤로가기버튼
	private Button mStopButton; // 멈추기버튼

	private HBEBT mBluetooth;
	private Button mMusicButton;// music on버튼
	MediaPlayer sound;
	float y1 = 0;// 가속도센서 y값 (좌우)
	int flag = 1;// sendData작동유무 0이면 stop 1이면 go

	SensorManager sensorManager;
	Sensor sensor1;
	int width = 0;
	int height = 0;
	TextView text;
	Vibrator vi;

	int startx = 0, starty = 0, endx = 0, endy = 0, deltax = 0, deltay = 0;
	byte speed = 1;

	public static byte[] CMD_SENSOR_ON = { 0x76, 0x00, 0x30, 0x00, 0x01, 0x00,
			0x31 };
	public static byte[] CMD_SENSOR_OFF = { 0x76, 0x00, 0x30, 0x00, 0x00, 0x00,
			0x30 };

	private TextView[] mSensor = new TextView[12];

	private Button mSensingButton;

	private byte[] mBuff = new byte[100];
	private int mBuffLen = 0;

	private LinearLayout mLayout;

	// -----------------------------------------------sendData함수
	public void call(int flag, byte[] mCommands, byte speed) {

		byte[] cmd;
		cmd = mCommands;
		cmd[5] = speed;
		cmd[6] = getCheckSum(cmd);
		if (flag == 1) {
			mBluetooth.sendData(cmd);
		}
	}

	// ---------------------------------------------------------color
	public void colorR(LinearLayout mLayout) {
		mLayout.setBackgroundColor(Color.rgb(255, 0, 127));
	}

	public void colorW(LinearLayout mLayout) {
		mLayout.setBackgroundColor(Color.rgb(255, 255, 255));
	}

	// -----------------------------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		mLayout = (LinearLayout) findViewById(R.id.mLayout);
		mLayout.setBackgroundColor(Color.rgb(255, 255, 255));
		// ---------------------------------------------------------화면크기받아오기---------

		DisplayMetrics dm = getApplicationContext().getResources()
				.getDisplayMetrics();

		width = dm.widthPixels;
		height = dm.heightPixels;

		// ---------------------------------------------------------------------------------
		// ----------------------------------------------------------------------------음악
		LayoutInflater inflater = getLayoutInflater();
		View layout = (View) inflater.inflate(R.layout.music, null);
		addContentView(layout, new LinearLayout.LayoutParams(width, height));
		// --------------------------------------------------------------블루투스
		mState = (TextView) findViewById(R.id.state);

		mConnectButton = (Button) findViewById(R.id.button1);
		mConnectButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mBluetooth.conntect();

			}
		});
		mBluetooth = new HBEBT(this);
		mBluetooth.setListener(this);

		// ----------------------------------------------------------------------
		mSensor = new TextView[12];
		mState = (TextView) findViewById(R.id.state);

		// ---------------------------------------------------------------------센서
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sensor1 = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		vi = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		// -----------------------------------------------------------------------
		// -------------------------------------------------------------------버튼생성
		mMoveButton = new Button[1];
		mMoveButton[0] = (Button) findViewById(R.id.go_stop);
		// ------------------------------------------------------------------------
		// -----------------------------------------------------------------제어명령어

		mCommands = new byte[5][];
		mCommands[0] = CMD_STOP;
		mCommands[1] = CMD_FORWARD_RIGHT;
		mCommands[2] = CMD_FORWARD_LEFT;
		mCommands[3] = CMD_FORWARD;
		mCommands[4] = CMD_BACKWARD;
		// -------------------------------------------------------------------------

		mUpButton = (Button) findViewById(R.id.go);
		mUpButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				call(flag, mCommands[3], speed);
			}
		});

		mStopButton = (Button) findViewById(R.id.go_stop);
		mStopButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				call(flag, mCommands[0], speed);
			}
		});

		mDownButton = (Button) findViewById(R.id.down);
		mDownButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				call(flag, mCommands[4], speed);
			}
		});
		sound = MediaPlayer.create(getApplicationContext(), R.raw.music);
		mMusicButton = (Button) findViewById(R.id.music);
		mMusicButton.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mMusicButton.setSelected(true);
				sound.start();
				sound.setLooping(true);

			}
		});

	}

	// ------------------------------------------------------------터치
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);

		mBluetooth.sendData(CMD_SENSOR_ON);
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			startx = (int) event.getX();
			starty = (int) event.getY();
		}
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			endx = (int) event.getX();
			endy = (int) event.getY();
			deltay = endy - starty;
		}

		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (deltay < 0) {
				speed = 4;
			}
			if (deltay > 0) {
				speed = 1;

			}
			String msg = "현재 speed: " + speed;
			Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
		}

		return false;
	}

	// --------------------------------------------------------------------------------
	public void onResume() {

		super.onResume();
		sensorManager.registerListener(accelListener, sensor1,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void onStop() {
		super.onStop();
		sensorManager.unregisterListener(accelListener);
	}

	SensorEventListener accelListener = new SensorEventListener() {
		public void onAccuracyChanged(Sensor sensor, int acc) {
		}

		public void onSensorChanged(SensorEvent event) {

			y1 = event.values[1];

			if (y1 > 2) {// 오른쪽 회전시
				mMoveButton[0].setOnClickListener(new Button.OnClickListener() {
					@Override
					public void onClick(View v) {

						call(flag, mCommands[0], speed);// go_stop버튼 누르면 잠시멈춤

					}
				});

				call(flag, mCommands[1], speed);

			} else if (y1 < -2) {// 왼쪽 회전시
				mMoveButton[0].setOnClickListener(new Button.OnClickListener() {
					@Override
					public void onClick(View v) {
						call(flag, mCommands[0], speed);// go_stop버튼 누르면 잠시멈춤
					}
				});

				call(flag, mCommands[2], speed);

			}

		}

	};

	@Override
	public void onConnected() {
		// TODO Auto-generated method stub
		mState.setText("Bluetooth State: Connected");

	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		mState.setText("Bluetooth State: Disconnected");

	}

	@Override
	public void onConnecting() {
		// TODO Auto-generated method stub
		mState.setText("Bluetooth State: Connecting");
	}

	@Override
	public void onConnectionFailed() {
		// TODO Auto-generated method stub
		mState.setText("Bluetooth State: ConnectionFailed");

	}

	@Override
	public void onConnectionLost() {
		// TODO Auto-generated method stub
		mState.setText("Bluetooth State: ConnectionLost");

	}

	@Override
	public void onReceive(byte[] buffer) {
		// TODO Auto-generated method stub
		colorW(mLayout);
		int sensor = 0;
		for (int n = 0; n < buffer.length; n++) {
			byte buff = buffer[n];

			if (mBuffLen == 0 && buff != 0x76) {
				return;
			} else if (mBuffLen == 1 && buff != 0x00) {
				mBuffLen = 0;
				return;
			} else if (mBuffLen > 1 && buff == 0x00
					&& mBuff[mBuffLen - 1] == 0x76) {
				mBuffLen = 2;
				mBuff[0] = 0x76;
				mBuff[1] = 0x00;
			} else {
				mBuff[mBuffLen++] = buff;

				if (mBuffLen == 17 && mBuff[2] == 0x3C) {

					for (int i = 0; i < 12; i++) {
						sensor = mBuff[i + 4] & 0xFF;
						if (i <= 3) {

							if (sensor < 30) {
								vi.vibrate(100);
							}

						}
						if (i >= 9) {

							if (sensor < 30) {
								vi.vibrate(100);
								if (i == 9) {
									colorR(mLayout);
								} else {
									colorW(mLayout);
								}
							}
						}

					}

					mBuffLen = 0;
				}
			}
		}
	}

	public byte getCheckSum(byte[] buff) {
		int ret = (buff[2] & 0xFF) + (buff[4] & 0xFF) + (buff[5] & 0xFF);

		return (byte) ret;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		// 하드웨어 뒤로가기 버튼에 따른 이벤트 설정
		case KeyEvent.KEYCODE_BACK:

			new AlertDialog.Builder(this)
					.setTitle("프로그램 종료")
					.setMessage("프로그램을 종료 하시겠습니까?")
					.setPositiveButton("예",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									android.os.Process
											.killProcess(android.os.Process
													.myPid());
								}
							}).setNegativeButton("아니오", null).show();

			break;

		default:
			break;
		}

		return super.onKeyDown(keyCode, event);
	}

}
