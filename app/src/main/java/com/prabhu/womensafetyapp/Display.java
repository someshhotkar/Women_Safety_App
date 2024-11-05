package com.prabhu.womensafetyapp;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;

public class Display extends Activity {

	Cursor c;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display);

		// Open or create the SQLite database
		SQLiteDatabase db;
		db = openOrCreateDatabase("NumDB", Context.MODE_PRIVATE, null);

		// Query the database for records in the 'details' table
		c = db.rawQuery("SELECT * FROM details", null);
		if (c.getCount() == 0) {
			showMessage("Error", "No records found.");
			return;
		}

		// Append the database content into a buffer
		StringBuffer buffer = new StringBuffer();
		while (c.moveToNext()) {
			buffer.append("Name: " + c.getString(0) + "\n");
			buffer.append("Number: " + c.getString(1) + "\n");
		}

		// Display the fetched data
		showMessage("Details", buffer.toString());

		// Start a background service
		Intent i_startservice = new Intent(Display.this, BgService.class);
		startService(i_startservice);
	}

	// Show message method for displaying alerts
	public void showMessage(String title, String message) {
		Builder builder = new Builder(this);
		builder.setCancelable(true);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.show();
	}

	// Handle back button click, navigate to MainActivity
	public void back(View v) {
		Intent i_back = new Intent(Display.this, MainActivity.class);
		startActivity(i_back);
		finish();  // Close this activity
	}
}
