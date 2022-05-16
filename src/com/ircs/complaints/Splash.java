package com.ircs.complaints;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ircs.complaint.R;

public class Splash extends Activity implements AnimationListener {


	ImageView imgLogo;
	TextView txt;
	Button btnStart;
	// Animation
	Animation animTogether;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_activity);
		imgLogo = (ImageView) findViewById(R.id.imageView1);
		txt=(TextView)findViewById(R.id.txt);
		animTogether = AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.rotate);

		// set animation listener
		animTogether.setAnimationListener(this);


		imgLogo.startAnimation(animTogether);
		imgLogo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent main_Intent=new Intent(Splash.this,MenuActivity.class);
				startActivity(main_Intent);
			}
		});
		
//		Thread splash_thread=new Thread()
//		{
//			public void run()
//			{
//				try
//				{
//					sleep(5000);
//				}
//				catch(Exception e)
//				{
//					e.printStackTrace();
//				}
//				finally
//				{
//					Intent main_Intent=new Intent(Splash.this,MainActivity.class);
//					startActivity(main_Intent);
//				}
//			}
//		};
//		splash_thread.start();
	}

	@Override
	public void onAnimationEnd(Animation arg0) {
		// TODO Auto-generated method stub
		// check for zoom in animation

	}

	@Override
	public void onAnimationRepeat(Animation arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationStart(Animation arg0) {
		// TODO Auto-generated method stub

	}
}
