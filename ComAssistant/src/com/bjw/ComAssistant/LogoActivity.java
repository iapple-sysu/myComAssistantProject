package com.bjw.ComAssistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class LogoActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.logo);
		new Handler().postDelayed(new Runnable() {
			
			public void run() {
				// TODO Auto-generated method stub
				Intent intent=new Intent(LogoActivity.this,ComAssistantActivity.class);
				startActivity(intent);
				LogoActivity.this.finish();
			}
		}, 3000);
	}
}
