package com.comantis.bedBuzz.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.SystemClock;

import com.comantis.bedBuzz.VO.AlarmVO;
import com.comantis.bedBuzz.VO.MessageVO;
import com.comantis.bedBuzz.VO.RSSClip;
import com.comantis.bedBuzz.activities.ComposeMessageActivity;


public class SoundManager  implements MediaPlayer.OnCompletionListener  {

	public static final String RSS_CLIP_PLAYING = "com.comantis.rssClipPlaying";
	public static final String RSS_CLIPS_END = "com.comantis.rssClipEnded";
	public static final String MESSAGE_PLAYING = "com.comantis.messagePlaying";
	public static final String MESSAGE_STOPPED_PLAYING = "com.comantis.messageStoppedPlaying";
	public static final String COUNTDOWN_NUMBER_PLAYED = "com.comantis.countdownNumberPlayed";
	MediaPlayer player = null;
	Context context;
	private boolean justPlayedRSS = false;
	
	private Queue<String> playQueue = new LinkedList<String>();
	
	private HashMap<String, MessageVO> messagesIndexedByFilename = new HashMap<String, MessageVO> ();
	private boolean justPlayedMessage = false;
	public AlarmVO _currentAlarm;
	public boolean isCountdownPlaying = false;
	
private static SoundManager soundManager = null; 
	
	public static SoundManager getSoundModel(Context context) { 
	  if (soundManager == null) { 
		  soundManager = new SoundManager(context); 
	  } 
	  
	  // initialize
	  
	  return soundManager; 
	}
	
	private SoundManager(Context context)
	{
		this.context  = context;
		
		player = new MediaPlayer();
        player.setOnCompletionListener(this);
	}
	
	public void playTest()
	{
		this.resetPlayQueue();
		
		this.addPersonalGreeting(true);
		this.addAddtionalMessage(true);
		
		playNextSoundInQueue();
	}
	
	public void addMessagesToPlayQueue()
	{
		 messagesIndexedByFilename = new HashMap<String, MessageVO> ();
		
		int i=0;
		
		MessagingModel messagingModel = MessagingModel.getMessagingModel();
		
		if ( messagingModel.messagesToPlay== null )
		{
			return;
		}
		
		for (MessageVO message  : messagingModel.messagesToPlay)
		{
			String filename = storeMp3FromByteArr(message.sound,"MESSAGE",i);
			addSoundToQueue(filename);
			i++;
			
			messagesIndexedByFilename.put(filename, message);
		}
	}
	
	public void playMessages()
	{
		this.resetPlayQueue();
		
		addMessagesToPlayQueue();
		
		playNextSoundInQueue();
	}
	
	private void addSoundToQueue(String fileName)
	{
		playQueue.add(fileName);
	}
	
	private void playNextSoundInQueue()
	{
		AssetFileDescriptor afd = null;
		player.reset();
		
		try {
			String fileName = playQueue.element();
			String extDir = Environment.getExternalStorageDirectory().toString();
			if (fileName.contains(extDir))
			{
				player.setDataSource(fileName);
				
			}
			else if(fileName.contains(context.getCacheDir().toString()))
			{
				
				File tempMp3 = new File(fileName);
				//tempMp3.deleteOnExit();
				//player.setDataSource(fileName);
				
				FileInputStream fis = new FileInputStream(tempMp3);
				player.setDataSource(fis.getFD());
				
				if (fileName.contains("MESSAGE"))
				{
					// mark the message as read
					MessageVO message = (MessageVO) this.messagesIndexedByFilename.get(fileName);
					MessagingModel.getMessagingModel().removeFromMessagesToPlay(message);
					
					Intent i = new Intent(MESSAGE_PLAYING);
			        i.putExtra("messageText",  message.messageText);
			        i.putExtra("fromFBUserID",  message.senderID);
			        context.sendBroadcast(i);
			        
			        justPlayedMessage = true;
				}
				else if (fileName.contains("rss"))
				{
					// broadcast intent saying we are playing an RSS clip
					RSSClip rssClip = (RSSClip)RSSModel.getRSSModell().rssClipsIndexedByURL.get(fileName);
					
					Intent i = new Intent(RSS_CLIP_PLAYING);
			        i.putExtra("rssLink",  rssClip.link);
			        i.putExtra("soundFileName",  fileName);
			        context.sendBroadcast(i);
			        
			        justPlayedRSS = true;
				}
			}
			else
			{
				afd = context.getAssets().openFd(fileName);
				player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			// try to play the next sound
			if (playQueue.size() > 0 )
			{
				playQueue.remove();
			}
			player.reset();
			
			if (playQueue.size() > 0 )
			{
				playNextSoundInQueue();
			}
		}
	    catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	    try {
			player.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    player.start();
	    
	    startTime = SystemClock.elapsedRealtime();
	}
	
	Long startTime;
	
	/*
	 * And this is the bug fix.
	 * On some systems the onCompletion is called too early.
	 * Seems on these system they call it when the last compression block of a file is
	 * started. So you'll find that depending on the system and file sleepTime will be around 300ms.
	 */
	@Override
	public void onCompletion(MediaPlayer mp)
	{
		int loopCount = 0;
		
		try
		{
			try
			{
				//The fix! :)
				long sleepTime = player.getDuration() - (int)(SystemClock.elapsedRealtime() - startTime);
				if( sleepTime > 0 && sleepTime < 10000 )
				{
					Thread.sleep(sleepTime);
				}
			}
			catch (InterruptedException e)
			{
			}
			
			loopCount--;
			if( loopCount < 1 )
			{
				//And now call at the correct time! ;)
				myOnCompletionListener(player);
				
			}
			else
			{
				mp.start();
			}
		}
		catch(Exception e)
		{
			// try to play the next sound
			if (playQueue.size() > 0 )
			{
				playQueue.remove();
			}
			player.reset();
			
			if (playQueue.size() > 0 )
			{
				playNextSoundInQueue();
			}
		}
		
	}

	public void myOnCompletionListener(MediaPlayer player) {
		
		// remove the sound from the queue
		if (playQueue.size() > 0 )
		{
			playQueue.remove();
		}
		player.reset();
		
		if (playQueue.size() > 0 )
		{
			playNextSoundInQueue();
		}
		
		if (justPlayedRSS && playQueue.size() == 0)
		{
			Intent i = new Intent(RSS_CLIPS_END);
	        context.sendBroadcast(i);
	        justPlayedRSS = false;
		}
		
		if (this.isCountdownPlaying)
		{
			Intent i = new Intent(COUNTDOWN_NUMBER_PLAYED);
	        context.sendBroadcast(i);
		}
		
		if (justPlayedMessage)
		{
			Intent i = new Intent(MESSAGE_STOPPED_PLAYING);
	        context.sendBroadcast(i);
	        justPlayedMessage = false;
		}
		
		if (playQueue.size() == 0 && _currentAlarm!=null &&!UserModel.getUserModel().isSnoozing)
		{
			this.playAlarmMusic();
		}
		
	}
	
	private void resetPlayQueue()
	{
		playQueue = new LinkedList<String>(); 
		player.reset();
	}
	
	private String getVoiceDir()
	{
		UserModel um = UserModel.getUserModel();
		
		if (um.userSettings == null || um.userSettings.voiceName == null) {
			return "english.ukfemale";
		}
		else if (um.userSettings.voiceName.equals("usenglishmale1")) {
			
			return "english/paul";
		}
		else if (um.userSettings.voiceName.equals("usenglishfemale1") )
		{
			return "english.usfemale";
		}
		else
		{
			return "english.ukfemale";
		}
	}
	
	private boolean isEvening() {
		Date now = new Date();
		
		if (now.getHours() <18)
		{
			return false;
		}
		
		return true;
	}
	
	private void addWeatherFilesToQueue()
	{
		WeatherModel weatherModel = WeatherModel.getWeatherModel();
		
		if (weatherModel.currentWeather == null)
		{
			return;
		}
		
		UserModel userModel = UserModel.getUserModel();
		
		// currently the weather is
		this.addSoundToQueue(getVoiceDir() + "/currentlyTheWeatherIs.mp3");
		
		// weather
		String currentWeatherSound = weatherModel.currentWeather.currentSound +".mp3";
		
	    if (currentWeatherSound!=null)
	    {
	    	String weatherFileName = getVoiceDir() + "/weatherSounds/" + currentWeatherSound;
	        
	    	if (weatherModel.getIsDark() && currentWeatherSound.indexOf("sunny.mp3")!=-1)
	    	{
	    		weatherFileName = getVoiceDir() + "/weatherSounds/clear.mp3";
	    	}
	    	addSoundToQueue(weatherFileName);
	        
	        
	        // the temperature is
	    	this.addSoundToQueue(getVoiceDir() + "/theTempIs.mp3");
	        
	        // minus if < 0
	        if (weatherModel.currentWeather.currentTempF < 0)
	        {
	        	this.addSoundToQueue(getVoiceDir() + "/numbers/minus.mp3");
	        }
	        
	        // temp
	        if (userModel.userSettings.isCelcius)
	        {
	        	String tempFile = getVoiceDir() + "/numbers/" + weatherModel.currentWeather.currentTempC + ".mp3";
	        	this.addSoundToQueue(tempFile);
	        	this.addSoundToQueue(getVoiceDir() + "/degreesCelcius.mp3");
	        }
	        else {
	        	String tempFile = getVoiceDir() + "/numbers/" + weatherModel.currentWeather.currentTempF + ".mp3";
	        	this.addSoundToQueue(tempFile);
	        	this.addSoundToQueue(getVoiceDir() + "/degreesF.mp3");
	        }
	        
	        
	        // if not evening, weather forecast for today
	        if (!this.isEvening())
	        {
	        	this.addSoundToQueue(getVoiceDir() + "/forecastToday.mp3");
	        	String todayWeatherSound = getVoiceDir() + "/weatherSounds/"+ weatherModel.currentWeather.todaySound + ".mp3";
	        	addSoundToQueue(todayWeatherSound);
	            
	        	this.addSoundToQueue(getVoiceDir() + "/withHighsOf.mp3");
	            
	            if (userModel.userSettings.isCelcius)
	            {
	            	String tempFile =  getVoiceDir() + "/numbers/"+ weatherModel.currentWeather.todayTempC + ".mp3";
	            	addSoundToQueue(tempFile);
	                
	                // degrees C
	            	this.addSoundToQueue(getVoiceDir() + "/degreesCelcius.mp3");
	            }
	            else {
	            	String tempFile =  getVoiceDir() + "/numbers/"+ weatherModel.currentWeather.todayTempF + ".mp3";
	            	addSoundToQueue(tempFile);
	                
	                // degrees F
	            	this.addSoundToQueue(getVoiceDir() + "/degreesF.mp3");
	            }
	        }
	        else {
	            // else, weather forecast for tomorrow
	        	this.addSoundToQueue(getVoiceDir() + "/forecastTomorrow.mp3");
	            
	        	String tomorrowWeatherSound = getVoiceDir() + "/weatherSounds/"+ weatherModel.currentWeather.tomorrowSound + ".mp3";
	        	addSoundToQueue(tomorrowWeatherSound);
	            
	        	this.addSoundToQueue(getVoiceDir() + "/withHighsOf.mp3");
	            
	        	if (userModel.userSettings.isCelcius)
	            {
	            	String tempFile =  getVoiceDir() + "/numbers/"+ weatherModel.currentWeather.tomorrowTempC + ".mp3";
	            	addSoundToQueue(tempFile);
	                
	                // degrees C
	            	this.addSoundToQueue(getVoiceDir() + "/degreesCelcius.mp3");
	            }
	            else {
	            	String tempFile =  getVoiceDir() + "/numbers/"+ weatherModel.currentWeather.tomorrowTempF + ".mp3";
	            	addSoundToQueue(tempFile);
	                
	                // degrees F
	            	this.addSoundToQueue(getVoiceDir() + "/degreesF.mp3");
	            }
	            
	            
	        }
	    }

	}
	
	private String getMonthFile() {
		
		Calendar now =  Calendar.getInstance();
		String fileName ="";
		switch (now.get(Calendar.MONTH)+1) {
			case 1:
				fileName = "/months/january.mp3";
				break;
			case 2:
				fileName = "/months/february.mp3";
				break;
			case 3:
				fileName = "/months/march.mp3";
				break;
			case 4:
				fileName ="/months/april.mp3";
				break;
			case 5:
				fileName = "/months/may.mp3";
				break;
			case 6:
				fileName = "/months/june.mp3";
				break;
			case 7:
				fileName = "/months/july.mp3";
				break;
			case 8:
				fileName = "/months/august.mp3";
				break;
			case 9:
				fileName = "/months/september.mp3";
				break;
			case 10:
				fileName = "/months/october.mp3";
				break;
			case 11:
				fileName = "/months/november.mp3";
				break;
			case 12:
				fileName = "/months/december.mp3";
				break;
			default:
	            fileName = "";
				break;
		}
		
		return fileName;
	    
	}

	private String getDateFile() {
		
		Calendar now = Calendar.getInstance();
		int day = now.get(Calendar.DATE);
		String file = null;
		if (day!=1 && day!=2 && day!=3 && day!=21 && day!=22 && day!=23 && day!=31)
		{
			file = "/monthDates/"+ day +"th.mp3";
		}
		else if (day == 1 || day == 21 || day ==31)
		{
			file = "/monthDates/"+ day +"st.mp3";
		}
		else if (day == 2 || day == 22)
		{
			file = "/monthDates/"+ day +"nd.mp3";
		}
		else if (day == 3 || day == 23)
		{
			file = "/monthDates/"+ day +"rd.mp3";
		}
		
		return file;
	}

	private void addAddtionalMessage(Boolean forTest) {
		
		UserModel userModel = UserModel.getUserModel();
	    
		if ( (userModel.userSettings.additionalMessage != null && userModel.userSettings.additionalMessage.length() != 0 ) || forTest)
		{
			String filePath = Environment.getExternalStorageDirectory() +"/bedBuzz/";
			
			if (!forTest)
			{
				filePath += "additionalMessage.mp3";
			}
			else {
				filePath += "additionalMessageTest.mp3";
			}
	        
			addSoundToQueue(filePath);
	        
		}
	}
	
	private void addPersonalGreeting(Boolean isTest)
	{
		UserModel userModel = UserModel.getUserModel();
		if (userModel.userSettings.shouldGreetWithName || isTest)
		{
			// get document directory
			
			String filePath = Environment.getExternalStorageDirectory() +"/bedBuzz/";
			
			// get time now
			Date date = new Date();
			int hour = date.getHours();
			
			if (!isTest)
			{
				if (hour <12)
				{
					filePath += "goodMorning.mp3";
				}
				else if (hour >=12 && hour < 18)
				{
					filePath += "goodAfternoon.mp3";
				}
				else {
					filePath += "goodEvening.mp3";
				}
			}
			else {
				if (hour <12)
				{
					filePath += "goodMorningTest.mp3";
				}
				else if (hour >=12 && hour < 18)
				{
					filePath += "goodAfternoonTest.mp3";
				}
				else {
					filePath += "goodEveningTest.mp3";
				}
			}
	        
	        
			addSoundToQueue(filePath);
		}
		else
		{
			// get time now
			Date date = new Date();
			int hour = date.getHours();
			String filePath;
			
			if (hour <12)
			{
				filePath = getVoiceDir() + "/goodMorning.mp3"; 
			}
			else if (hour >=12 && hour < 18)
			{
				filePath =  getVoiceDir() + "/goodAfternoon.mp3"; 
			}
			else {
				filePath =   getVoiceDir() + "/goodEvening.mp3"; 
			}
			
			addSoundToQueue(filePath);
		}
	}
	
	private void addTimeFilesToQueue() {
		this.addSoundToQueue(getVoiceDir() + "/todaysDate.mp3");
		
		Date now = new Date();
		
		switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
			case 1:
				// sunday
				addSoundToQueue(getVoiceDir() + "/days/sunday.mp3");
	            
				break;
			case 2:
				// mon
				addSoundToQueue(getVoiceDir() + "/days/monday.mp3");
	            
				break;
			case 3:
				// tue

				addSoundToQueue(getVoiceDir() + "/days/tuesday.mp3");
	            
				break;
			case 4:
				// wed
				addSoundToQueue(getVoiceDir() + "/days/wednesday.mp3");
	            
				break;
			case 5:
				// thu
				addSoundToQueue(getVoiceDir() + "/days/thursday.mp3");
	            
				break;
			case 6:
				// fri
				addSoundToQueue(getVoiceDir() + "/days/friday.mp3");
	            
				break;
			case 7:
				// sat
				addSoundToQueue(getVoiceDir() + "/days/saturday.mp3");
	            
				break;
			default:
				break;
		}
		
		String file = getMonthFile();
		addSoundToQueue(getVoiceDir() + file);
		
		file = getDateFile();
		addSoundToQueue(getVoiceDir() + file);
		
		String amPMFile;
		int hour = now.getHours();
		if (hour >=12)
		{
			
			amPMFile = getVoiceDir() + "/pm.mp3";
		}
		else
		{
			amPMFile =  getVoiceDir() + "/am.mp3";	
		}
		
		
		if (hour == 0)
		{
			hour = 12;
		}
		else if (hour >12)
		{
			// convert hour to 12 hr (from 24)
			hour = hour -12;
		}
		
		String hourFile = getVoiceDir() + "/numbers/"+ hour+".mp3";
		
		int minute = now.getMinutes();
		
		String minuteFile = getVoiceDir() +"/numbers/"+minute+".mp3";
		
		addSoundToQueue(getVoiceDir() + "/theTimeIs.mp3");
		
		addSoundToQueue(hourFile);
		
		if (minute != 0)
		{
			if (minute < 10)
			{
				addSoundToQueue(getVoiceDir() + "/oh.mp3");
			}
			addSoundToQueue(minuteFile);
		}
		
		addSoundToQueue(amPMFile);
	}
	
	public void  playWeather() {
	    
		// reset the play queue
		resetPlayQueue();
	    
		addWeatherFilesToQueue();
		
		playNextSoundInQueue();
	    
	}
	
	public void  playTime() {
		
		// reset the play queue
		resetPlayQueue();
		
		addPersonalGreeting(false);
		
		addAddtionalMessage(false);
		
		addTimeFilesToQueue();
		
		playNextSoundInQueue();
	}
	
	
	
	public void playAlarm(AlarmVO currentAlarm) {
		
		if (currentAlarm == null)
			return;
		
		justPlayedRSS = false;
		_currentAlarm = currentAlarm;
		
		// reset the play queue
		resetPlayQueue();
		
		addPersonalGreeting(false);
		
		addAddtionalMessage(false);
		
		addTimeFilesToQueue();
		
		addWeatherFilesToQueue();
		
		this.addMessagesToPlayQueue();
		
		this.addRSSClipsToPlayQueue();
		
		
		
		playNextSoundInQueue();
	}
	
	public void playAlarmMusic() {
		
		try {
			player.setDataSource(_currentAlarm.musicFile);
			player.prepare();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
        player.start();
        player.setLooping(true);
	}

	public void stopSounds() {
		// reset the play queue
		resetPlayQueue();
		
		this.player.stop();
		
	}

	public void playTestMessage(String fileName) {
		// reset the play queue
		resetPlayQueue();
		
		addSoundToQueue(Environment.getExternalStorageDirectory() +"/bedBuzz/testMessage.mp3");
		
		playNextSoundInQueue();
	}
	
	public void playRSSClips() {
		
		// reset the play queue
		resetPlayQueue();
		
		addRSSClipsToPlayQueue();
		
		playNextSoundInQueue();
	}
	
	
	private String storeMp3FromByteArr(byte[] mp3SoundByteArray, String soundType, int soundNumber) {
	    try {
	        // create temp file that will hold byte array
	        File tempMp3 = File.createTempFile(soundType+soundNumber, ".mp3", this.context.getCacheDir());
	    	
	    	// File tempMp3 = File.createTempFile("sound"+soundNumber, ".mp3", this.context.getExternalFilesDir(null));
		        
	    	// File tempMp3 = File.createTempFile(soundType+soundNumber, ".mp3", this.context.getExternalFilesDir(null));
	    	
	        FileOutputStream fos = new FileOutputStream(tempMp3);
	        fos.write(mp3SoundByteArray);
	        fos.close();

	        return tempMp3.getAbsolutePath().toString();
	        
	    } catch (IOException ex) {
	        String s = ex.toString();
	        ex.printStackTrace();
	    }
		return null;
	}

	public void playWelcomeMessage() {
		this.resetPlayQueue();
		this.addSoundToQueue(getVoiceDir() + "/welcomeWizard1.mp3");
		playNextSoundInQueue();
	}
	
	public void playCanYouTellmeYourName() {
		this.resetPlayQueue();
		this.addSoundToQueue(getVoiceDir() + "/initialGreeting.mp3");
		playNextSoundInQueue();
	}
	
	public void playEnableFacebookQuestion() {
		this.resetPlayQueue();
		this.addPersonalGreeting(false);
		this.addSoundToQueue(getVoiceDir() + "/enableFacebook.mp3");
		playNextSoundInQueue();
	}

	public void addRSSClipsToPlayQueue() {
		RSSModel rssModel = RSSModel.getRSSModell();
		
		RSSModel.getRSSModell().rssClipsIndexedByURL = new HashMap<String,RSSClip>();
		
		if (rssModel.rssClips==null)
		{
			return;
		}
		
		int i=0;
		for (RSSClip clip : rssModel.rssClips)
		{
			String rssSoundFileName = storeMp3FromByteArr(clip.sound,"rss",i);
			addSoundToQueue(rssSoundFileName);
			i++;
			
			RSSModel.getRSSModell().rssClipsIndexedByURL.put(rssSoundFileName, clip);
		}
		
	}

	public void playThanksForReviewing() {
		this.resetPlayQueue();
		this.addSoundToQueue("english.ukfemale/reviewApp.mp3");
		playNextSoundInQueue();
		
	}
	
	public void playPleaseReview() {
		this.resetPlayQueue();
		this.addSoundToQueue("english.ukfemale/leaveReview.mp3");
		playNextSoundInQueue();
		
	}

	public void playChangeVoiceQuestion() {
		this.resetPlayQueue();
		this.addSoundToQueue("english.ukfemale/voiceQuestion.mp3");
		playNextSoundInQueue();
		
	}
	
	public void playSubscribeQuestion() {
		this.resetPlayQueue();
		this.addSoundToQueue("english.ukfemale/subscribeQuestion.mp3");
		playNextSoundInQueue();
		
	}
	
	public void playSendMessageQuestion() {
		this.resetPlayQueue();
		this.addSoundToQueue("english.ukfemale/wakeUpMessageQuestion.mp3");
		playNextSoundInQueue();
		
	}
	
	public int getNumberOfSoundsInPlayQueue() {
		
		return playQueue.size();
	}

	public void stopAndPlaynextSound() {
		player.stop();
		
		// next sound should play automatically?
		
	}

	public void playSelectFriendsHelp() {
		this.resetPlayQueue();
		this.addSoundToQueue("english.ukfemale/firstSelectFriends.mp3");
		playNextSoundInQueue();
	}
	
	public void playComposeMessageHelp() {
		this.resetPlayQueue();
		this.addSoundToQueue("english.ukfemale/typeMessage.mp3");
		playNextSoundInQueue();
	}

	public void playMessageFromMic() {
		this.resetPlayQueue();
		this.addSoundToQueue(ComposeMessageActivity.messageFromMicFileName);
		playNextSoundInQueue();
		
	}

	public void playCountdown() {
		this.isCountdownPlaying  = true;
		this.resetPlayQueue();
		this.addSoundToQueue(getVoiceDir() + "/numbers/3.mp3");
		this.addSoundToQueue(getVoiceDir() + "/numbers/2.mp3");
		this.addSoundToQueue(getVoiceDir() + "/numbers/1.mp3");
		playNextSoundInQueue();
		
	}
}
