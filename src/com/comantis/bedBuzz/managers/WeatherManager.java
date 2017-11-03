package com.comantis.bedBuzz.managers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.location.Location;
import android.os.Bundle;

import com.comantis.bedBuzz.R;
import com.comantis.bedBuzz.VO.WeatherLookupVO;
import com.comantis.bedBuzz.models.WeatherModel;
import com.comantis.bedBuzz.service.WUndergroundService;
import com.comantis.bedBuzz.utils.LocationGetter;


public class WeatherManager extends BroadcastReceiver {

	
	//public static Context mContext;
	
	public WeatherManager(Context context) 
	{
		//mContext = context.getApplicationContext();
		
	}
	
	public WeatherManager() 
	{
	}
	
	public void parseWeatherXML(Context context) throws XmlPullParserException, IOException
	{
		WeatherModel wm = WeatherModel.getWeatherModel();
		wm.codesMap = new HashMap<Integer,WeatherLookupVO>();
		
		XmlResourceParser xpp = context.getResources().getXml(R.xml.wwoconditioncodes);
		   
		xpp.next();
		   int eventType = xpp.getEventType();
		   while (eventType != XmlPullParser.END_DOCUMENT)
		   {
		    if(eventType == XmlPullParser.START_DOCUMENT)
		    {
		    }
		    else if(eventType == XmlPullParser.START_TAG)
		    {
		     
		     if (xpp.getName().equalsIgnoreCase("condition"))
		     {
		    	 WeatherLookupVO weatherOj = new WeatherLookupVO();
		    	 
		    	 eventType = xpp.next();
		    	 eventType = xpp.next();
		    	 
		    		 weatherOj.code = Integer.parseInt( xpp.getText());
		    	 
		    	 eventType = xpp.next();
		    	 eventType = xpp.next();
		    	 eventType = xpp.next();
		    	 
		    	weatherOj.description = xpp.getText();
		    	 
		    	 
		    	 eventType = xpp.next();
		    	 eventType = xpp.next();
		    	 eventType = xpp.next();
		    	 
		    	 weatherOj.dayIcon = xpp.getText();
		    	 
		    	 eventType = xpp.next();
		    	 eventType = xpp.next();
		    	 eventType = xpp.next();
		    	 
		    	 weatherOj.nightIcon = xpp.getText();
		    	 
		    	 eventType = xpp.next();
		    	 eventType = xpp.next();
		    	 eventType = xpp.next();
		    	 
		    	 weatherOj.sound = xpp.getText();
		    	 
		    	 
		    	 wm.codesMap.put(weatherOj.code, weatherOj);
		     }
		   }
		    
		    eventType = xpp.next();
		  }
	}
	
	public void getWeatherFromService(Context context, Context activityContext)
	{
		// first get the current location
		new LocationGetter().getLocation(context,activityContext);
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		// now get the weather
		
		Bundle b = intent.getExtras();
	    Location loc = (Location)b.get("location");
		new WUndergroundService(context,loc ).execute();
		
	}
	
}
