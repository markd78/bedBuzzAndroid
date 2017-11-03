package com.comantis.bedBuzz.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.comantis.bedBuzz.R;

public class LocationGetter {

	private static final String LOCATION_UPDATED = "com.comantis.bedBuzz.LocationUpdated";
	private LocationManager locationManager;
	private LocationListener locationListener;
	private Context mContext;
	private Context activityContext;

	public void getLocation(Context appContext, Context activityCtx)
	{
		mContext = appContext;
		activityContext = activityCtx;
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location provider.
				makeUseOfNewLocation(location);
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {}

			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {
				if (activityContext!=null)
				{
					AlertDialog alertDialog = new AlertDialog.Builder(activityContext).create();
					alertDialog.setTitle("Please enable location");
					alertDialog.setMessage("The 'Use wireless networks' location setting is currently disabled. Please enable this in order to receive your weather forecast (Settings->Location->Use Wireless Networks) ");
					alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// TODO Add your code for the button here.
						} });
					alertDialog.show();
					
					makeUseOfNewLocation(null);
				}		    	
			}
		};

		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	}

	public void makeUseOfNewLocation(Location location)
	{
		// Remove the listener you previously added
		locationManager.removeUpdates(locationListener);

		// complete
		Intent i = new Intent(LOCATION_UPDATED);
		i.putExtra("location", location);
		mContext.sendBroadcast(i);
	}
}
