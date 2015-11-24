package com.bjw.ComAssistant;

import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class OrderReceive extends BroadcastReceiver{
	public static String mMessage="";
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			if (intent.getAction().equals("com.chan.action")) {// 如果和发送的相等
				JSONObject json = new JSONObject(intent.getExtras().getString(
						"com.avos.avoscloud.Data"));// 规定的，这样下面的语句才能获得消息
				final String message = json.getString("message");// 获得发送的消息
				mMessage=message;
				Toast.makeText(context, message, Toast.LENGTH_LONG).show();//获取没问题
			}
		} catch (Exception e) {
			
		}
		
	}

}
