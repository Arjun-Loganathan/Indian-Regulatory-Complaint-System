package com.ircs.complaints;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.ircs.complaint.R;

public class MainActivity extends Activity {

	String myDate;


	ListView list;
	String[] web = {
			"TNEB",
			"TWAD",
			"TRAFFIC",
			"POLICE",
			"TNSTC",
			"CHILD_LABOR"
	} ;
	String[] webex = {
			"Tamilnadu Electricity Board",
			"Tamil Nadu Water Supply and Drainage Board",
			"Chennai Metropolitan Traffic Police",
			"Tamil Nadu Police",
			"Tamil Nadu State Transport Corporation",
			"Child labour in India"
	} ;
	Integer[] imageId = {
			R.drawable.tneb,
			R.drawable.twad,
			R.drawable.traffic,
			R.drawable.police,
			R.drawable.tnstc,
			R.drawable.child,
	};


	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			System.out.println("*** My thread is now configured to allow connection");
		}

		CustomList adapter = new
				CustomList(MainActivity.this, web, webex, imageId);
		list=(ListView)findViewById(R.id.list);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				String title = web[+ position];

				// Starting new intent
				Intent in = new Intent(getApplicationContext(),
						CameraPhotoCapture.class);

				in.putExtra("title", title);

				startActivity(in);
				Toast.makeText(MainActivity.this, "You Clicked at " +web[+ position], Toast.LENGTH_SHORT).show();
				Toast.makeText(MainActivity.this, title, Toast.LENGTH_SHORT).show();
			}
		});
	}





	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


}
