package com.example.weatherdetector;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weatherdetector.util.SystemUiHider;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity implements
		ConnectionCallbacks, OnConnectionFailedListener {

	private static final String TAG = "DEBUG";
	/**
	 * Represents a geographical location.
	 */
	protected Location mLastLocation;
	/**
	 * Provides the entry point to Google Play services.
	 */
	protected GoogleApiClient mGoogleApiClient;

	private double mLat;
	private double mLog;

	private TextView mData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_fullscreen);
		Log.d(TAG, "ON CREATE");

		buildGoogleApiClient();

		findViewById(R.id.get_weather).setOnClickListener(
				mGetLocationButtonClickListener);

		mData = (TextView) findViewById(R.id.weather_data);
	}

	View.OnClickListener mGetLocationButtonClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			mLastLocation = LocationServices.FusedLocationApi
					.getLastLocation(mGoogleApiClient);
			if (mLastLocation != null) {
				mLat = mLastLocation.getLatitude();
				mLog = mLastLocation.getLongitude();
				checkForWeather(mLat, mLog);
				Toast.makeText(FullscreenActivity.this, "Location Found",
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(FullscreenActivity.this, "No Location Found. Kindly turn on GPS and click again later.",
						Toast.LENGTH_LONG).show();
			}

		}
	};

	/**
	 * Builds a GoogleApiClient. Uses the addApi() method to request the
	 * LocationServices API.
	 */
	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mGoogleApiClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}

	/**
	 * Runs when a GoogleApiClient object successfully connects.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		// Provides a simple way of getting a device's location and is well
		// suited for
		// applications that do not require a fine-grained location and that do
		// not need location
		// updates. Gets the best and most recent location currently available,
		// which may be null
		// in rare cases when a location is not available.
		mLastLocation = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);
		if (mLastLocation != null) {
			mLat = mLastLocation.getLatitude();
			mLog = mLastLocation.getLongitude();
		} else {
			Toast.makeText(this, R.string.no_location_detected,
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Refer to the javadoc for ConnectionResult to see what error codes
		// might be returned in
		// onConnectionFailed.
		Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
				+ result.getErrorCode());
	}

	@Override
	public void onConnectionSuspended(int cause) {
		// The connection to Google Play services was lost for some reason. We
		// call connect() to
		// attempt to re-establish the connection.
		Log.i(TAG, "Connection suspended");
		mGoogleApiClient.connect();
	}

	private void checkForWeather(double lat, double log) {

		DownloadWeatherData task = new DownloadWeatherData();
		task.execute(new String[] { "http://api.openweathermap.org/data/2.5/weather?lat="
				+ lat + "&lon=" + log + "&units=imperial" });

	}

	private class DownloadWeatherData extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			String response = "";
			for (String url : urls) {

				HttpClient client = new DefaultHttpClient();
				HttpGet httpget = new HttpGet(url);
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				try {
					response = client.execute(httpget, responseHandler);
					Log.d(TAG, response);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			parseJSONData(result);
		}
	}

	public void parseJSONData(String data) {
	//	Toast.makeText(FullscreenActivity.this, data, Toast.LENGTH_LONG).show();
		try {
			JSONObject jo = new JSONObject(data);
			JSONArray weatherAry = jo.getJSONArray("weather");
			JSONObject weather = weatherAry.getJSONObject(0);
			String weatherMain = weather.getString("main");
			String weatherDes = weather.getString("description");

			JSONObject main = jo.getJSONObject("main");
			String temp = main.getString("temp");
			String hum = main.getString("humidity");

			JSONObject coord = jo.getJSONObject("coord");
			String lon = coord.getString("lon");
			String lat = coord.getString("lat");

			String name = jo.getString("name");

			JSONObject wind = jo.getJSONObject("wind");
			String speed = wind.getString("speed");
			String degree = wind.getString("deg");

			mData.setText("Location: " + name + "\n" + "Co-Ordinates: " + lon
					+ " , " + lat + "\n" + "Weather: " + weatherMain + "\n"
					+ weatherDes + "\n" + "Temperatur: " + temp + " F" + "\n"
					+ "Humidity: " + hum + "\n" + "Wind > Speed: " + speed
					+ " " + "Degree: " + degree);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Toast.makeText(FullscreenActivity.this,
					"EXCEPTION IN DATA PARSING", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}

	}

}
