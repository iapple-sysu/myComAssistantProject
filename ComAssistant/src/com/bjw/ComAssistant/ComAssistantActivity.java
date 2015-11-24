package com.bjw.ComAssistant;

import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;

import java.io.FileOutputStream;

import java.io.IOException;

import java.io.ObjectInputStream;

import java.io.ObjectOutputStream;

import java.security.InvalidParameterException;

import java.util.ArrayList;

import java.util.LinkedList;

import java.util.List;

import java.util.Queue;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.LogUtil.log;
import com.avos.avoscloud.PushService;
import com.avos.avoscloud.SaveCallback;

import com.avos.avoscloud.AVOSCloud;

import com.avos.avoscloud.AVObject;

import com.bjw.bean.AssistBean;

import com.bjw.bean.ComBean;

import android.app.Activity;

import android.content.Context;

import android.content.SharedPreferences;

import android.content.pm.PackageInfo;

import android.content.pm.PackageManager;

import android.content.pm.PackageManager.NameNotFoundException;

import android.content.res.Configuration;

import android.os.Bundle;

import android.text.InputType;

import android.text.method.KeyListener;

import android.text.method.NumberKeyListener;

import android.text.method.TextKeyListener;

import android.text.method.TextKeyListener.Capitalize;

import android.util.Base64;
import android.util.Log;

import android.view.KeyEvent;

import android.view.View;

import android.widget.AdapterView;

import android.widget.ArrayAdapter;

import android.widget.Button;
import android.widget.CheckBox;

import android.widget.CompoundButton;

import android.widget.EditText;

import android.widget.RadioButton;

import android.widget.Spinner;

import android.widget.TextView;

import android.widget.Toast;

import android.widget.ToggleButton;

import android_serialport_api.SerialPortFinder;

public class ComAssistantActivity extends Activity {

	FileOutputStream fos;
	public static int counterdog = 0;
	public static int yon = 1;
	EditText editTextRecDisp, editTextLines, editTextCOMA, editTextTimeCOMA;
	CheckBox checkBoxAutoClear, checkBoxAutoCOMA;
	Button ButtonClear, ButtonSendCOMA;
	ToggleButton toggleButtonCOMA;
	Spinner SpinnerCOMA, SpinnerBaudRateCOMA;
	RadioButton radioButtonTxt, radioButtonHex;
	SerialControl ComA;// 一个串口类对象，该类继承了辅助类SerialHelper
	DispQueueThread DispQueue;
	SerialPortFinder mSerialPortFinder;// 串口设备搜索
	AssistBean AssistData;// 用于界面数据序列化和反序列化
	int iRecLines = 0;// 接收区行数、
	boolean flag=false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		ComA = new SerialControl();// 串口控制类
		DispQueue = new DispQueueThread();
		DispQueue.start();
		AssistData = getAssistData();// 获取界面数据
		setControls();// 设置,用方法封装
		try {
			fos = new FileOutputStream("newFile.txt");//新建一个文件输出流还有一个文件名，写
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		AVOSCloud.initialize(this,
				"ayk5411ufeh28fn09xxxy2pynq1a8r886rt962y32olujq1u",
				"odbmx06t8j1si9zgy48crt4xfj2l46wnd59h0pv19j9lek94");
		AVAnalytics.trackAppOpened(getIntent());
		// 设置默认打开的Activity，通过调用以下代码启动推送服务----Chan
		PushService.setDefaultPushCallback(this, ComAssistantActivity.class);
		// 在保存 Installation 之前调用PushService.subscribe方法
		// 订阅频道，当该频道消息到来的时候，打开对应的 Activity
		PushService.subscribe(this, "private", ComAssistantActivity.class);
		// 保存InstallationId----Chan
		AVInstallation.getCurrentInstallation().saveInBackground(
				new SaveCallback() {

					@Override
					public void done(AVException arg0) {
						AVInstallation.getCurrentInstallation()
								.saveInBackground();

					}
				});

	}

	@Override
	public void onDestroy() {
		saveAssistData(AssistData);
		CloseComPort(ComA);
		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		CloseComPort(ComA);
		setContentView(R.layout.main);
		setControls();
	}

	private void setControls() {
		String appName = getString(R.string.app_name);
		try {//获取版本号,可以不用
			PackageInfo pinfo = getPackageManager().getPackageInfo(
					"com.bjw.ComAssistant", PackageManager.GET_CONFIGURATIONS);
			String versionName = pinfo.versionName;
			setTitle(appName + " V" + versionName);//底层方法
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		editTextRecDisp = (EditText) findViewById(R.id.editTextRecDisp);
		editTextLines = (EditText) findViewById(R.id.editTextLines);
		editTextCOMA = (EditText) findViewById(R.id.editTextCOMA);
		editTextTimeCOMA = (EditText) findViewById(R.id.editTextTimeCOMA);
		checkBoxAutoClear = (CheckBox) findViewById(R.id.checkBoxAutoClear);
		checkBoxAutoCOMA = (CheckBox) findViewById(R.id.checkBoxAutoCOMA);
		ButtonClear = (Button) findViewById(R.id.ButtonClear);
		ButtonSendCOMA = (Button) findViewById(R.id.ButtonSendCOMA);
		toggleButtonCOMA = (ToggleButton) findViewById(R.id.toggleButtonCOMA);
		SpinnerCOMA = (Spinner) findViewById(R.id.SpinnerCOMA);
		SpinnerBaudRateCOMA = (Spinner) findViewById(R.id.SpinnerBaudRateCOMA);
		radioButtonTxt = (RadioButton) findViewById(R.id.radioButtonTxt);
		radioButtonHex = (RadioButton) findViewById(R.id.radioButtonHex);
		//监听
		editTextCOMA.setOnEditorActionListener(new EditorActionEvent());
		editTextTimeCOMA.setOnEditorActionListener(new EditorActionEvent());
		editTextCOMA.setOnFocusChangeListener(new FocusChangeEvent());
		editTextTimeCOMA.setOnFocusChangeListener(new FocusChangeEvent());
		radioButtonTxt.setOnClickListener(new radioButtonClickEvent());
		radioButtonHex.setOnClickListener(new radioButtonClickEvent());
		ButtonClear.setOnClickListener(new ButtonClickEvent());
		ButtonSendCOMA.setOnClickListener(new ButtonClickEvent());
		toggleButtonCOMA
				.setOnCheckedChangeListener(new ToggleButtonCheckedChangeEvent());
		checkBoxAutoCOMA.setOnCheckedChangeListener(new CheckBoxChangeEvent());
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.baudrates_value,
				android.R.layout.simple_spinner_item);//最后的是规定的？
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);//自定义下拉菜单里的样式
		SpinnerBaudRateCOMA.setAdapter(adapter);
		SpinnerBaudRateCOMA.setSelection(14);// 默认为38400
		mSerialPortFinder = new SerialPortFinder();
		String[] entryValues = mSerialPortFinder.getAllDevicesPath();
		List<String> allDevices = new ArrayList<String>();
		for (int i = 0; i < entryValues.length; i++) {
			allDevices.add(entryValues[i]);
		}

		ArrayAdapter<String> aspnDevices = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, allDevices);
		aspnDevices
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		SpinnerCOMA.setAdapter(aspnDevices);
		if (allDevices.size() > 0) {
			SpinnerCOMA.setSelection(12);// 默认ttySAC3

		}

		SpinnerCOMA.setOnItemSelectedListener(new ItemSelectedEvent());
		SpinnerBaudRateCOMA.setOnItemSelectedListener(new ItemSelectedEvent());
		DispAssistData(AssistData);

	}

	// 串口号或波特率变化时，关闭打开的串口
	class ItemSelectedEvent implements Spinner.OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if ((arg0 == SpinnerCOMA) || (arg0 == SpinnerBaudRateCOMA)) {

				CloseComPort(ComA);//关闭串口
				checkBoxAutoCOMA.setChecked(false);//让打开的按钮变为关闭状态
				toggleButtonCOMA.setChecked(false);//让打开的按钮变为关闭状态
			}

		}

		public void onNothingSelected(AdapterView<?> arg0) {

		}

	}

	class FocusChangeEvent implements EditText.OnFocusChangeListener {
		public void onFocusChange(View v, boolean hasFocus) {
			if (v == editTextCOMA) {//焦点事件，编辑框会改变颜色
				setSendData(editTextCOMA);
			} else if (v == editTextTimeCOMA) {
				setDelayTime(editTextTimeCOMA);
			}

		}

	}

	// 编辑框完成事件

	class EditorActionEvent implements EditText.OnEditorActionListener {
		// 软键盘的Enter键默认显示的是“完成”文本
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			// 在编辑完之后点击软键盘上的回车键触发该方法
			if (v == editTextCOMA) {// 判断点击的view
				setSendData(editTextCOMA);
			} else if (v == editTextTimeCOMA) {
				setDelayTime(editTextTimeCOMA);
			}

			return false;// 规定的
		}

	}

	// Txt、Hex模式选
	class radioButtonClickEvent implements RadioButton.OnClickListener {
		public void onClick(View v) {
			if (v == radioButtonTxt) {
				KeyListener TxtkeyListener = new TextKeyListener(
						Capitalize.NONE, false);
				editTextCOMA.setKeyListener(TxtkeyListener);
				AssistData.setTxtMode(true);
			} else if (v == radioButtonHex) {
				KeyListener HexkeyListener = new NumberKeyListener() {
					public int getInputType() {
						return InputType.TYPE_CLASS_TEXT;
					}

					@Override
					protected char[] getAcceptedChars() {
						return new char[] { '0', '1', '2', '3', '4', '5', '6',
								'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
								'A', 'B', 'C', 'D', 'E', 'F' };
					}
				};

				editTextCOMA.setKeyListener(HexkeyListener);
				AssistData.setTxtMode(false);
			}

			editTextCOMA.setText(AssistData.getSendA());
			setSendData(editTextCOMA);

		}

	}

	// 自动发送，需要点击

	class CheckBoxChangeEvent implements CheckBox.OnCheckedChangeListener {
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (buttonView == checkBoxAutoCOMA) {// 判断是否是自动发送按钮
				if (!toggleButtonCOMA.isChecked() && isChecked) {// 串口已经打开且已经点击了自动发送按钮
					buttonView.setChecked(false);// 改变状态
					return;

				}

				SetLoopData(ComA, editTextCOMA.getText().toString());// 自动发送
				SetAutoSend(ComA, isChecked);

			}

		}

	}

	// 清除按钮、发送按钮

	class ButtonClickEvent implements View.OnClickListener {
		public void onClick(View v) {
			if (v == ButtonClear) {// 判断是那个按钮
				editTextRecDisp.setText("");// 清除的是屏幕上串口读进来的数据，而不是推送的消息
			} else if (v == ButtonSendCOMA) {// 判断点击的是发送按钮
				flag=false;
				sendPortData(ComA, editTextCOMA.getText().toString());// 获取编辑的内容，发送
				Log.v("ABC", editTextCOMA.getText().toString());
			}
		}

	}

	// 打开或者关闭串口
	class ToggleButtonCheckedChangeEvent implements
			ToggleButton.OnCheckedChangeListener {
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (buttonView == toggleButtonCOMA) {// 判断是否是开关按钮
				if (isChecked) {// 是否按下？
					ComA.setPort(SpinnerCOMA.getSelectedItem().toString());// 获得接口参数
					ComA.setBaudRate(SpinnerBaudRateCOMA.getSelectedItem()
							.toString());// 波特率
					flag=true;
					OpenComPort(ComA);// 打开
					
				} else {
					flag=false;
					CloseComPort(ComA);
					checkBoxAutoCOMA.setChecked(false);//设置自动不能按

				}

			}

		}

	}

	// 串口控制类

	private class SerialControl extends SerialHelper {
		public SerialControl() {
		}

		@Override
		protected void onDataReceived(final ComBean ComRecData) {
			DispQueue.AddQueue(ComRecData);// 线程定时刷新显示
		}

	}

	private class DispQueueThread extends Thread {
		private Queue<ComBean> QueueList = new LinkedList<ComBean>();

		@Override
		public void run() {
			super.run();
			while (!isInterrupted()) {
				final ComBean ComData;
				while ((ComData = QueueList.poll()) != null) {
					runOnUiThread(new Runnable() {
						public void run() {
							DispRecData(ComData);

						}

					});

					try {
						Thread.sleep(100);

					} catch (Exception e) {
						e.printStackTrace();

					}

					break;

				}

			}

		}

		public synchronized void AddQueue(ComBean ComData) {

			QueueList.add(ComData);

		}

	}

	private void DispAssistData(AssistBean AssistData) {
		editTextCOMA.setText(AssistData.getSendA());
		setSendData(editTextCOMA);
		if (AssistData.isTxt()) {
			radioButtonTxt.setChecked(true);
		} else {
			radioButtonHex.setChecked(true);
		}
		editTextTimeCOMA.setText(AssistData.sTimeA);
		setDelayTime(editTextTimeCOMA);
	}

	// 保存界面数据
	private void saveAssistData(AssistBean AssistData) {
		AssistData.sTimeA = editTextTimeCOMA.getText().toString();// 设置时间
		SharedPreferences msharedPreferences = getSharedPreferences(
				"ComAssistant", Context.MODE_PRIVATE);

		try {
			//ObjectOutputStream只能对Serializable接口的类的对象进行序列化
			ByteArrayOutputStream baos = new ByteArrayOutputStream();// 字节流
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(AssistData);//对参数指定的obj对象进行序列化(对象转换为二进制)，
			//把得到的字节序列写到一个目标输出流中。
			String sBase64 = new String(Base64.encode(baos.toByteArray(), 0));
			SharedPreferences.Editor editor = msharedPreferences.edit();
			editor.putString("AssistData", sBase64);
			editor.commit();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// 获取界面数据

	private AssistBean getAssistData() {
		SharedPreferences msharedPreferences = getSharedPreferences(
				"ComAssistant", Context.MODE_PRIVATE);
		AssistBean AssistData = new AssistBean();
		try {
			String personBase64 = msharedPreferences
					.getString("AssistData", "");
			byte[] base64Bytes = Base64.decode(personBase64.getBytes(), 0);
			ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			AssistData = (AssistBean) ois.readObject();
			return AssistData;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return AssistData;
	}

	// 设置自动发送延时
	private void setDelayTime(TextView v) {
		AssistData.sTimeA = v.getText().toString();
		SetiDelayTime(ComA, v.getText().toString());
	}

	// 设置自动发送数据

	private void setSendData(TextView v) {// 要先编辑
		AssistData.setSendA(v.getText().toString());// 拿到编辑文本内容,在这里设置获取云端的消息
		// 拿到编辑文本内容,在这里设置获取云端的消息
		SetLoopData(ComA, v.getText().toString());
		// 获取之后就自动发送？
		// SetLoopData(ComA, new OrderReceive().mMessage);// 获取之后就自动发送
	}

	// 设置自动发送延时,可以保留

	private void SetiDelayTime(SerialHelper ComPort, String sTime) {
		ComPort.setiDelay(Integer.parseInt(sTime));
	}

	// 设置自动发送数据

	private void SetLoopData(SerialHelper ComPort, String sLoopData) {
		if (radioButtonTxt.isChecked()) {
			ComPort.setTxtLoopData(sLoopData);// 输送获得的字符串

		} else if (radioButtonHex.isChecked()) {
			ComPort.setHexLoopData(sLoopData);
		}
	}

	// private int counter = 0;
	// 显示接收数据
	private void DispRecData(ComBean ComRecData) {
		StringBuilder sMsg = new StringBuilder();
		sMsg.append(ComRecData.sRecTime);
		sMsg.append("[");
		sMsg.append(ComRecData.sComPort);
		sMsg.append("]");
		if (radioButtonTxt.isChecked()) {
			sMsg.append("[Txt] ");
			sMsg.append(new String(ComRecData.bRec));
		} else if (radioButtonHex.isChecked()) {
			sMsg.append("[Hex] ");
			// try{
			// fos.write(ComRecData.bRec);
			// }catch (IOException ioe)
			// {
			// ioe.printStackTrace();
			// }
			// for(int i=0;i<10;i++)
			// System.out.println(MyFunc.ByteArrToHex(ComRecData.bRec));
			// sMsg.append(MyFunc.ByteArrToHex(ComRecData.bRec));
			// if(counter>5){
			// String ="FB";
			// String str = new String(SerialHelper.sb);
			// int startfb=SerialHelper.sb.indexOf("FB");
			// if(startfb>0){
			// SerialHelper.sb.delete(0, startfb+2);
			// int secondfb=SerialHelper.sb.indexOf("FB");
			// String str=SerialHelper.sb.substring(0, secondfb);
			// SerialHelper.sb
			// }
			//
			// String substr= str.substring(0, 16);
			// SerialHelper.sb.delete(0, 16);
			// sMsg.append(substr);
			// }else{
			// counter++;
			// }
			sMsg.append(SerialHelper.str);
			// AVObject testObject = new AVObject("ZigbeeCollectedData");
			// testObject.put("data", SerialHelper.str);
			// testObject.saveInBackground();
		}
		sMsg.append("\r\n");
		// editTextRecDisp.append(sMsg);
		iRecLines++;
		if (SerialHelper.str != "") {
			editTextRecDisp.append(sMsg);
			editTextLines.setText(String.valueOf(iRecLines));
			AVObject testObject = new AVObject("ZigbeeCollectedData");
			testObject.put("data", SerialHelper.str);
			testObject.saveInBackground();
		}
		if ((iRecLines > 500) && (checkBoxAutoClear.isChecked())) {// 达到500项自动清除

			editTextRecDisp.setText("");
			editTextLines.setText("0");
			iRecLines = 0;
		}
	}

	private void SetAutoSend(SerialHelper ComPort, boolean isAutoSend) {
		if (isAutoSend) {// 判断是否已经点击了自动按钮

			ComPort.startSend();
		} else {
			ComPort.stopSend();
		}
	}

	// 串口发送，按发送按钮，就调用该方法

	private void sendPortData(SerialHelper ComPort, String sOut) {
		if (ComPort != null && ComPort.isOpen()) {// 串口需要先打开

			// if (radioButtonTxt.isChecked())
			// {
			
			ComPort.sendTxt(sOut);// 通过调用SerialHelper的底层方法，将控制指令写出
			
			// }else if (radioButtonHex.isChecked()) {
			// ComPort.sendHex(sOut);
			// }
		}
	}

	// 关闭串口

	private void CloseComPort(SerialHelper ComPort) {
		if (ComPort != null) {
			ComPort.stopSend();
			ComPort.close();//关闭串口
		}
	}

	// 打开串口

	private void OpenComPort(SerialHelper ComPort) {
		try {
			ComPort.open();//打开串口
			new Thread(new Runnable() {

				public void run() {
					try {
						while (flag) {
							sendPortData(ComA, new OrderReceive().mMessage);
							Thread.sleep(500);
							Log.v("abc", new OrderReceive().mMessage);
						}

					} catch (Exception e) {

					}

				}
			}).start();

		} catch (SecurityException e) {
			ShowMessage("打开串口失败:没有串口读/写权限!");
		} catch (IOException e) {
			ShowMessage("打开串口失败:未知错误!");
		} catch (InvalidParameterException e) {
			ShowMessage("打开串口失败:参数错误!");
		}
	}

	private void ShowMessage(String sMsg) {
		Toast.makeText(this, sMsg, Toast.LENGTH_SHORT).show();
	}
}