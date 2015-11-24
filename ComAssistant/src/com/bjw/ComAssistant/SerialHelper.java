package com.bjw.ComAssistant;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import com.avos.avoscloud.AVObject;
import com.bjw.bean.ComBean;

import android_serialport_api.SerialPort;

/**
 *���ڸ���������
 */
public abstract class SerialHelper{
//	public StringBuilder sbhex = new StringBuilder();
//	private int counter = 0;
	public static StringBuilder sb = new StringBuilder();
	public static String str ="";
	public static String hstr="";
	private SerialPort mSerialPort;
	private OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread;
	private SendThread mSendThread;
	private String sPort="/dev/s3c2410_serial0";
	private int iBaudRate=9600;
	private boolean _isOpen=false;
	private byte[] _bLoopData=new byte[]{0x30};
	private int iDelay=500;
	//----------------------------------------------------
	public SerialHelper(String sPort,int iBaudRate){
		this.sPort = sPort;
		this.iBaudRate=iBaudRate;
	}
	public SerialHelper(){
		this("/dev/s3c2410_serial0",9600);
	}
	public SerialHelper(String sPort){
		this(sPort,9600);
	}
	public SerialHelper(String sPort,String sBaudRate){
		this(sPort,Integer.parseInt(sBaudRate));
	}
	//----------------------------------------------------
	public void open() throws SecurityException, IOException,InvalidParameterException{
		mSerialPort =  new SerialPort(new File(sPort), iBaudRate, 0);
		mOutputStream = mSerialPort.getOutputStream();
		mInputStream = mSerialPort.getInputStream();
		mReadThread = new ReadThread();
		mReadThread.start();//开启读线程
//		mSendThread = new SendThread();
//		mSendThread.setSuspendFlag();
//		mSendThread.start();
		_isOpen=true;
	}
	//----------------------------------------------------
	public void close(){
		if (mReadThread != null)
			mReadThread.interrupt();
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
		_isOpen=false;
	}
	//----------------------------------------------------
	public void send(byte[] bOutArray){
		try
		{
			mOutputStream.write(bOutArray);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	//----------------------------------------------------
	public void sendHex(String sHex){
		byte[] bOutArray = MyFunc.HexToByteArr(sHex);
		send(bOutArray);		
	}
	//----------------------------------------------------
	public void sendTxt(String sTxt){
		byte[] bOutArray =sTxt.getBytes();
		send(bOutArray);		
	}
	//----------------------------------------------------
	private class ReadThread extends Thread {
		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				try
				{
					if (mInputStream == null) return;
					try
					{
							Thread.sleep(900);
					} catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					BufferedInputStream bis = new BufferedInputStream(mInputStream,17280);
					
//					FileOutputStream fos = new FileOutputStream("datas.txt");
//					ComAssistantActivity.yon=1;
					byte[] buffer=new byte[76];
					int size =bis.read(buffer);
					if(size<=8){
						StringBuilder sbhex = new StringBuilder();
						for (int i = 0; i < size; i++)
						{
							sbhex.append(MyFunc.Byte2Hex(buffer[i]));
							//strBuilder.append(" ");
						}
						hstr=sbhex.toString();
						str="";
						ComAssistantActivity.yon=2;
					}else if(ComAssistantActivity.yon==2){
						StringBuilder sbhex = new StringBuilder();
						for (int i = 0; i < size; i++)
						{
							sbhex.append(MyFunc.Byte2Hex(buffer[i]));
							//strBuilder.append(" ");
						}
						str=hstr+sbhex.toString();
						ComAssistantActivity.yon=1;
					}else{
						StringBuilder sbhex = new StringBuilder();
						for (int i = 0; i < size; i++)
						{
							sbhex.append(MyFunc.Byte2Hex(buffer[i]));
							//strBuilder.append(" ");
						}
						hstr="";
						str=sbhex.toString();
						ComAssistantActivity.yon=1;
					}
					
//					byte[] buffer1=new byte[76];
//					int size1 = bis.read(buffer1);
//					if(size1<=8){
//						try
//						{
//								Thread.sleep(50);
//						} catch (InterruptedException e)
//						{
//							e.printStackTrace();
//						}
//						byte[] buffer2 = new byte[76];
//						int size2 = bis.read(buffer2);
//						size=size1+size2;
//						buffer=new byte[size];
//						for(int i=0;i<size;i++){
//							if(i<size1)
//								buffer[i]=buffer1[i];
//							else
//								buffer[i]=buffer2[size-size1];
//						}
//					}
//					else{
//						size=size1;
//						buffer=new byte[size];
//						for(int i=0;i<size;i++)
//							buffer[i]=buffer1[i];
//					}
					
					
					
//					fos.write(buffer);
//					int j=buffer.length;
//					int j=size;
//					StringBuilder sbhex = new StringBuilder();
//					for (int i = 0; i < j; i++)
//					{
//						sbhex.append(MyFunc.Byte2Hex(buffer[i]));
//						//strBuilder.append(" ");
//					}
//					
//					int ffb=sbhex.indexOf("FB");
//					if(ffb>0){
//						sbhex.replace(ffb-1, ffb+1, "BF");
//						int sfb=sbhex.indexOf("FB");
//						if(sfb-ffb>=16){
//							str=sbhex.substring(ffb+2, sfb);
//							str="FB"+str;
//						}
//					}
//					sb.append(sbhex.toString());
//					if(counter>5){
//						int startfb=sb.indexOf("FB");
//						sb.delete(0, startfb+2);
//						int secondfb=sb.indexOf("FB");
//						String senddata=sb.substring(0, secondfb);
//						str="FB"+secondfb;
//					}else{
//						counter++;
//					}
					
					
					
//					str = sbhex.toString();
					//System.out.println("***");
//					System.out.println(sb);
//					String strr = MyFunc.ByteArrToHex(buffer);
//					if(strr=="FB"){
//						str = strr;
//					}
					//for(int i=0;i<20;i++){
					//	System.out.println(str);
					//}
					//System.out.println("*****");

//					if ((size>15)&&(size<19)){
						ComBean ComRecData = new ComBean(sPort,buffer,size);
						onDataReceived(ComRecData);
//						System.out.println(size);

						StringBuilder strBuilder=new StringBuilder();
						for (int i = 0; i < size; i++)
						{
							strBuilder.append(String.format("%02x", buffer[i]).toUpperCase());
							//strBuilder.append(" ");
						}
//						AVObject testObject = new AVObject("ZigbeeCollectedData");
//						testObject.put("data", strBuilder.toString());
//						testObject.saveInBackground();
//					}
//					try
//					{
//						Thread.sleep(400);
//					} catch (InterruptedException e)
//					{
//						e.printStackTrace();
//					}
				} catch (Throwable e)
				{
					e.printStackTrace();
					return;
				}
			}
			
			
		}
	}
	//----------------------------------------------------
	private class SendThread extends Thread{
		public boolean suspendFlag = true;// �����̵߳�ִ��
		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				synchronized (this)
				{
					while (suspendFlag)
					{
						try
						{
							wait();
						} catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
				}
				send(getbLoopData());
				try
				{
					Thread.sleep(iDelay);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}

		//�߳���ͣ
		public void setSuspendFlag() {
			this.suspendFlag = true;
		}

		//�����߳�
		public synchronized void setResume() {
			this.suspendFlag = false;
			notify();
		}
	}
	//----------------------------------------------------
	public int getBaudRate()
	{
		return iBaudRate;
	}
	public boolean setBaudRate(int iBaud)
	{
		if (_isOpen)
		{
			return false;
		} else
		{
			iBaudRate = iBaud;
			return true;
		}
	}
	public boolean setBaudRate(String sBaud)
	{
		int iBaud = Integer.parseInt(sBaud);
		return setBaudRate(iBaud);
	}
	//----------------------------------------------------
	public String getPort()
	{
		return sPort;
	}
	public boolean setPort(String sPort)
	{
		if (_isOpen)
		{
			return false;
		} else
		{
			this.sPort = sPort;
			return true;
		}
	}
	//----------------------------------------------------
	public boolean isOpen()
	{
		return _isOpen;
	}
	//----------------------------------------------------
	public byte[] getbLoopData()
	{
		return _bLoopData;
	}
	//----------------------------------------------------
	public void setbLoopData(byte[] bLoopData)
	{
		this._bLoopData = bLoopData;
	}
	//----------------------------------------------------
	public void setTxtLoopData(String sTxt){
		this._bLoopData = sTxt.getBytes();
	}
	//----------------------------------------------------
	public void setHexLoopData(String sHex){
		this._bLoopData = MyFunc.HexToByteArr(sHex);
	}
	//----------------------------------------------------
	public int getiDelay()
	{
		return iDelay;
	}
	//----------------------------------------------------
	public void setiDelay(int iDelay)
	{
		this.iDelay = iDelay;
	}
	//----------------------------------------------------
	public void startSend()
	{
		if (mSendThread != null)
		{
			mSendThread.setResume();
		}
	}
	//----------------------------------------------------
	public void stopSend()
	{
		if (mSendThread != null)
		{
			mSendThread.setSuspendFlag();
		}
	}
	//----------------------------------------------------
	protected abstract void onDataReceived(ComBean ComRecData);
}