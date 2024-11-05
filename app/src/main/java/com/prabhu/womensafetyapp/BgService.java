package com.prabhu.womensafetyapp;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.Nullable;

@SuppressLint("HandlerLeak")
public class BgService extends Service implements AccelerometerListener {

	private static final String TAG = "BgService";
	private String str_address;
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;

	// Handler that receives messages from the thread.
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			// No specific message handling for this example.
			stopSelf(msg.arg1);
		}
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null; // We don't provide binding, so return null.
	}

	@Override
	public void onCreate() {
		super.onCreate();

		if (AccelerometerManager.isSupported(this)) {
			AccelerometerManager.startListening(this);
		}

		HandlerThread thread = new HandlerThread("ServiceStartArguments", android.os.Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		mServiceHandler.sendMessage(msg);

		return START_STICKY; // Ensures service keeps running until explicitly stopped.
	}

	// Handler to process Geocoder results
	public class GeocoderHandler extends Handler {
		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
				case 1:
					Bundle bundle = message.getData();
					str_address = bundle.getString("address");

					SQLiteDatabase db = null;
					Cursor c = null;
					Cursor c1 = null;

					try {
						db = openOrCreateDatabase("NumDB", Context.MODE_PRIVATE, null);
						c = db.rawQuery("SELECT * FROM details", null);
						c1 = db.rawQuery("SELECT * FROM SOURCE", null);

						if (c1 != null && c1.moveToFirst()) {
							String source_ph_number = c1.getString(0);

							while (c != null && c.moveToNext()) {
								String target_ph_number = c.getString(1);

								// Send SMS with location details
								SmsManager smsManager = SmsManager.getDefault();
								String messageBody = "Please help me. I need help immediately. " +
										"This is where I am now: " + str_address;

								// Check if the target phone number is valid before sending the message
								if (target_ph_number != null && !target_ph_number.isEmpty()) {
									smsManager.sendTextMessage(target_ph_number, source_ph_number, messageBody, null, null);
									Log.d(TAG, "SMS sent to " + target_ph_number);
								} else {
									Log.e(TAG, "Invalid target phone number");
								}
							}
						}
					} catch (Exception e) {
						Log.e(TAG, "Error accessing database or sending SMS", e);
					} finally {
						if (c != null) c.close();
						if (c1 != null) c1.close();
						if (db != null) db.close();
					}
					break;

				default:
					str_address = null;
			}
		}
	}

	// Triggered when there is a change in acceleration (shake detection)
	@Override
	public void onAccelerationChanged(float x, float y, float z) {
		// No action needed for simple acceleration change.
	}

	// Triggered when a shake is detected
	@Override
	public void onShake(float force) {
		GPSTracker gps = new GPSTracker(BgService.this);
		if (gps.canGetLocation()) {
			double latitude = gps.getLatitude();
			double longitude = gps.getLongitude();

			// Reverse geocoding to get the address
			RGeocoder RGeocoder = new RGeocoder();
			RGeocoder.getAddressFromLocation(latitude, longitude, getApplicationContext(), new GeocoderHandler());
		} else {
			gps.showSettingsAlert();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (AccelerometerManager.isListening()) {
			AccelerometerManager.stopListening();
		}
	}
}
