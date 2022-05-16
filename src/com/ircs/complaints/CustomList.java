package com.ircs.complaints;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ircs.complaint.R;

public class CustomList extends ArrayAdapter<String>{
	private final Activity context;
	private final String[] web;
	private final String[] webex;
	private final Integer[] imageId;

	public CustomList(Activity context, String[] web,
			String[] webex, Integer[] imageId) {
		super(context, R.layout.list_single, web);
		this.context = context;
		this.web = web;
		this.webex=webex;
		this.imageId = imageId;
		
	}
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView= inflater.inflate(R.layout.list_single, null, true);
		TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
		TextView txtEx = (TextView) rowView.findViewById(R.id.txtex);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
		txtTitle.setText(web[position]);
		txtEx.setText(webex[position]);
		imageView.setImageResource(imageId[position]);
		return rowView;
	}
}