package com.prabhu.womensafetyapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set StrictMode policies for debugging
		if (BuildConfig.DEBUG) {
			StrictMode.setThreadPolicy(
					new StrictMode.ThreadPolicy.Builder()
							.detectAll()   // Detect all problems
							.penaltyLog()  // Log detected problems
							.build()
			);
			StrictMode.setVmPolicy(
					new StrictMode.VmPolicy.Builder()
							.detectAll()
							.penaltyLog()
							.build()
			);
		}
	}

	public void register(View v) {
		Intent intentRegister = new Intent(MainActivity.this, Register.class);
		startActivity(intentRegister);
	}

	public void displayNo(View v) { // Method name should be displayNo
		Intent intentView = new Intent(MainActivity.this, Display.class);
		startActivity(intentView);
	}

	public void instruct(View v) {
		Intent intentHelp = new Intent(MainActivity.this, Instructions.class);
		startActivity(intentHelp);
	}

	public void verify(View v) {
		Intent intentVerify = new Intent(MainActivity.this, Verify.class);
		startActivity(intentVerify);
	}
}
