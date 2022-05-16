package com.ircs.complaints;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.ircs.complaint.R;

public class MenuActivity extends Activity implements OnClickListener{
	Button emg_btn,nor_btn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.category_layout);

		emg_btn=(Button)findViewById(R.id.emg_btn);
		nor_btn=(Button)findViewById(R.id.ord_btn);




		emg_btn.setOnClickListener(this);
		nor_btn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.emg_btn:
			Intent menu_intent=new Intent(MenuActivity.this,MyListActivity.class);
			startActivity(menu_intent);
			break;
		case R.id.ord_btn:
			Intent map_intent=new Intent(MenuActivity.this,MainActivity.class);
			startActivity(map_intent);
			break;
		default:
			break;
		}

	}



}
