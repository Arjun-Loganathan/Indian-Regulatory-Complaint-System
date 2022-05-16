package com.ircs.complaints;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ircs.complaint.R;

@SuppressLint("InflateParams")
public class CameraPhotoCapture extends Activity implements OnClickListener {
	public SQLiteDatabase db;

	int serverResponseCode = 0;
	ProgressDialog dialog = null;

	String upLoadServerUri = null;

	static String full_path_name;
	InputStream is=null;
	String result=null;
	String line=null;
	int code;
	private static final int SELECT_PICTURE = 1;
	private String selectedImagePath;
	EditText edit_complaint_type,edit_complaint_details,edit_latitude,edit_longitude,edit_area,edit_people_name,edit_people_email,edit_people_address;
	TextView edit_phno,txtv;
	Button btn_submit,btn_reset,subbtn,uploadButton,btn_photo;;
	//	String Variable Declaration
	String $complaint_type,$complaint_details,$image_details,$latitude,$longitude,$datetime,$area,$people_name,$people_email,$people_address,$phoneno,imageId;
	String myDate="";

	String keys;
	String $number;




	GPSTracker gpsTracker=null;   

	private static final char[] CHARSET_AZ_09 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

	final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
	// flag for Internet connection status
	Boolean isInternetPresent = false;

	// Connection detector class
	ConnectionDetector cd;

	// Alert Dialog Manager	
	AlertDialogManager alert = new AlertDialogManager();

	// GPS Location
	GPSTracker gps;
	String title;
	Uri imageUri                      = null;
	static TextView imageDetails      = null;
	public  static ImageView showImg  = null;
	CameraPhotoCapture CameraActivity = null;

	public String table_name="";
	Double latitude;
	Double longtude;
	TextView message;
	//	String EMAIL_REGEX;






	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_photo_layout);

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			System.out.println("*** My thread is now configured to allow connection");
		}



		edit_complaint_type=(EditText)findViewById(R.id.edittype);
		edit_complaint_details=(EditText)findViewById(R.id.editdet);
		edit_latitude=(EditText)findViewById(R.id.editlat);
		edit_longitude=(EditText)findViewById(R.id.editlong);
		edit_area=(EditText)findViewById(R.id.editarea);
		edit_people_name=(EditText)findViewById(R.id.edituname);
		edit_people_email=(EditText)findViewById(R.id.edituemail);
		edit_people_address=(EditText)findViewById(R.id.editadd);
		btn_photo=(Button)findViewById(R.id.btn_photo);
		//		edit_phno=(EditText)findViewById(R.id.editmob);
		message=(TextView)findViewById(R.id.message);
		subbtn=(Button)findViewById(R.id.subbtn);
		subbtn.setOnClickListener(this);
		edit_latitude.setText("0.0");
		edit_longitude.setText("0.0");
		edit_phno=(TextView)findViewById(R.id.edittxt);
		txtv=(TextView)findViewById(R.id.mobtxt);
		CameraActivity = this;

		imageDetails = (TextView) findViewById(R.id.imageDetails);

		showImg = (ImageView) findViewById(R.id.showImg);



btn_photo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showAlert();
			}
		});
		try
		{
			Bundle extras = getIntent().getExtras();
			title = extras.getString("title");
			table_name=title;
		}
		catch(Exception e)
		{
			Log.e("ddddddddddfffff","ddddd"+e);
		}
		message.setText(title+" Complaint Form");
		myDate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		isInternetPresent = cd.isConnectingToInternet();
		if (!isInternetPresent) {
			// Internet Connection is not present
			alert.showAlertDialog(CameraPhotoCapture.this, "Internet Connection Error",
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
			alert.showAlertDialog(CameraPhotoCapture.this, "GPS Status",
					"Couldn't get location information. Please enable GPS",
					false);
			// stop executing code by return
			return;
		}

		db = openOrCreateDatabase("smartcomplaint.db", MODE_PRIVATE, null);
		String see ="create table if not exists "+table_name+"(id integer primary key autoincrement,comtype varchar(120),comdetails varchar(3000),image varchar(300),latitude varchar(20),longtitude varchar(20),area varchar(200),datetime varchar(20),pname varchar(50),pemail varchar(100),address varchar(200),mobile int(15))";
		db.execSQL(see);


		

		locationget();
		/************* Php script path ****************/
		upLoadServerUri = "http://172.17.2.139/manimca/smartcomplaints/files_upload.php";


		txtv.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				showInputDialog();

			}
		});

	}

	public void locationget()
	{
		//	if(gpsTracker.canGetLocation())
		//	{
		$latitude=Double.toString(gps.getLatitude());
		$longitude=Double.toString(gps.getLongitude());

		latitude=Double.valueOf($latitude);
		longtude=Double.valueOf($longitude);
		Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + $latitude + "\nLong: " + $longitude, Toast.LENGTH_LONG).show();   

		String se_address = getCurrentLocationViaJSON(latitude,longtude);

		Toast.makeText(getApplicationContext(), "Your address is - \nLat: "+ se_address, Toast.LENGTH_LONG).show();   

		if(se_address.equals("") || se_address==null)
		{

			Toast.makeText(getApplicationContext(), "Your Network Connection is poor State", Toast.LENGTH_LONG).show();   


		}
		else
		{
			edit_area.setText(se_address);
			edit_latitude.setText(""+$latitude);
			edit_longitude.setText(""+$longitude);

		}


	}
	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data)
	{
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {

			if (resultCode == RESULT_OK) {
				if (requestCode == SELECT_PICTURE) {
					try
					{
	                imageUri = data.getData();
	                selectedImagePath = getPath(imageUri);
	                Log.e("",""+selectedImagePath);
					}
					catch(Exception e)
					{
						
					}
//	                showImg.setImageURI(selectedImagePath+"/"+".jpg");
	            }
				/*********** Load Captured Image And Data Start ****************/

				imageId = convertImageUriToFile(imageUri, CameraActivity);
				
				// Create and excecute AsyncTask to load capture image

				new LoadImagesFromSDCard().execute("" + imageId);

				/*********** Load Captured Image And Data End ****************/

			} else if (resultCode == RESULT_CANCELED) {

				Toast.makeText(this, " Picture was not taken ", Toast.LENGTH_SHORT).show();
			} else {

				Toast.makeText(this, " Picture was not taken ", Toast.LENGTH_SHORT).show();
			}
		}
	}


	/************ Convert Image Uri path to physical path **************/

	public static String convertImageUriToFile ( Uri imageUri, Activity activity )  {

		Cursor cursor = null;
		int imageID = 0;

		try {

			/*********** Which columns values want to get *******/
			String [] proj={
					MediaStore.Images.Media.DATA,
					MediaStore.Images.Media._ID,
					MediaStore.Images.Thumbnails._ID,
					MediaStore.Images.ImageColumns.ORIENTATION
			};

			cursor = activity.managedQuery(

					imageUri,         //  Get data for specific image URI
					proj,             //  Which columns to return
					null,             //  WHERE clause; which rows to return (all rows)
					null,             //  WHERE clause selection arguments (none)
					null              //  Order-by clause (ascending by name)

					);

			//  Get Query Data

			int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
			int columnIndexThumb = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID);
			int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

			//int orientation_ColumnIndex = cursor.
			//    getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);

			int size = cursor.getCount();

			/*******  If size is 0, there are no images on the SD Card. *****/

			if (size == 0) {


				imageDetails.setText("No Image");
			}
			else
			{

				int thumbID = 0;
				if (cursor.moveToFirst()) {

					/**************** Captured image details ************/

					/*****  Used to show image on view in LoadImagesFromSDCard class ******/
					imageID     = cursor.getInt(columnIndex);

					thumbID     = cursor.getInt(columnIndexThumb);

					String Path = cursor.getString(file_ColumnIndex);

					//String orientation =  cursor.getString(orientation_ColumnIndex);

					//					String CapturedImageDetails = " CapturedImageDetails : \n\n"
					//							+" ImageID :"+imageID+"\n"
					//							+" ThumbID :"+thumbID+"\n"
					//							+" Path :"+Path+"\n";

					String CapturedImageDetails=Path.substring(Path.lastIndexOf("/")+1);

					full_path_name=Path;
					// Show Captured Image detail on activity
					imageDetails.setText(CapturedImageDetails);
					

				}
			}   
		}
		catch(Exception e)
		{
			
		}
		
		// Return Captured Image ImageID ( By this ImageID Image will load from sdcard )

		return ""+imageID;
	}


	/**
	 * Async task for loading the images from the SD card.
	 *
	 * @author Android Example
	 *
	 */

	// Class with extends AsyncTask class

	public class LoadImagesFromSDCard  extends AsyncTask<String, Void, Void> {

		private ProgressDialog Dialog = new ProgressDialog(CameraPhotoCapture.this);

		Bitmap mBitmap;

		protected void onPreExecute() {
			/****** NOTE: You can call UI Element here. *****/

			// Progress Dialog
			Dialog.setMessage(" Loading image from Sdcard..");
			Dialog.show();
		}


		// Call after onPreExecute method
		protected Void doInBackground(String... urls) {

			Bitmap bitmap = null;
			Bitmap newBitmap = null;
			Uri uri = null;      


			try {

				/**  Uri.withAppendedPath Method Description
				 * Parameters
				 *    baseUri  Uri to append path segment to
				 *    pathSegment  encoded path segment to append
				 * Returns
				 *    a new Uri based on baseUri with the given segment appended to the path
				 */

				uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + urls[0]);

				/**************  Decode an input stream into a bitmap. *********/
				bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));

				if (bitmap != null) {

					/********* Creates a new bitmap, scaled from an existing bitmap. ***********/

					newBitmap = Bitmap.createScaledBitmap(bitmap, 170, 170, true);

					bitmap.recycle();

					if (newBitmap != null) {

						mBitmap = newBitmap;
					}
				}
			} catch (IOException e) {
				// Error fetching image, try to recover

				/********* Cancel execution of this task. **********/
				cancel(true);
			}

			return null;
		}


		protected void onPostExecute(Void unused) {

			// NOTE: You can call UI Element here.

			// Close progress dialog
			Dialog.dismiss();

			if(mBitmap != null)
			{
				// Set Image to ImageView 

				showImg.setImageBitmap(mBitmap);
			} 

		}

	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static JSONObject getLocationInfo(double lat, double lng) {

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
			.permitAll().build();

			StrictMode.setThreadPolicy(policy);

			HttpGet httpGet = new HttpGet(
					"http://maps.googleapis.com/maps/api/geocode/json?latlng="
							+ lat + "," + lng + "&sensor=true");
			HttpClient client = new DefaultHttpClient();
			HttpResponse response;
			StringBuilder stringBuilder = new StringBuilder();

			try {
				response = client.execute(httpGet);
				HttpEntity entity = response.getEntity();
				InputStream stream = entity.getContent();
				int b;
				while ((b = stream.read()) != -1) {
					stringBuilder.append((char) b);
				}
			} catch (ClientProtocolException e) {

			} catch (IOException e) {

			}

			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject = new JSONObject(stringBuilder.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return jsonObject;
		}
		return null;
	}

	public static String getCurrentLocationViaJSON(double lat, double lng) {

		JSONObject jsonObj = getLocationInfo(lat, lng);
		Log.i("JSON string =>", jsonObj.toString());

		String Address1 = "";
		String Address2 = "";
		String City = "";
		String State = "";
		String Country = "";
		String County = "";
		String PIN = "";

		String currentLocation = "";

		try {
			String status = jsonObj.getString("status").toString();
			Log.i("status", status);

			if (status.equalsIgnoreCase("OK")) {
				JSONArray Results = jsonObj.getJSONArray("results");
				JSONObject zero = Results.getJSONObject(0);
				JSONArray address_components = zero
						.getJSONArray("address_components");

				for (int i = 0; i < address_components.length(); i++) {
					JSONObject zero2 = address_components.getJSONObject(i);
					String long_name = zero2.getString("long_name");
					JSONArray mtypes = zero2.getJSONArray("types");
					String Type = mtypes.getString(0);

					if (Type.equalsIgnoreCase("street_number")) {
						Address1 = long_name + " ";
					} else if (Type.equalsIgnoreCase("route")) {
						Address1 = Address1 + long_name;
					} else if (Type.equalsIgnoreCase("sublocality")) {
						Address2 = long_name;
					} else if (Type.equalsIgnoreCase("locality")) {
						// Address2 = Address2 + long_name + ", ";
						City = long_name;
					} else if (Type
							.equalsIgnoreCase("administrative_area_level_2")) {
						County = long_name;
					} else if (Type
							.equalsIgnoreCase("administrative_area_level_1")) {
						State = long_name;
					} else if (Type.equalsIgnoreCase("country")) {
						Country = long_name;
					} else if (Type.equalsIgnoreCase("postal_code")) {
						PIN = long_name;
					}

				}

				currentLocation = Address1 + "," + Address2 + "," + City + ","
						+ State + "," + Country + "," + PIN;

			}
		} catch (Exception e) {

		}
		return currentLocation;
	}






	@Override
	public void onClick(View arg0) {


		$complaint_type = edit_complaint_type.getText().toString();
		$complaint_details = edit_complaint_details.getText().toString();
		$image_details = imageDetails.getText().toString();
		$latitude = edit_latitude.getText().toString();
		$longitude = edit_longitude.getText().toString();
		$area = edit_area.getText().toString();
		$datetime = myDate;	
		$people_name = edit_people_name.getText().toString();	
		$people_email = edit_people_email.getText().toString();
		$people_address = edit_people_address.getText().toString();
		$phoneno =  edit_phno.getText().toString();


		if ($complaint_type.equals("") ||$complaint_details.equals("") ||$image_details.equals("") ||$latitude.equals("") || $longitude.equals("") ||$area.equals("") ||$people_name.equals("") ||$people_email.equals("") || $people_address.equals("") || $phoneno.equals(""))
		{				
			if ($complaint_type.equals(""))
			{
				Toast.makeText(this, "ENTER USER COMPLAINT TYPE",	Toast.LENGTH_LONG).show();
			}
			if ($complaint_details.equals(""))
			{
				Toast.makeText(this, "ENTER USER COMPLAINT DETAILS",Toast.LENGTH_LONG).show();
			}
			if ($image_details.equals(""))
			{
				Toast.makeText(this, "Capture Image",Toast.LENGTH_LONG).show();
			}
			if ($latitude.equals("")) 
			{
				Toast.makeText(this, "Your Latitude Field is Empty",Toast.LENGTH_LONG).show();
			}
			if ($longitude.equals(""))
			{
				Toast.makeText(this, "Your Longitude Field is Empty",Toast.LENGTH_LONG).show();

			}
			if ($area.equals(""))
			{
				Toast.makeText(this, "Your Area Field is Empty",Toast.LENGTH_LONG).show();

			}
			if ($people_name.equals(""))
			{
				Toast.makeText(this, "ENTER Your Name",Toast.LENGTH_LONG).show();

			}
			if ($people_email.equals(""))
			{
				Toast.makeText(this, "ENTER Your Email ID",Toast.LENGTH_LONG).show();

			}
			if ($people_address.equals(""))
			{
				Toast.makeText(this, "ENTER Your Address",Toast.LENGTH_LONG).show();

			}
			if ($phoneno.equals(""))
			{
				Toast.makeText(this, "ENTER Your Phone Number",Toast.LENGTH_LONG).show();

			}
		}
		else if(!isValidUser($people_name))
		{
			Toast.makeText(this, "ENTER VALID USERNAME",
					Toast.LENGTH_LONG).show();
		}

		else if(!isValidEmail($people_email))
		{
			Toast.makeText(this, "ENTER VALID EMAIL ID",
					Toast.LENGTH_LONG).show();
		}
		else if(!isValidPhone($phoneno))
		{
			Toast.makeText(this, "ENTER VALID PHONE NUMBER",
					Toast.LENGTH_LONG).show();
		}



		else
		{



			try
			{
				new Thread(new Runnable() {
					public void run() {
						runOnUiThread(new Runnable() {
							public void run() {
								Log.e("show some Test", "hlleo");
							}
						});                     

						try
						{
							setRegisterPage(full_path_name);
						}
						catch(Exception e)
						{
							Log.e("ddd","ffff");
						}
					}
				}).start();    


				db = openOrCreateDatabase("smartcomplaint.db", MODE_PRIVATE, null);
				String see ="create table if not exists "+table_name+"(id integer primary key autoincrement,comtype varchar(120),comdetails varchar(3000),image varchar(300),latitude varchar(20),longtitude varchar(20),area varchar(200),datetime varchar(20),pname varchar(50),pemail varchar(100),address varchar(200),mobile int(15))";
				db.execSQL(see);



				ContentValues initialValues = new ContentValues();
				initialValues.put("comtype", edit_complaint_type.getText().toString());
				initialValues.put("comdetails", edit_complaint_details.getText().toString());
				initialValues.put("image", imageDetails.getText().toString());
				initialValues.put("latitude", edit_latitude.getText().toString());
				initialValues.put("longtitude", edit_longitude.getText().toString());
				initialValues.put("area", edit_area.getText().toString());
				initialValues.put("datetime", myDate);
				initialValues.put("pname", edit_people_name.getText().toString());
				initialValues.put("pemail", edit_people_email.getText().toString());
				initialValues.put("address", edit_people_address.getText().toString());
				initialValues.put("mobile", edit_phno.getText().toString());

				db.insert(table_name, null, initialValues);
				Intent int_call = new Intent(CameraPhotoCapture.this,MainActivity.class);
				int_call.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(int_call);
			}
			catch(Exception e)
			{
				Log.e("dfd","dfd");
			}

		}



	}


	public int setRegisterPage(final String sourceFileUri)
	{

		String fileName = sourceFileUri;

		HttpURLConnection conn = null;
		DataOutputStream dos = null; 
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		File sourceFile = new File(sourceFileUri);

		if (!sourceFile.isFile()) {
			dialog = ProgressDialog.show(CameraPhotoCapture.this, "", "Uploading file...", true);
			dialog.dismiss();

			Log.e("uploadFile", "Source File not exist :" );

			runOnUiThread(new Runnable() {
				public void run() {
					//   messageText.setText("Source File not exist :"                             +uploadFilePath + "" + uploadFileName);
				}
			});

			return 0;
		}
		else
		{
			try {

				// open a URL connection to the Servlet
				FileInputStream fileInputStream = new FileInputStream(sourceFile);
				URL url = new URL(upLoadServerUri);




				conn = (HttpURLConnection) url.openConnection();
				conn.setDoInput(true); 
				conn.setDoOutput(true);
				conn.setUseCaches(false); 
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("ENCTYPE", "multipart/form-data");
				conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
				conn.setRequestProperty("uploaded_file", fileName);

				dos = new DataOutputStream(conn.getOutputStream());

				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; name=uploaded_file;filename="
						+ fileName + "" + lineEnd);

				dos.writeBytes(lineEnd);

				// create a buffer of  maximum size
				bytesAvailable = fileInputStream.available();

				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];

				// read file and write it into form...
				bytesRead = fileInputStream.read(buffer, 0, bufferSize); 

				while (bytesRead > 0) {

					dos.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);  

				}

				// send multipart form data necesssary after file data...
				dos.writeBytes(lineEnd);
				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

				// Responses from the server (code and message)
				serverResponseCode = conn.getResponseCode();
				String serverResponseMessage = conn.getResponseMessage();

				Log.i("uploadFile", "HTTP Response is : "
						+ serverResponseMessage + ": " + serverResponseCode);

				if(serverResponseCode == 200){

					runOnUiThread(new Runnable() {
						public void run() {


							File se = new File(full_path_name);

							String ser = se.getName();

							String string =full_path_name;
							String[] parts = string.split("/");
							String part1 = parts[0]; // 004
							String part2 = parts[1]; 
							String part3 = parts[2];
							String part4 = parts[3];
							String msg = "http://172.17.2.139/manimca/smartcomplaints/images/"+part4;

							Log.e("","full path"+full_path_name);
							//    messageText.setText(msg);
							Toast.makeText(CameraPhotoCapture.this, "File Upload Complete.",
									Toast.LENGTH_SHORT).show();



							try
							{
								HttpClient httpclient = new DefaultHttpClient();
								HttpPost httppost = new HttpPost("http://172.17.2.139/manimca/smartcomplaints/"+table_name+".php");
								if(httppost!=null)
								{
									Context context = getApplicationContext();
									CharSequence text = "Connected";
									int duration = Toast.LENGTH_LONG;
									Toast toast = Toast.makeText(context, text, duration);
									toast.show();
								}


								ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

								nameValuePairs.add(new BasicNameValuePair("complaint_type", $complaint_type));
								nameValuePairs.add(new BasicNameValuePair("complaint_details", $complaint_details));
								nameValuePairs.add(new BasicNameValuePair("image_details", $image_details));
								nameValuePairs.add(new BasicNameValuePair("latitude",$latitude));
								nameValuePairs.add(new BasicNameValuePair("longitude", $longitude));
								nameValuePairs.add(new BasicNameValuePair("area", $area));
								nameValuePairs.add(new  BasicNameValuePair("datetime", myDate.toString().trim()));
								nameValuePairs.add(new  BasicNameValuePair("people_name", $people_name));
								nameValuePairs.add(new  BasicNameValuePair("people_email", $people_email));
								nameValuePairs.add(new  BasicNameValuePair("people_address", $people_address));
								nameValuePairs.add(new  BasicNameValuePair("phoneno", $phoneno));



								httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
								ResponseHandler<String> responseHandler = new BasicResponseHandler();
								final String response = httpclient.execute(httppost,responseHandler);   

								if (response.equalsIgnoreCase("You are now registered")){
									Toast.makeText(CameraPhotoCapture.this, "SUCCESSFLLY REGISTERED", Toast.LENGTH_SHORT).show();
									finish();
									//								HttpResponse response = httpclient.execute(httppost); 
									//								HttpEntity entity = response.getEntity();
									//								is = entity.getContent();
									Log.e("pass 1", "connection success ");

								}
							}

							catch(Exception e)
							{
								Log.e("Fail 1", e.toString());
								Toast.makeText(getApplicationContext(), "Invalid IP Address",
										Toast.LENGTH_LONG).show();
							}     

							try
							{
								BufferedReader reader = new BufferedReader
										(new InputStreamReader(is,"iso-8859-1"),8);
								StringBuilder sb = new StringBuilder();
								while ((line = reader.readLine()) != null)
								{
									sb.append(line + "\n");
								}
								is.close();
								result = sb.toString();
								Log.e("pass 2", "connection success ");
							}
							catch(Exception e)
							{
								Log.e("Fail 2", e.toString());
							}     

							try
							{
								JSONObject json_data = new JSONObject(result);
								code=(json_data.getInt("code"));

								if(code==1)
								{
									Toast.makeText(getBaseContext(), "Inserted Successfully",
											Toast.LENGTH_SHORT).show();
								}
								else
								{
									Toast.makeText(getBaseContext(), "Sorry, Try Again",
											Toast.LENGTH_LONG).show();
								}
							}
							catch(Exception e)
							{
								Log.e("Fail 3", e.toString());
							}
						}
					});               
				}   
				//close the streams //
				fileInputStream.close();
				dos.flush();
				dos.close();

			} catch (MalformedURLException ex) {

				dialog.dismiss(); 
				ex.printStackTrace();

				runOnUiThread(new Runnable() {
					public void run() {
						//      messageText.setText("MalformedURLException Exception : check script url.");
						Toast.makeText(CameraPhotoCapture.this, "MalformedURLException",
								Toast.LENGTH_SHORT).show();
					}
				});

				Log.e("Upload file to server", "error: " + ex.getMessage(), ex); 
			} catch (Exception e) {

				dialog.dismiss(); 
				e.printStackTrace();

				runOnUiThread(new Runnable() {
					public void run() {
						//    messageText.setText("Got Exception : see logcat ");
						Toast.makeText(CameraPhotoCapture.this, "No Internet Connection ",
								Toast.LENGTH_SHORT).show();
					}
				});
				Log.e("Upload file to server Exception", "Exception : "
						+ e.getMessage(), e); 
			}
			dialog.dismiss();      
			return serverResponseCode;

		} // End else block
	}


	public void showMessgeAlert(String Message)
	{
		Toast.makeText(getApplicationContext(), Message, Toast.LENGTH_SHORT).show();
	}



	protected void showInputDialog() {

		// get prompts.xml view
		LayoutInflater layoutInflater = LayoutInflater.from(CameraPhotoCapture.this);
		View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				CameraPhotoCapture.this);
		alertDialogBuilder.setView(promptView);

		final EditText editText = (EditText) promptView.findViewById(R.id.editText1);
		// setup a dialog window
		alertDialogBuilder
		.setCancelable(false)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				$number=editText.getText().toString();

				if($number.equals("") ||$number.equals(""))
				{
					Toast.makeText(getApplicationContext(), "Enter the Phone Numberss", Toast.LENGTH_SHORT).show();
					showInputDialog();
				}
				else
				{
					Toast.makeText(getApplicationContext(), ""+$number, Toast.LENGTH_SHORT).show();
					keys = GenrandomKey.randomString(CHARSET_AZ_09, 6);
					getGenkey($number, "Verification Key"+keys.toString().trim());	
					Log.e("",""+keys);
					showInputDialog1();
				}
			}
		})
		.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		// create an alert dialog
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();

	}
	protected void showInputDialog1() {

		// get prompts.xml view
		LayoutInflater layoutInflater = LayoutInflater.from(CameraPhotoCapture.this);
		View promptView = layoutInflater.inflate(R.layout.input_dialog1, null);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				CameraPhotoCapture.this);
		alertDialogBuilder.setView(promptView);

		final EditText editText = (EditText) promptView.findViewById(R.id.editText2);
		// setup a dialog window
		alertDialogBuilder
		.setCancelable(false)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				String verify=editText.getText().toString();
				Toast.makeText(getApplicationContext(), ""+verify, Toast.LENGTH_SHORT).show();
				if(verify.equals(keys))
				{
					edit_phno.setText($number);
				}
				else
				{
					showInputDialog1();
				}


			}
		})
		.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		// create an alert dialog
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();

	}


	protected void showViewDialog() {

		// get prompts.xml view
		LayoutInflater layoutInflater = LayoutInflater.from(CameraPhotoCapture.this);
		View promptView = layoutInflater.inflate(R.layout.view_dialog, null);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				CameraPhotoCapture.this);
		alertDialogBuilder.setView(promptView);

		final TextView resultview = (TextView) promptView.findViewById(R.id.resultView);
		//		resultview.setText(cityname);
		// setup a dialog window
		alertDialogBuilder
		.setCancelable(false)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

			}
		})
		.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		// create an alert dialog
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();

	}

	public void getGenkey(String phone,String msg)
	{
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(phone, null, msg, null, null);
	}
	public boolean isValidEmail(String email) {
		String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
				+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";


		Pattern pattern = Pattern.compile(EMAIL_PATTERN);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}
	public boolean isValidUser(String email) {

		String USERNAME_PATTERN="[a-zA-Z]{1,250}";
		Pattern pattern = Pattern.compile(USERNAME_PATTERN);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}
	public boolean isValidPhone(String email) {

		String PHONE_NUMBER_PATTERN="^[7-9][0-9]{9}$";
		Pattern pattern = Pattern.compile(PHONE_NUMBER_PATTERN);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}

	public void showAlert() {
//		showImg.setImageResource(android.R.color.transparent);
		showImg.setImageBitmap(null);
		CameraPhotoCapture.this.runOnUiThread(new Runnable() {
			public void run() {
				
				final String [] items = new String[] {"From Camera", "From Gallery"};
	            final Integer[] icons = new Integer[] {R.drawable.cam, R.drawable.file};
	            ListAdapter adapter = new ArrayAdapterWithIcon(CameraPhotoCapture.this, items, icons);
	            
	            new AlertDialog.Builder(CameraPhotoCapture.this).setTitle("Select Image")
	                .setAdapter(adapter, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int item ) {
	                    	showImg.setImageBitmap(null);
	                    	if(item==0)
	                    	{
	                    		/*************************** Camera Intent Start ************************/

                				// Define the file-name to save photo taken by Camera activity

                				String fileName = "Camera_Example.jpg";

                				// Create parameters for Intent with filename

                				ContentValues values = new ContentValues();

                				values.put(MediaStore.Images.Media.TITLE, fileName);

                				values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera");

                				// imageUri is the current activity attribute, define and save
                				// it for later usage

                				imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                				/****
                				 * EXTERNAL_CONTENT_URI : style URI for the "primary" external
                				 * storage volume.
                				 ****/

                				// Standard Intent action that can be sent to have the camera
                				// application capture an image and return it.

                				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                				intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

                				startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

                				/*************************** Camera Intent End ************************/
	                    	}
	                    	else
	                    	{
	                    		Toast.makeText(CameraPhotoCapture.this, "Item Selected: " + item, Toast.LENGTH_SHORT).show();
	                    		
	                    		  Intent intent = new Intent();
	                              intent.setType("image/*");
	                              intent.setAction(Intent.ACTION_GET_CONTENT);
	                              startActivityForResult(Intent.createChooser(intent,
	                                      "Select Picture"), SELECT_PICTURE);
	                    	}
	                    	       
	                    }
	            }).show();
				
				
			}
		});
	}
	

	    /**
	     * helper to retrieve the path of an image URI
	     */
	    public String getPath(Uri uri) {
	            // just some safety built in 
	            if( uri == null ) {
	                // TODO perform some logging or show user feedback
	                return null;
	            }
	            // try to retrieve the image from the media store first
	            // this will only work for images selected from gallery
	            String[] projection = { MediaStore.Images.Media.DATA };
	            Cursor cursor = managedQuery(uri, projection, null, null, null);
	            if( cursor != null ){
	                int column_index = cursor
	                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	                cursor.moveToFirst();
	                return cursor.getString(column_index);
	            }
	            // this is our fallback here
	            return uri.getPath();
	    }


}