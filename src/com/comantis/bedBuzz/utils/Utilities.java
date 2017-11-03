package com.comantis.bedBuzz.utils;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpVersion;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import com.comantis.bedBuzz.enums.VoiceType;

public class Utilities {

	public static String getConnection() {
		
		return "https://bedbuzzserver.appspot.com/api/";
		//return "http://10.0.2.2:8888/api/";
	}

	public static String getTimeStringFromHoursAndMins(Integer hours, Integer mins, Boolean is24Hr)
	{
		String time = "";
		String amPM = "";
		
		if (!is24Hr)
		{
			if (hours < 12)
				amPM = "am";
			else
				amPM = "pm";
		}
		
		if (!is24Hr && hours > 12)
		{
			hours = hours - 12;
		}
		
		if (!is24Hr && hours == 0)
		{
			hours = 12;
		}
		
		time = hours.toString() + ":";
		
		if (mins < 10)
		{
			time+= "0";
		}
		
		
		
		time += mins.toString();
		
		if (!is24Hr)
		{
				time += " "+amPM;
		}
		
		return time;
	}

	public static String getVoiceNameTxtforVoiceType(VoiceType voiceType) {
		String voiceName;
		
		switch (voiceType)
		{
			case UKFEMALE:
				voiceName = "ukenglishfemale1";
				break;
			case USMALE:
				voiceName = "usenglishmale1";
				break;
			case USFEMALE:
				voiceName = "usenglishfemale1";
				break;
			default:
				voiceName = "error";
				break;
		}
		
		return voiceName;
		
	}

	public static CharSequence getFormattedMins(Integer hours, Integer mins, Boolean is24HrMode) {
		String minsStr ="";
		
		if (hours==null || mins == null)
		{
			return "";
		}
		
		if (mins < 10)
		{
			minsStr = "0"+mins;
		}
		else
		{
			minsStr = mins.toString();
		}
		
		
		return minsStr;
	}

	public static String getFormatterHour(Integer hours, Boolean is24HrMode) {
		
		String hourStr = "";
		
		if (hours==null)
		{
			return "";
		}
		
		if (!is24HrMode && hours > 12)
		{
			hours = hours - 12;
		}
		if (!is24HrMode && hours == 0)
		{
			hours = 12;
		}
		
		hourStr = hours.toString() ;
		
		
		
		return hourStr;
	}

	public static CharSequence getFormattedDate() {
		Format formatter;

		// Get today's date
		Date date = new Date();

		formatter = new SimpleDateFormat("E dd MMM");
		String s = formatter.format(date);
		return s;
	}
	
	/*public static DefaultHttpClient getDefaultHTTPClient()
	{
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
		 
		HttpParams params = new BasicHttpParams();
		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		 
		ClientConnectionManager cm = new SingleClientConnManager(params, schemeRegistry);
		DefaultHttpClient httpClient = new DefaultHttpClient(cm, params);
		
		return httpClient;
	}*/
	
	public static DefaultHttpClient getDefaultHTTPClient()
	{
		
		
		return new DefaultHttpClient();
	}
}
