package com.comantis.bedBuzz.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.comantis.bedBuzz.VO.WeatherVO;
import com.comantis.bedBuzz.models.WeatherModel;
import com.comantis.bedBuzz.utils.Utilities;

public class WUndergroundService extends AsyncTask<Integer, Integer, Void> {
   
    public static final String WEATHER_UPDATED = "com.comantis.bedBuzz.weatherUpdated";
    
	SharedPreferences settings;
    SharedPreferences.Editor editor;
    String mainURL = "http://api.wunderground.com/api/37832a4590eac49d/conditions/astronomy/forecast/q/";
    
    
    //String backupURL = "http://backup.worldweatheronline.com/feed/premium-weather-v2.ashx?key=4bdb250413215747111002&feedkey=a49a8a283d215849111002&format=json&q=";
    String url = "";
    Context context;
    Location location;

	private WeatherVO mWeather;
    
    public  WUndergroundService(Context context, Location location){
       
    	if (location!=null)
    	{
    		this.url = mainURL + location.getLatitude() +"," + location.getLongitude()+".json";
    	}
    	
        this.context = context;
        this.location = location;
    }

    private Boolean didFetchInLastHour()
    {
    	Boolean didFetchInLastHour = false;
    	WeatherModel wm = WeatherModel.getWeatherModel();
    	
    	if (wm.weatherWasLastFetchedAt == null)
    	{
    		return false;
    	}
    	
    	Calendar oneHourAgo = Calendar.getInstance();
    	
    	oneHourAgo.add(Calendar.HOUR, -1);
    	
    //	oneHourAgo.add(Calendar.MINUTE, -1);
    	
    	long diffInMilliSecs = wm.weatherWasLastFetchedAt.getTimeInMillis() - oneHourAgo.getTimeInMillis();
    	
    	if (diffInMilliSecs > 0 )
    	{
    		didFetchInLastHour = true;
    	}
    	
    	return didFetchInLastHour;
    	
    }
    
    @Override
    protected Void doInBackground(Integer... integers) {
        
    	WeatherModel wm = WeatherModel.getWeatherModel();
    	
    	// if the weather has already been fetched in the last hour, then do not fetch again - just send out the intent
    	if (didFetchInLastHour() || location ==null)
    	{
    		Intent i = new Intent(WEATHER_UPDATED);
        	i.putExtra("weather", wm.currentWeather);
            context.sendBroadcast(i);
            
            return null;
    	}
    	else
    	{
    		wm.weatherWasLastFetchedAt = Calendar.getInstance();
    	
	    	 InputStream source = retrieveStream(url);
	         
	         String jsonStr = null;
			try {
				jsonStr = IOUtils.toString(source, "UTF-8");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
	         
	         try {
				parseJSON(jsonStr);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
					
			}
    	}

        return null;
    }
    
    
    
    private void parseJSON(String jsonStr) throws JSONException
    {
    	try
    	{
    	JSONObject jObject = new JSONObject(jsonStr); 
    	
    	JSONObject currentConditionObject = jObject.getJSONObject("current_observation");
    	
    	 mWeather = new WeatherVO();
    	
    	mWeather.currentTempF = currentConditionObject.getInt("temp_f");
    	
    	mWeather.currentTempC = currentConditionObject.getInt("temp_c");
    	
    	String weatherType = currentConditionObject.getString("weather");
    	
    	mWeather.currentCode = this.getWeatherCodeForCondition(weatherType);
    	
    	JSONObject astronomyObject = jObject.getJSONObject("moon_phase");
    	
    	mWeather.sunsetTimeStr = astronomyObject.getJSONObject("sunset").getString("hour")+":"+
    	astronomyObject.getJSONObject("sunset").getString("minute");
    	mWeather.sunriseTimeStr = astronomyObject.getJSONObject("sunrise").getString("hour")+":"+
    	astronomyObject.getJSONObject("sunrise").getString("minute");
    	
    	JSONObject forecast = jObject.getJSONObject("forecast");
    	JSONObject simpleforecast = forecast.getJSONObject("simpleforecast");
    	
    	JSONArray forecasts = simpleforecast.getJSONArray("forecastday");
    	
    	mWeather.todayTempC = forecasts.getJSONObject(0).getJSONObject("high").getInt("celsius");
    	mWeather.todayTempF = forecasts.getJSONObject(0).getJSONObject("high").getInt("fahrenheit");
    	
    	String todayCondition = forecasts.getJSONObject(0).getString("conditions");
    	mWeather.todayCode = this.getWeatherCodeForCondition(todayCondition);
    	
    	mWeather.tomorrowTempC = forecasts.getJSONObject(1).getJSONObject("high").getInt("celsius");
    	
    	mWeather.tomorrowTempF = forecasts.getJSONObject(1).getJSONObject("high").getInt("fahrenheit");
    	
    	String tomorrowCondition = forecasts.getJSONObject(1).getString("conditions");
    	mWeather.tomorrowCode = this.getWeatherCodeForCondition(tomorrowCondition);
    	
    	WeatherModel wm = WeatherModel.getWeatherModel();
    	
    	mWeather.currentSound= wm.codesMap.get(mWeather.currentCode).sound;
    	mWeather.todaySound = wm.codesMap.get(mWeather.todayCode).sound;
    	mWeather.tomorrowSound = wm.codesMap.get(mWeather.tomorrowCode).sound;
    	
    	wm.currentWeather = mWeather;
    	}
    	catch (Exception ex)
    	{
    		Intent i = new Intent(WEATHER_UPDATED);
        	i.putExtra("weather", mWeather);
            context.sendBroadcast(i);
    	}
    }
    
    private int getWeatherCodeForCondition(String weatherType)
    	{
    	    // converts the type into a 'code'
    	    if (weatherType.indexOf("Drizzle") != -1)
    	    {
    	        return 266;
    	    }
    	    else if (weatherType.indexOf("Light Rain") != -1)
    	    {
    	        return 296;
    	    }
    	    else if (weatherType.indexOf("Heavy Rain") != -1)
    	    {
    	        return 308;
    	    }
    	    else if (weatherType.indexOf("Rain") != -1)
    	    {
    	        return 302;
    	    }
    	    else if (weatherType.indexOf("Light Snow") != -1)
    	    {
    	        return 326;
    	    }
    	    else if (weatherType.indexOf("Heavy Snow") != -1)
    	    {
    	        return 338;
    	    }
    	    else if (weatherType.indexOf("Snow") != -1)
    	    {
    	        return 332;
    	    }
    	    else if (weatherType.indexOf("Snow Grains") != -1)
    	    {
    	        return 332;
    	    }
    	    else if (weatherType.indexOf("Ice Crystals") != -1)
    	    {
    	        return 311;
    	    }
    	    else if (weatherType.indexOf("Ice Pellets") != -1)
    	    {
    	        return 311;
    	    }
    	    else if (weatherType.indexOf("Hail") != -1)
    	    {
    	        return 1000; //todo sound
    	    }
    	    else if (weatherType.indexOf("Mist") != -1)
    	    {
    	        return 143;
    	    }
    	    else if (weatherType.indexOf("Fog") != -1)
    	    {
    	        return 248;
    	    }
    	    else if (weatherType.indexOf("Smoke") != -1)
    	    {
    	        return 1001; //todo sound
    	    }
    	    else if (weatherType.indexOf("Volcanic Ash") != -1)
    	    {
    	        return 1002; //todo sound
    	    }
    	    else if (weatherType.indexOf("Widespread Dust") != -1)
    	    {
    	        return 1003; //todo sound
    	    }
    	    else if (weatherType.indexOf("Sand") != -1)
    	    {
    	        return 1004; //todo
    	    }
    	    else if (weatherType.indexOf("Haze") != -1)
    	    {
    	        return 1005; //todo
    	    }
    	    else if (weatherType.indexOf("Spray") != -1)
    	    {
    	        return 1006; //todo
    	    }
    	    else if (weatherType.indexOf("Dust Whirls") != -1)
    	    {
    	        return 1007; //todo
    	    }
    	    else if (weatherType.indexOf("Sandstorm") != -1)
    	    {
    	        return 1008; //todo
    	    }
    	    else if (weatherType.indexOf("Low Drifting Snow") != -1)
    	    {
    	        return 1009; //todo
    	    }
    	    else if (weatherType.indexOf("Low Drifting Widespread Dust") != -1)
    	    {
    	        return 1010; //todo
    	    }
    	    else if (weatherType.indexOf("Low Drifting Sand") != -1)
    	    {
    	        return 1011; //todo
    	    }
    	    else if (weatherType.indexOf("Blowing Snow") != -1)
    	    {
    	        return 227;
    	    }
    	    else if (weatherType.indexOf("Blowing Widespread Dust") != -1)
    	    {
    	        return 1012; //todo
    	    }
    	    else if (weatherType.indexOf("Blowing Sand") != -1)
    	    {
    	        return 1013; //todo
    	    }
    	    else if (weatherType.indexOf("Rain Mist") != -1)
    	    {
    	        return 143;
    	    }
    	    else if (weatherType.indexOf("Rain Showers") != -1)
    	    {
    	        return 293;
    	    }
    	    else if (weatherType.indexOf("Snow Showers") != -1)
    	    {
    	        return 368;
    	    }
    	    else if (weatherType.indexOf("Ice Pellet Showers") != -1)
    	    {
    	        return 1014; //todo
    	    }
    	    else if (weatherType.indexOf("Hail Showers") != -1)
    	    {
    	        return 1015; //todo
    	    }
    	    else if (weatherType.indexOf("Small Hail Showers") != -1)
    	    {
    	        return 1016; //todo
    	    }
    	    else if (weatherType.indexOf("Thunderstorms and Rain") != -1)
    	    {
    	        return 1017; //todo
    	        
    	    }
    	    else if (weatherType.indexOf("Thunderstorms and Snow") != -1)
    	    {
    	        return 395;
    	    }
    	    else if (weatherType.indexOf("Thunderstorms and Ice Pellets") != -1)
    	    {
    	        return 1018; //todo
    	        
    	    }
    	    else if (weatherType.indexOf("Thunderstorms with Hail") != -1)
    	    {
    	        return 1019; //todo
    	        
    	    }
    	    else if (weatherType.indexOf("Thunderstorms with Small Hail") != -1)
    	    {
    	        return 1020; //todo
    	        
    	    }
    	    else if (weatherType.indexOf("Thunderstorm") != -1)
    	    {
    	        return 1021; //todo
    	        
    	    }
    	    else if (weatherType.indexOf("Freezing Drizzle") != -1)
    	    {
    	        return 281;
    	    }
    	    else if (weatherType.indexOf("Freezing Rain") != -1)
    	    {
    	        return 314; 
    	        
    	    }
    	    else if (weatherType.indexOf("Freezing Fog") != -1)
    	    {
    	        return 260;
    	    }
    	    else if (weatherType.indexOf("Patches of Fog") != -1)
    	    {
    	        return 248; 
    	        
    	    }
    	    else if (weatherType.indexOf("Shallow Fog") != -1)
    	    {
    	        return 248; 
    	        
    	    }
    	    else if (weatherType.indexOf("Overcast") != -1)
    	    {
    	        return 122;
    	    }
    	    else if (weatherType.indexOf("Clear") != -1)
    	    {
    	        return 113;
    	    }
    	    else if (weatherType.indexOf("Partly Cloudy") != -1)
    	    {
    	        return 116;
    	    }
    	    else if (weatherType.indexOf("Mostly Cloudy") != -1)
    	    {
    	        return 119;
    	    }
    	    else if (weatherType.indexOf("Scattered Clouds") != -1)
    	    {
    	        return 1022; //todo
    	        
    	    }
    	    else // unknown
    	    {
    	        return -1;
    	    }

    	

    }
    
private InputStream retrieveStream(String url) {
        
        DefaultHttpClient client = Utilities.getDefaultHTTPClient(); 
        
        HttpGet getRequest = new HttpGet(url);
          
        try {
           
           HttpResponse getResponse = client.execute(getRequest);
           final int statusCode = getResponse.getStatusLine().getStatusCode();
           
           if (statusCode != HttpStatus.SC_OK) { 
              Log.w(getClass().getSimpleName(), 
                  "Error " + statusCode + " for URL " + url); 
              return null;
           }

           HttpEntity getResponseEntity = getResponse.getEntity();
           return getResponseEntity.getContent();
           
        } 
        catch (IOException e) {
           getRequest.abort();
           Log.w(getClass().getSimpleName(), "Error for URL " + url, e);
        }
        
        return null;
        
     }


    
    protected void onPostExecute(Void result)    {
        // complete
    	Intent i = new Intent(WEATHER_UPDATED);
    	i.putExtra("weather", mWeather);
        context.sendBroadcast(i);
    }

}
