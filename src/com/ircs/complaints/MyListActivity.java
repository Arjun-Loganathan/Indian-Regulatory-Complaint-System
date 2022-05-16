package com.ircs.complaints;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.ircs.complaint.R;

public class MyListActivity extends Activity {
	
	public SQLiteDatabase db;
	// flag for Internet connection status
	Boolean isInternetPresent = false;

	// Connection detector class
	ConnectionDetector cd;
	
	// Alert Dialog Manager
	AlertDialogManager alert = new AlertDialogManager();

	// Google Places
	GooglePlaces googlePlaces;

	// Places List
	PlacesList nearPlaces;

	// GPS Location
	GPSTracker gps;

	// Button
	Button btnShowOnMap;

	// Progress dialog
	ProgressDialog pDialog;
	
	// Places Listview
	ListView list;
	
	// ListItems data
	ArrayList<HashMap<String, String>> placesListItems = new ArrayList<HashMap<String,String>>();
	public Double lati_do;
	public Double longe_do;
	public String value;
	
	// KEY Strings
	public static String KEY_REFERENCE = "reference"; // id of the place
	public static String KEY_NAME="name"; // name of the place
	public static String KEY_VICINITY = "vicinity"; // Place area name

	String lat="";
	String longet="";
	String name="";
	String city="";
	Place place;
	String[] placename;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_activity);
		
		
		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		isInternetPresent = cd.isConnectingToInternet();
		if (!isInternetPresent) {
			// Internet Connection is not present
			alert.showAlertDialog(MyListActivity.this, "Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}

		// creating GPS Class object
		gps = new GPSTracker(this);

		// check if GPS location can get
		if (gps.canGetLocation()) {
			Log.d("Your Location", "latitude:" + gps.getLatitude() + ", longitude: " + gps.getLongitude());
		} else {
			// Can't get user's current location
			alert.showAlertDialog(MyListActivity.this, "GPS Status",
					"Couldn't get location information. Please enable GPS",
					false);
			// stop executing code by return
			return;
		}
		
		Toast.makeText(getApplicationContext(), "lat"+longet, Toast.LENGTH_SHORT).show();
		try
		{
		lati_do=gps.getLatitude();
		longe_do=gps.getLongitude();
		
		Log.e("",""+lati_do+ longe_do);
		}
		catch(Exception e)
		{
			
		}
		
		new LoadPlaces().execute();


		list=(ListView)findViewById(R.id.list);
		

		// Adding data into listview
		
		list.setOnItemClickListener(new OnItemClickListener() {

		    @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {

		    	Intent i = new Intent(getApplicationContext(),
						PlacesMapActivity.class);
				// Sending user current geo location
				i.putExtra("user_latitude", Double.toString(gps.getLatitude()));
				i.putExtra("user_longitude", Double.toString(gps.getLongitude()));
				
//				i.putExtra("place_latitude", Double.toString(lati_do));
//				i.putExtra("place_longitude", Double.toString(longe_do));
				// passing near places to map activity
				i.putExtra("near_places", nearPlaces);
				// staring activity
				startActivity(i);
            }
        });
			
		 		
	    	
		list.setOnItemLongClickListener(new OnItemLongClickListener() {
	        @Override
	        public boolean onItemLongClick(AdapterView<?> parent, View arg1,
	                int position, long arg3) {
	        	Intent i = new Intent(getApplicationContext(),
						PlacesMapActivity.class);
				// Sending user current geo location
				i.putExtra("user_latitude", Double.toString(gps.getLatitude()));
				i.putExtra("user_longitude", Double.toString(gps.getLongitude()));
				
//				i.putExtra("place_latitude", Double.toString(lati_do));
//				i.putExtra("place_longitude", Double.toString(longe_do));
				// passing near places to map activity
				i.putExtra("near_places", nearPlaces);
				// staring activity
				startActivity(i);
	        	
	            return false;
	        }
	    });
	}


	 @SuppressWarnings("unchecked")
	private ArrayList getAllCotacts() {
		// TODO Auto-generated method stub
		 ArrayList array_list = new ArrayList();
			try {
			String getMeaningquery="select place from maptable";
			db = openOrCreateDatabase("mapdb.db", MODE_PRIVATE, null);
		   	String see = "create table if not exists maptable(docid integer primary key autoincrement,place varchar(120) not null,reference varchar(2000) not null)";
		   	db.execSQL(see);
			
		   	
		   Cursor res =  db.rawQuery(getMeaningquery, null );
	      res.moveToFirst();
	      while(res.isAfterLast() == false){
	    	  array_list.add(res.getString(res.getColumnIndex("place")));
	      res.moveToNext();
	      }
	      res.close();
		
		}catch(SQLException sqle){
		 				throw sqle;
		}finally {
			
			if(db!=null)
				db.close();
		}
			return array_list;
	}


	 class LoadPlaces extends AsyncTask<String, String, String> {

			/**
			 * Before starting background thread Show Progress Dialog
			 * */
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				pDialog = new ProgressDialog(MyListActivity.this);
				pDialog.setMessage(Html.fromHtml("<b>Search</b><br/>Loading Places..."));
				pDialog.setIndeterminate(false);
				pDialog.setCancelable(false);
				pDialog.show();
			}

			/**
			 * getting Places JSON
			 * */
			protected String doInBackground(String... args) {
			
				// creating Places class object
				googlePlaces = new GooglePlaces();
				
				try {
					// Separeate your place types by PIPE symbol "|"
					// If you want all types places make it as null
					// Check list of types supported by google
					// 
					String types = "atm|bus_station|airport|hospital|church|mosque|police"; // Listing places only cafes, restaurants
					
					// Radius in meters - increase this value if you don't find any places
					double radius = 1000; // 1000 meters 
					
					// get nearest places

					nearPlaces = googlePlaces.search(lati_do,
							longe_do, radius, types);
					  Log.e("latitude============================================================", ""+nearPlaces);
			

				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			/**
			 * After completing background task Dismiss the progress dialog
			 * and show the data in UI
			 * Always use runOnUiThread(new Runnable()) to update UI from background
			 * thread, otherwise you will get error
			 * **/
			protected void onPostExecute(String file_url) {
				// dismiss the dialog after getting all products
				pDialog.dismiss();
				try
				{
				// updating UI from Background Thread
				runOnUiThread(new Runnable() {
					public void run() {
						/**
						 * Updating parsed Places into LISTVIEW
						 * */
						// Get json response status
						
						String status = nearPlaces.status;
					
						
						// Check for all possible status
						if(status.equals("OK")){
							// Successfully got places details
							if (nearPlaces.results != null) {
								// loop through each place
								for (Place p : nearPlaces.results) {
									HashMap<String, String> map = new HashMap<String, String>();
									
									// Place reference won't display in listview - it will be hidden
									// Place reference is used to get "place full details"
									map.put(KEY_REFERENCE, p.reference);
									
									// Place name
									map.put(KEY_NAME, p.name);
									
									
									// adding HashMap to ArrayList
									placesListItems.add(map);
								}
								// list adapter
								ListAdapter adapter = new SimpleAdapter(MyListActivity.this, placesListItems,
						                R.layout.list_item,
						                new String[] { KEY_REFERENCE, KEY_NAME}, new int[] {
						                        R.id.reference, R.id.name });
								Log.e("",""+KEY_NAME);
								
								// Adding data into listview
								list.setAdapter(adapter);
							}
						}
						else if(status.equals("ZERO_RESULTS")){
							// Zero results found
							alert.showAlertDialog(MyListActivity.this, "Near Places",
									"Sorry no places found. Try to change the types of places",
									false);
						}
						else if(status.equals("UNKNOWN_ERROR"))
						{
							alert.showAlertDialog(MyListActivity.this, "Places Error",
									"Sorry unknown error occured.",
									false);
						}
						else if(status.equals("OVER_QUERY_LIMIT"))
						{
							alert.showAlertDialog(MyListActivity.this, "Places Error",
									"Sorry query limit to google places is reached",
									false);
						}
						else if(status.equals("REQUEST_DENIED"))
						{
							alert.showAlertDialog(MyListActivity.this, "Places Error",
									"Sorry error occured. Request is denied",
									false);
						}
						else if(status.equals("INVALID_REQUESTW"))
						{
							alert.showAlertDialog(MyListActivity.this, "Places Error",
									"Sorry error occured. Invalid Request",
									false);
						}
						else
						{
							alert.showAlertDialog(MyListActivity.this, "Places Error",
									"Sorry error occured.",
									false);
						}
					}
					
				});
				}
				catch(Exception e)
				{
					Log.e("nearplaces",""+e);
				}
			}

		}
	
}
